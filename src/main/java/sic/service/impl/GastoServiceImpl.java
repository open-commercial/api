package sic.service.impl;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaGastoCriteria;
import sic.service.IGastoService;
import java.time.LocalDateTime;
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

@Service
@Validated
public class GastoServiceImpl implements IGastoService {

  private final GastoRepository gastoRepository;
  private final ISucursalService sucursalService;
  private final ICajaService cajaService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
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
    return gastoRepository.findAll(
        this.getBuilder(criteria),
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    String ordenDefault = "fecha";
    if(pagina == null) pagina = 0;
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  private BooleanBuilder getBuilder(BusquedaGastoCriteria criteria) {
    QGasto qGasto = QGasto.gasto;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getConcepto() != null) {
      String[] terminos = criteria.getConcepto().split(" ");
      BooleanBuilder rsPredicate = new BooleanBuilder();
      for (String termino : terminos) {
        rsPredicate.and(qGasto.concepto.containsIgnoreCase(termino));
      }
      builder.or(rsPredicate);
    }
    if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
      criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
      criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        builder.and(qGasto.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        builder.and(qGasto.fecha.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        builder.and(qGasto.fecha.before(criteria.getFechaHasta()));
      }
    }
    if (criteria.getIdFormaDePago() != null)
      builder.or(qGasto.formaDePago.id_FormaDePago.eq(criteria.getIdFormaDePago()));
    if (criteria.getNroGasto() != null) builder.or(qGasto.nroGasto.eq(criteria.getNroGasto()));
    if (criteria.getIdUsuario() != null)
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
    Sucursal sucursal, FormaDePago formaDePago, LocalDateTime desde, LocalDateTime hasta) {
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
    long idSucursal, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        gastoRepository.getTotalGastosEntreFechasPorFormaDePago(
          idSucursal, idFormaDePago, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastosQueAfectanCajaEntreFechas(
    long idSucursal, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total =
        gastoRepository.getTotalGastosQueAfectanCajaEntreFechas(idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastosEntreFechas(long idSucursal, LocalDateTime desde, LocalDateTime hasta) {
    BigDecimal total = gastoRepository.getTotalGastosEntreFechas(idSucursal, desde, hasta);
    return (total == null) ? BigDecimal.ZERO : total;
  }

  @Override
  public BigDecimal getTotalGastos(BusquedaGastoCriteria criteria) {
    return gastoRepository.getTotalGastos(this.getBuilder(criteria));
  }
}
