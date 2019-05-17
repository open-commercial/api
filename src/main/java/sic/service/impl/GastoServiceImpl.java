package sic.service.impl;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.service.IGastoService;

import java.util.*;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.service.BusinessServiceException;
import sic.repository.GastoRepository;
import sic.service.ICajaService;
import sic.service.IEmpresaService;
import sic.util.FormatterFechaHora;

@Service
@Validated
public class GastoServiceImpl implements IGastoService {

  private final GastoRepository gastoRepository;
  private final IEmpresaService empresaService;
  private final ICajaService cajaService;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  @Lazy
  public GastoServiceImpl(
      GastoRepository gastoRepository, IEmpresaService empresaService, ICajaService cajaService) {
    this.gastoRepository = gastoRepository;
    this.empresaService = empresaService;
    this.cajaService = cajaService;
  }

  @Override
  public Gasto getGastoPorId(Long idGasto) {
    return gastoRepository
        .findById(idGasto)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    ResourceBundle.getBundle("Mensajes").getString("mensaje_gasto_no_existente")));
  }

  @Override
  public void validarOperacion(Gasto gasto) {
    this.cajaService.validarMovimiento(gasto.getFecha(), gasto.getEmpresa().getId_Empresa());
    if (gastoRepository.findById(gasto.getId_Gasto()).isPresent()) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_gasto_duplicada"));
    }
  }

  @Override
  public Page<Gasto> buscarGastos(BusquedaGastoCriteria criteria) {
    return gastoRepository.findAll(this.getBuilder(criteria), criteria.getPageable());
  }

  private BooleanBuilder getBuilder(BusquedaGastoCriteria criteria) {
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_pedido_fechas_busqueda_invalidas"));
    }
    if (criteria.isBuscaPorFecha()) {
      Calendar cal = new GregorianCalendar();
      cal.setTime(criteria.getFechaDesde());
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      criteria.setFechaDesde(cal.getTime());
      cal.setTime(criteria.getFechaHasta());
      cal.set(Calendar.HOUR_OF_DAY, 23);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 59);
      criteria.setFechaHasta(cal.getTime());
    }
    QGasto qGasto = QGasto.gasto;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.isBuscaPorConcepto()) {
      String[] terminos = criteria.getConcepto().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qGasto.concepto.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.isBuscaPorFecha()) {
      FormatterFechaHora formateadorFecha =
          new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
      DateExpression<Date> fDesde =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaDesde()));
      DateExpression<Date> fHasta =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaHasta()));
      builder.and(qGasto.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscarPorFormaDePago())
      builder.or(qGasto.formaDePago.id_FormaDePago.eq(criteria.getIdFormaDePago()));
    if (criteria.isBuscaPorNro()) builder.or(qGasto.nroGasto.eq(criteria.getNroGasto()));
    if (criteria.isBuscaPorUsuario())
      builder.and(qGasto.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    builder.and(
        qGasto.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qGasto.eliminado.eq(false)));
    return builder;
  }

  @Override
  @Transactional
  public Gasto guardar(@Valid Gasto gasto) {
    gasto.setNroGasto(this.getUltimoNumeroDeGasto(gasto.getEmpresa().getId_Empresa()) + 1);
    this.validarOperacion(gasto);
    gasto = gastoRepository.save(gasto);
    logger.warn("El Gasto {} se guardó correctamente.", gasto);
    return gasto;
  }

  @Override
  public List<Gasto> getGastosEntreFechasYFormaDePago(
      Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta) {
    return gastoRepository.getGastosEntreFechasPorFormaDePago(
        empresa.getId_Empresa(), formaDePago.getId_FormaDePago(), desde, hasta);
  }

  @Override
  @Transactional
  public void actualizar(@Valid Gasto gasto) {
    gastoRepository.save(gasto);
  }

  @Override
  @Transactional
  public void eliminar(long idGasto) {
    Gasto gastoParaEliminar = this.getGastoPorId(idGasto);
    if (this.cajaService
        .getUltimaCaja(gastoParaEliminar.getEmpresa().getId_Empresa())
        .getEstado()
        .equals(EstadoCaja.CERRADA)) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_caja_cerrada"));
    }
    gastoParaEliminar.setEliminado(true);
    gastoRepository.save(gastoParaEliminar);
  }

  @Override
  public long getUltimoNumeroDeGasto(long idEmpresa) {
    Gasto gasto =
        gastoRepository.findTopByEmpresaAndEliminadoOrderByNroGastoDesc(
            empresaService.getEmpresaPorId(idEmpresa), false);
    if (gasto == null) {
      return 1; // No existe ningun Gasto anterior
    } else {
      return 1 + gasto.getNroGasto();
    }
  }

  @Override
  public BigDecimal getTotalGastosEntreFechasYFormaDePago(
      long idEmpresa, long idFormaDePago, Date desde, Date hasta) {
    BigDecimal total =
        gastoRepository.getTotalGastosEntreFechasPorFormaDePago(
            idEmpresa, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastosQueAfectanCajaEntreFechas(
      long idEmpresa, Date desde, Date hasta) {
    BigDecimal total =
        gastoRepository.getTotalGastosQueAfectanCajaEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastosEntreFechas(long idEmpresa, Date desde, Date hasta) {
    BigDecimal total = gastoRepository.getTotalGastosEntreFechas(idEmpresa, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastos(BusquedaGastoCriteria criteria) {
    return gastoRepository.getTotalGastos(this.getBuilder(criteria));
  }
}
