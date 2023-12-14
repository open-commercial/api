package sic.service.impl;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaGastoCriteria;
import sic.service.IGastoService;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.repository.GastoRepository;
import sic.service.ICajaService;
import sic.service.ISucursalService;
import sic.util.CustomValidator;

@Service
@Slf4j
public class GastoServiceImpl implements IGastoService {

  private final GastoRepository gastoRepository;
  private final ISucursalService sucursalService;
  private final ICajaService cajaService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  @Lazy
  public GastoServiceImpl(
    GastoRepository gastoRepository,
    ISucursalService sucursalService,
    ICajaService cajaService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.gastoRepository = gastoRepository;
    this.sucursalService = sucursalService;
    this.cajaService = cajaService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
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
  public void validarReglasDeNegocio(Gasto gasto) {
    this.cajaService.validarMovimiento(gasto.getFecha(), gasto.getSucursal().getIdSucursal());
    if (gastoRepository.existsByNroGastoAndSucursal(gasto.getNroGasto(), gasto.getSucursal())) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_gasto_duplicada", null, Locale.getDefault()));
    }
  }

  @Override
  public Page<Gasto> buscarGastos(BusquedaGastoCriteria criteria) {
    return gastoRepository.findAll(
        this.getBuilder(criteria),
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  @Override
  public Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    String ordenDefault = "fecha";
    if(pagina == null) pagina = 0;
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public BooleanBuilder getBuilder(BusquedaGastoCriteria criteria) {
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
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        builder.and(qGasto.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        builder.and(qGasto.fecha.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        builder.and(qGasto.fecha.before(criteria.getFechaHasta()));
      }
    }
    if (criteria.getIdFormaDePago() != null)
      builder.and(qGasto.formaDePago.idFormaDePago.eq(criteria.getIdFormaDePago()));
    if (criteria.getNroGasto() != null) builder.and(qGasto.nroGasto.eq(criteria.getNroGasto()));
    if (criteria.getIdUsuario() != null)
      builder.and(qGasto.usuario.idUsuario.eq(criteria.getIdUsuario()));
    if (criteria.getIdSucursal() != null) {
      builder.and(
              qGasto.sucursal.idSucursal.eq(criteria.getIdSucursal()).and(qGasto.eliminado.eq(false)));
    }
    return builder;
  }

  @Override
  @Transactional
  public Gasto guardar(Gasto gasto) {
    customValidator.validar(gasto);
    gasto.setNroGasto(this.getUltimoNumeroDeGasto(gasto.getSucursal().getIdSucursal()));
    this.validarReglasDeNegocio(gasto);
    gasto = gastoRepository.save(gasto);
    log.warn("El Gasto {} se guardó correctamente.", gasto);
    return gasto;
  }

  @Override
  public List<Gasto> getGastosEntreFechasYFormaDePago(
    Sucursal sucursal, FormaDePago formaDePago, LocalDateTime desde, LocalDateTime hasta) {
    return gastoRepository.getGastosEntreFechasPorFormaDePago(
        sucursal.getIdSucursal(), formaDePago.getIdFormaDePago(), desde, hasta);
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
        gastoRepository.findTopBySucursalOrderByNroGastoDesc(sucursalService.getSucursalPorId(idSucursal));
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
