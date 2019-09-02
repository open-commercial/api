package sic.service.impl;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaGastoCriteria;
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
import sic.exception.BusinessServiceException;
import sic.repository.GastoRepository;
import sic.service.ICajaService;
import sic.service.ISucursalService;
import sic.util.FormatterFechaHora;

@Service
@Validated
public class GastoServiceImpl implements IGastoService {

  private final GastoRepository gastoRepository;
  private final ISucursalService sucursalService;
  private final ICajaService cajaService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  @Lazy
  public GastoServiceImpl(
      GastoRepository gastoRepository,
      ISucursalService sucursalService,
      ICajaService cajaService,
      MessageSource messageSource) {
    this.gastoRepository = gastoRepository;
    this.sucursalService = sucursalService;
    this.cajaService = cajaService;
    this.messageSource = messageSource;
  }

  @Override
  public Gasto getGastoNoEliminadoPorId(Long idGasto) {
    Optional<Gasto> gasto = gastoRepository
      .findById(idGasto);
    if (gasto.isPresent() && !gasto.get().isEliminado()) {
      return gasto.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_gasto_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public void validarOperacion(Gasto gasto) {
    this.cajaService.validarMovimiento(gasto.getFecha(), gasto.getSucursal().getIdSucursal());
    if (gastoRepository.findById(gasto.getId_Gasto()).isPresent()) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_gasto_duplicada", null, Locale.getDefault()));
    }
  }

  @Override
  public Page<Gasto> buscarGastos(BusquedaGastoCriteria criteria) {
    return gastoRepository.findAll(this.getBuilder(criteria), criteria.getPageable());
  }

  private BooleanBuilder getBuilder(BusquedaGastoCriteria criteria) {
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_gasto_fechas_busqueda_invalidas", null, Locale.getDefault()));
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
        qGasto.sucursal.idSucursal.eq(criteria.getIdSucursal()).and(qGasto.eliminado.eq(false)));
    return builder;
  }

  @Override
  @Transactional
  public Gasto guardar(@Valid Gasto gasto) {
    gasto.setNroGasto(this.getUltimoNumeroDeGasto(gasto.getSucursal().getIdSucursal()) + 1);
    this.validarOperacion(gasto);
    gasto = gastoRepository.save(gasto);
    logger.warn("El Gasto {} se guardó correctamente.", gasto);
    return gasto;
  }

  @Override
  public List<Gasto> getGastosEntreFechasYFormaDePago(
    Sucursal sucursal, FormaDePago formaDePago, Date desde, Date hasta) {
    return gastoRepository.getGastosEntreFechasPorFormaDePago(
        sucursal.getIdSucursal(), formaDePago.getId_FormaDePago(), desde, hasta);
  }

  @Override
  @Transactional
  public void eliminar(long idGasto) {
    Gasto gastoParaEliminar = this.getGastoNoEliminadoPorId(idGasto);
    if (this.cajaService
        .getUltimaCaja(gastoParaEliminar.getSucursal().getIdSucursal())
        .getEstado()
        .equals(EstadoCaja.CERRADA)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_caja_cerrada", null, Locale.getDefault()));
    }
    gastoParaEliminar.setEliminado(true);
    gastoRepository.save(gastoParaEliminar);
  }

  @Override
  public long getUltimoNumeroDeGasto(long idSucursal) {
    Gasto gasto =
        gastoRepository.findTopBySucursalAndEliminadoOrderByNroGastoDesc(
            sucursalService.getSucursalPorId(idSucursal), false);
    if (gasto == null) {
      return 1; // No existe ningun Gasto anterior
    } else {
      return 1 + gasto.getNroGasto();
    }
  }

  @Override
  public BigDecimal getTotalGastosEntreFechasYFormaDePago(
    long idSucursal, long idFormaDePago, Date desde, Date hasta) {
    BigDecimal total =
        gastoRepository.getTotalGastosEntreFechasPorFormaDePago(
          idSucursal, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastosQueAfectanCajaEntreFechas(
    long idSucursal, Date desde, Date hasta) {
    BigDecimal total =
        gastoRepository.getTotalGastosQueAfectanCajaEntreFechas(idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastosEntreFechas(long idSucursal, Date desde, Date hasta) {
    BigDecimal total = gastoRepository.getTotalGastosEntreFechas(idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastos(BusquedaGastoCriteria criteria) {
    return gastoRepository.getTotalGastos(this.getBuilder(criteria));
  }
}
