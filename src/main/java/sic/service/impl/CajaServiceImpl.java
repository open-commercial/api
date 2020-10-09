package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.context.MessageSource;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaCajaCriteria;
import sic.service.*;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.repository.CajaRepository;
import sic.util.CustomValidator;

@Service
public class CajaServiceImpl implements ICajaService {

  private final CajaRepository cajaRepository;
  private final IFormaDePagoService formaDePagoService;
  private final IGastoService gastoService;
  private final ISucursalService sucursalService;
  private final IUsuarioService usuarioService;
  private final IReciboService reciboService;
  private final IClockService clockService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public CajaServiceImpl(
    CajaRepository cajaRepository,
    IFormaDePagoService formaDePagoService,
    IGastoService gastoService,
    ISucursalService sucursalService,
    IUsuarioService usuarioService,
    IReciboService reciboService,
    IClockService clockService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.cajaRepository = cajaRepository;
    this.formaDePagoService = formaDePagoService;
    this.gastoService = gastoService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.reciboService = reciboService;
    this.clockService = clockService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public void validarReglasDeNegocio(Caja caja) {
    // Una Caja por dia
    Caja ultimaCaja = this.getUltimaCaja(caja.getSucursal().getIdSucursal());
    if (ultimaCaja != null) {
      if (ultimaCaja.getEstado() == EstadoCaja.ABIERTA) {
        throw new BusinessServiceException(
            messageSource.getMessage("mensaje_caja_anterior_abierta", null, Locale.getDefault()));
      }
      if (caja.getFechaApertura().isBefore(ultimaCaja.getFechaCierre())) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_fecha_apertura_no_valida", null, Locale.getDefault()));
      }
    }
    // Duplicados
    if (cajaRepository.findById(caja.getIdCaja()) != null) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_caja_duplicada", null, Locale.getDefault()));
    }
  }

  @Override
  public void validarMovimiento(LocalDateTime fechaMovimiento, long idSucursal) {
    Caja caja = this.getUltimaCaja(idSucursal);
    if (caja == null) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_caja_no_existente", null, Locale.getDefault()));
    }
    if (caja.getEstado().equals(EstadoCaja.CERRADA)) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_caja_cerrada", null, Locale.getDefault()));
    }
    if (fechaMovimiento.isBefore(caja.getFechaApertura())) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_caja_movimiento_fecha_no_valida", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public Caja abrirCaja(Sucursal sucursal, Usuario usuarioApertura, BigDecimal saldoApertura) {
    Caja caja = new Caja();
    caja.setEstado(EstadoCaja.ABIERTA);
    caja.setSucursal(sucursal);
    caja.setSaldoApertura(saldoApertura);
    caja.setUsuarioAbreCaja(usuarioApertura);
    caja.setFechaApertura(this.clockService.getFechaActual());
    customValidator.validar(caja);
    this.validarReglasDeNegocio(caja);
    return cajaRepository.save(caja);
  }

  @Override
  public void actualizar(Caja caja) {
    customValidator.validar(caja);
    cajaRepository.save(caja);
  }

  @Override
  @Transactional
  public void eliminar(Long idCaja) {
    Caja caja = this.getCajaPorId(idCaja);
    if (caja == null) {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_caja_no_existente", null, Locale.getDefault()));
    }
    caja.setEliminada(true);
    this.actualizar(caja);
  }

  @Override
  public Caja getUltimaCaja(long idSucursal) {
    Pageable pageable = PageRequest.of(0, 1);
    List<Caja> topCaja =
        cajaRepository
            .findTopBySucursalAndEliminadaOrderByIdCajaDesc(idSucursal, pageable)
            .getContent();
    return (topCaja.isEmpty()) ? null : topCaja.get(0);
  }

  @Override
  public Caja getCajaPorId(Long idCaja) {
    Optional<Caja> caja = cajaRepository.findById(idCaja);
    if (caja.isPresent() && !caja.get().isEliminada()) {
      return caja.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_caja_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public Page<Caja> buscarCajas(BusquedaCajaCriteria criteria) {
    return cajaRepository.findAll(
        this.getBuilder(criteria),
        this.getPageable(criteria.getPagina(), criteria.getOrdenarPor(), criteria.getSentido()));
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "fechaApertura";
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

  private BooleanBuilder getBuilder(BusquedaCajaCriteria criteria) {
    QCaja qCaja = QCaja.caja;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qCaja.sucursal.idSucursal.eq(criteria.getIdSucursal()).and(qCaja.eliminada.eq(false)));
    if (criteria.getIdUsuarioApertura() != null && criteria.getIdUsuarioCierre() == null) {
      builder.and(qCaja.usuarioAbreCaja.idUsuario.eq(criteria.getIdUsuarioApertura()));
    }
    if (criteria.getIdUsuarioApertura() == null && criteria.getIdUsuarioCierre() != null) {
      builder.and(qCaja.usuarioCierraCaja.idUsuario.eq(criteria.getIdUsuarioCierre()));
    }
    if (criteria.getIdUsuarioApertura() != null && criteria.getIdUsuarioCierre() != null) {
      builder.and(
          qCaja
              .usuarioAbreCaja
              .idUsuario
              .eq(criteria.getIdUsuarioApertura())
              .and(qCaja.usuarioCierraCaja.idUsuario.eq(criteria.getIdUsuarioCierre())));
    }
    if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
      criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
      criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        builder.and(
            qCaja.fechaApertura.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        builder.and(qCaja.fechaApertura.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        builder.and(qCaja.fechaApertura.before(criteria.getFechaHasta()));
      }
    }
    return builder;
  }

  @Override
  public Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling) {
    Caja cajaACerrar = this.getCajaPorId(idCaja);
    cajaACerrar.setSaldoReal(monto);
    if (scheduling) {
      cajaACerrar.setFechaCierre(
          cajaACerrar.getFechaApertura().withHour(23).withMinute(59).withSecond(59));
    } else {
      cajaACerrar.setFechaCierre(this.clockService.getFechaActual());
    }
    if (idUsuario != null) {
      cajaACerrar.setUsuarioCierraCaja(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    }
    cajaACerrar.setSaldoSistema(this.getSaldoSistema(cajaACerrar));
    cajaACerrar.setEstado(EstadoCaja.CERRADA);
    this.actualizar(cajaACerrar);
    logger.warn("La Caja {} se cerró correctamente.", cajaACerrar);
    return cajaACerrar;
  }

  @Scheduled(cron = "30 0 0 * * *") // Todos los dias a las 00:00:30
  public void cerrarCajas() {
    logger.warn("Cierre automático de Cajas a las {}", LocalDateTime.now());
    List<Sucursal> sucursales = this.sucursalService.getSucusales(false);
    sucursales.stream()
        .map(sucursal -> this.getUltimaCaja(sucursal.getIdSucursal()))
        .filter(
            ultimaCajaDeSucursal ->
                ((ultimaCajaDeSucursal != null)
                    && (ultimaCajaDeSucursal.getEstado() == EstadoCaja.ABIERTA)))
        .forEachOrdered(
            ultimaCajadeSucursal -> {
              if (ultimaCajadeSucursal.getFechaApertura().isBefore(LocalDateTime.now())) {
                this.cerrarCaja(
                    ultimaCajadeSucursal.getIdCaja(),
                    this.getSaldoQueAfectaCaja(ultimaCajadeSucursal),
                    ultimaCajadeSucursal.getUsuarioAbreCaja().getIdUsuario(),
                    true);
              }
            });
  }

  @Override
  public BigDecimal getSaldoQueAfectaCaja(Caja caja) {
    LocalDateTime fechaHasta = LocalDateTime.now();
    if (caja.getFechaCierre() != null) {
      fechaHasta = caja.getFechaCierre();
    }
    BigDecimal totalRecibosCliente =
        reciboService.getTotalRecibosClientesQueAfectanCajaEntreFechas(
            caja.getSucursal().getIdSucursal(), caja.getFechaApertura(), fechaHasta);
    BigDecimal totalRecibosProveedor =
        reciboService.getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
            caja.getSucursal().getIdSucursal(), caja.getFechaApertura(), fechaHasta);
    BigDecimal totalGastos =
        gastoService.getTotalGastosQueAfectanCajaEntreFechas(
            caja.getSucursal().getIdSucursal(), caja.getFechaApertura(), fechaHasta);
    return caja.getSaldoApertura()
        .add(totalRecibosCliente)
        .subtract(totalRecibosProveedor)
        .subtract(totalGastos);
  }

  @Override
  public BigDecimal getSaldoSistema(Caja caja) {
    if (caja.getEstado().equals(EstadoCaja.ABIERTA)) {
      LocalDateTime fechaHasta = LocalDateTime.now();
      if (caja.getFechaCierre() != null) {
        fechaHasta = caja.getFechaCierre();
      }
      BigDecimal totalRecibosCliente =
          reciboService.getTotalRecibosClientesEntreFechas(
              caja.getSucursal().getIdSucursal(), caja.getFechaApertura(), fechaHasta);
      BigDecimal totalRecibosProveedor =
          reciboService.getTotalRecibosProveedoresEntreFechas(
              caja.getSucursal().getIdSucursal(), caja.getFechaApertura(), fechaHasta);
      BigDecimal totalGastos =
          gastoService.getTotalGastosEntreFechas(
              caja.getSucursal().getIdSucursal(), caja.getFechaApertura(), fechaHasta);
      return caja.getSaldoApertura()
          .add(totalRecibosCliente)
          .subtract(totalRecibosProveedor)
          .subtract(totalGastos);
    } else {
      return caja.getSaldoSistema();
    }
  }

  @Override
  public boolean isUltimaCajaAbierta(long idSucursal) {
    Caja caja = cajaRepository.isUltimaCajaAbierta(idSucursal);
    return (caja != null)
        && cajaRepository.isUltimaCajaAbierta(idSucursal).getEstado().equals(EstadoCaja.ABIERTA);
  }

  @Override
  public Map<Long, BigDecimal> getIdsFormasDePagoAndMontos(long idCaja) {
    Caja caja = cajaRepository.findById(idCaja);
    Map<Long, BigDecimal> totalesPorFomaDePago = new HashMap<>();
    List<MovimientoCaja> movimientos = new ArrayList<>();
    formaDePagoService
        .getFormasDePago()
        .forEach(
            fdp -> {
              movimientos.addAll(
                  this.getMovimientosPorFormaDePagoEntreFechas(
                      caja.getSucursal(),
                      fdp,
                      caja.getFechaApertura(),
                      (caja.getFechaCierre() != null
                          ? caja.getFechaCierre()
                          : LocalDateTime.now())));
              if (!movimientos.isEmpty()) {
                BigDecimal total =
                    movimientos.stream()
                        .map(MovimientoCaja::getMonto)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalesPorFomaDePago.put(fdp.getIdFormaDePago(), total);
              }
              movimientos.clear();
            });
    return totalesPorFomaDePago;
  }

  @Override
  public BigDecimal getSaldoSistemaCajas(BusquedaCajaCriteria criteria) {
    return cajaRepository.getSaldoSistemaCajas(this.getBuilder(criteria));
  }

  @Override
  public BigDecimal getSaldoRealCajas(BusquedaCajaCriteria criteria) {
    return cajaRepository.getSaldoRealCajas(this.getBuilder(criteria));
  }

  @Override
  public List<MovimientoCaja> getMovimientosPorFormaDePagoEntreFechas(
    Sucursal sucursal, FormaDePago formaDePago, LocalDateTime desde, LocalDateTime hasta) {
    List<MovimientoCaja> movimientos = new ArrayList<>();
    gastoService
        .getGastosEntreFechasYFormaDePago(sucursal, formaDePago, desde, hasta)
        .forEach(gasto -> movimientos.add(new MovimientoCaja(gasto)));
    reciboService
        .getRecibosEntreFechasPorFormaDePago(desde, hasta, formaDePago, sucursal)
        .forEach(recibo -> movimientos.add(new MovimientoCaja(recibo)));
    Collections.sort(movimientos);
    return movimientos;
  }

  @Override
  @Transactional
  public void reabrirCaja(long idCaja, BigDecimal saldoAperturaNuevo) {
    Caja caja = getCajaPorId(idCaja);
    Caja ultimaCaja = this.getUltimaCaja(caja.getSucursal().getIdSucursal());
    if (ultimaCaja == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_caja_no_existente", null, Locale.getDefault()));
    }
    if (caja.getIdCaja() == ultimaCaja.getIdCaja()) {
      caja.setSaldoSistema(null);
      caja.setSaldoApertura(saldoAperturaNuevo);
      caja.setSaldoReal(null);
      caja.setEstado(EstadoCaja.ABIERTA);
      caja.setUsuarioCierraCaja(null);
      caja.setFechaCierre(null);
      this.actualizar(caja);
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_caja_re_apertura_no_valida", null, Locale.getDefault()));
    }
  }

  @Override
  public Caja encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(
      long idSucursal, LocalDateTime fecha) {
    return cajaRepository.encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(
        idSucursal, fecha);
  }

  @Override
  @Transactional
  public void actualizarSaldoSistema(Caja caja, BigDecimal monto) {
    cajaRepository.actualizarSaldoSistema(caja.getIdCaja(), monto);
  }

}
