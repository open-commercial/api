package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import sic.modelo.*;
import sic.service.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
import sic.util.FormatterFechaHora;
import sic.util.Validator;
import sic.repository.CajaRepository;

@Service
public class CajaServiceImpl implements ICajaService {

  private final CajaRepository cajaRepository;
  private final IFormaDePagoService formaDePagoService;
  private final IGastoService gastoService;
  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;
  private final IReciboService reciboService;
  private final IClockService clockService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public CajaServiceImpl(
      CajaRepository cajaRepository,
      IFormaDePagoService formaDePagoService,
      IGastoService gastoService,
      IEmpresaService empresaService,
      IUsuarioService usuarioService,
      IReciboService reciboService,
      IClockService clockService) {
    this.cajaRepository = cajaRepository;
    this.formaDePagoService = formaDePagoService;
    this.gastoService = gastoService;
    this.empresaService = empresaService;
    this.usuarioService = usuarioService;
    this.reciboService = reciboService;
    this.clockService = clockService;
  }

  @Override
  public void validarCaja(Caja caja) {
    // Entrada de Datos
    // Requeridos
    if (caja.getFechaApertura() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_caja_fecha_vacia"));
    }
    if (caja.getEmpresa() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_caja_empresa_vacia"));
    }
    if (caja.getUsuarioAbreCaja() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_caja_usuario_vacio"));
    }
    // Una Caja por dia
    Caja ultimaCaja = this.getUltimaCaja(caja.getEmpresa().getId_Empresa());
    if (ultimaCaja != null) {
      if (ultimaCaja.getEstado() == EstadoCaja.ABIERTA) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_caja_anterior_abierta"));
      }
      if (Validator.compararDias(ultimaCaja.getFechaApertura(), caja.getFechaApertura()) >= 0) {
        throw new BusinessServiceException(
            RESOURCE_BUNDLE.getString("mensaje_fecha_apertura_no_valida"));
      }
    }
    // Duplicados
    if (cajaRepository.findById(caja.getId_Caja()) != null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_caja_duplicada"));
    }
  }

  @Override
  public void validarMovimiento(Date fechaMovimiento, long idEmpresa) {
    Caja caja = this.getUltimaCaja(idEmpresa);
    if (caja == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_caja_no_existente"));
    }
    if (caja.getEstado().equals(EstadoCaja.CERRADA)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_caja_cerrada"));
    }
    if (fechaMovimiento.before(caja.getFechaApertura())) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_caja_movimiento_fecha_no_valida"));
    }
  }

  @Override
  @Transactional
  public Caja abrirCaja(Empresa empresa, Usuario usuarioApertura, BigDecimal saldoApertura) {
    Caja caja = new Caja();
    caja.setEstado(EstadoCaja.ABIERTA);
    caja.setEmpresa(empresa);
    caja.setSaldoApertura(saldoApertura);
    caja.setUsuarioAbreCaja(usuarioApertura);
    caja.setFechaApertura(this.clockService.getFechaActual());
    this.validarCaja(caja);
    return cajaRepository.save(caja);
  }

  @Override
  public void actualizar(Caja caja) {
    cajaRepository.save(caja);
  }

  @Override
  @Transactional
  public void eliminar(Long idCaja) {
    Caja caja = this.getCajaPorId(idCaja);
    if (caja == null) {
      throw new EntityNotFoundException(RESOURCE_BUNDLE.getString("mensaje_caja_no_existente"));
    }
    caja.setEliminada(true);
    this.actualizar(caja);
  }

  @Override
  public Caja getUltimaCaja(long idEmpresa) {
    Pageable pageable = new PageRequest(0, 1);
    List<Caja> topCaja =
        cajaRepository
            .findTopByEmpresaAndEliminadaOrderByIdCajaDesc(idEmpresa, pageable)
            .getContent();
    return (topCaja.isEmpty()) ? null : topCaja.get(0);
  }

  @Override
  public Caja getCajaPorId(Long idCaja) {
    Caja caja = cajaRepository.findById(idCaja);
    if (caja == null) {
      throw new EntityNotFoundException(RESOURCE_BUNDLE.getString("mensaje_caja_no_existente"));
    }
    return caja;
  }

  @Override
  public Page<Caja> getCajasCriteria(BusquedaCajaCriteria criteria) {
    int pageNumber = 0;
    int pageSize = Integer.MAX_VALUE;
    Sort sorting = new Sort(Sort.Direction.DESC, "fechaApertura");
    if (criteria.getPageable() != null) {
      pageNumber = criteria.getPageable().getPageNumber();
      pageSize = criteria.getPageable().getPageSize();
      sorting = criteria.getPageable().getSort();
    }
    Pageable pageable = new PageRequest(pageNumber, pageSize, sorting);
    return cajaRepository.findAll(getBuilder(criteria), pageable);
  }

  private BooleanBuilder getBuilder(BusquedaCajaCriteria criteria) {
    // Fecha
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_caja_fechas_invalidas"));
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
    QCaja qcaja = QCaja.caja;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qcaja.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qcaja.eliminada.eq(false)));
    if (criteria.isBuscaPorUsuarioApertura() && !criteria.isBuscaPorUsuarioCierre()) {
      builder.and(qcaja.usuarioAbreCaja.id_Usuario.eq(criteria.getIdUsuarioApertura()));
    }
    if (criteria.isBuscaPorUsuarioCierre() && !criteria.isBuscaPorUsuarioApertura()) {
      builder.and(qcaja.usuarioCierraCaja.id_Usuario.eq(criteria.getIdUsuarioCierre()));
    }
    if (criteria.isBuscaPorUsuarioCierre() && criteria.isBuscaPorUsuarioApertura()) {
      builder.and(
          qcaja
              .usuarioAbreCaja
              .id_Usuario
              .eq(criteria.getIdUsuarioApertura())
              .and(qcaja.usuarioCierraCaja.id_Usuario.eq(criteria.getIdUsuarioCierre())));
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
      builder.and(qcaja.fechaApertura.between(fDesde, fHasta));
    }
    return builder;
  }

  @Override
  public Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling) {
    Caja cajaACerrar = this.getCajaPorId(idCaja);
    cajaACerrar.setSaldoReal(monto);
    if (scheduling) {
      LocalDateTime fechaCierre =
        LocalDateTime.ofInstant(cajaACerrar.getFechaApertura().toInstant(), ZoneId.systemDefault());
      fechaCierre = fechaCierre.withHour(23);
      fechaCierre = fechaCierre.withMinute(59);
      fechaCierre = fechaCierre.withSecond(59);
      cajaACerrar.setFechaCierre(Date.from(fechaCierre.atZone(ZoneId.systemDefault()).toInstant()));
    } else {
      cajaACerrar.setFechaCierre(this.clockService.getFechaActual());
    }
    if (idUsuario != null) {
      cajaACerrar.setUsuarioCierraCaja(usuarioService.getUsuarioPorId(idUsuario));
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
    List<Empresa> empresas = this.empresaService.getEmpresas();
    empresas
        .stream()
        .map(empresa -> this.getUltimaCaja(empresa.getId_Empresa()))
        .filter(
            ultimaCajaDeEmpresa ->
                ((ultimaCajaDeEmpresa != null)
                    && (ultimaCajaDeEmpresa.getEstado() == EstadoCaja.ABIERTA)))
        .forEachOrdered(
            ultimaCajaDeEmpresa -> {
              LocalDate fechaActual =
                  LocalDate.of(
                      LocalDate.now().getYear(),
                      LocalDate.now().getMonth(),
                      LocalDate.now().getDayOfMonth());
              Calendar fechaHoraCaja = new GregorianCalendar();
              fechaHoraCaja.setTime(ultimaCajaDeEmpresa.getFechaApertura());
              LocalDate fechaCaja =
                  LocalDate.of(
                      fechaHoraCaja.get(Calendar.YEAR),
                      fechaHoraCaja.get(Calendar.MONTH) + 1,
                      fechaHoraCaja.get(Calendar.DAY_OF_MONTH));
              if (fechaCaja.compareTo(fechaActual) < 0) {
                this.cerrarCaja(
                    ultimaCajaDeEmpresa.getId_Caja(),
                    this.getSaldoQueAfectaCaja(ultimaCajaDeEmpresa),
                    ultimaCajaDeEmpresa.getUsuarioAbreCaja().getId_Usuario(),
                    true);
              }
            });
  }

  @Override
  public BigDecimal getSaldoQueAfectaCaja(Caja caja) {
    Date fechaHasta = new Date();
    if (caja.getFechaCierre() != null) {
      fechaHasta = caja.getFechaCierre();
    }
    BigDecimal totalRecibosCliente =
        reciboService.getTotalRecibosClientesQueAfectanCajaEntreFechas(
            caja.getEmpresa().getId_Empresa(), caja.getFechaApertura(), fechaHasta);
    BigDecimal totalRecibosProveedor =
        reciboService.getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
            caja.getEmpresa().getId_Empresa(), caja.getFechaApertura(), fechaHasta);
    BigDecimal totalGastos =
        gastoService.getTotalGastosQueAfectanCajaEntreFechas(
            caja.getEmpresa().getId_Empresa(), caja.getFechaApertura(), fechaHasta);
    return caja.getSaldoApertura()
        .add(totalRecibosCliente)
        .subtract(totalRecibosProveedor)
        .subtract(totalGastos);
  }

  @Override
  public BigDecimal getSaldoSistema(Caja caja) {
    if (caja.getEstado().equals(EstadoCaja.ABIERTA)) {
      Date fechaHasta = new Date();
      if (caja.getFechaCierre() != null) {
        fechaHasta = caja.getFechaCierre();
      }
      BigDecimal totalRecibosCliente =
          reciboService.getTotalRecibosClientesEntreFechas(
              caja.getEmpresa().getId_Empresa(), caja.getFechaApertura(), fechaHasta);
      BigDecimal totalRecibosProveedor =
          reciboService.getTotalRecibosProveedoresEntreFechas(
              caja.getEmpresa().getId_Empresa(), caja.getFechaApertura(), fechaHasta);
      BigDecimal totalGastos =
          gastoService.getTotalGastosEntreFechas(
              caja.getEmpresa().getId_Empresa(), caja.getFechaApertura(), fechaHasta);
      return caja.getSaldoApertura()
          .add(totalRecibosCliente)
          .subtract(totalRecibosProveedor)
          .subtract(totalGastos);
    } else {
      return caja.getSaldoSistema();
    }
  }

  @Override
  public boolean isUltimaCajaAbierta(long idEmpresa) {
    Caja caja = cajaRepository.isUltimaCajaAbierta(idEmpresa);
    return (caja != null)
        && cajaRepository.isUltimaCajaAbierta(idEmpresa).getEstado().equals(EstadoCaja.ABIERTA);
  }

  private BigDecimal getTotalMovimientosPorFormaDePago(Caja caja, FormaDePago fdp) {
    Date fechaHasta = new Date();
    if (caja.getFechaCierre() != null) {
      fechaHasta = caja.getFechaCierre();
    }
    BigDecimal recibosTotal =
        reciboService
            .getTotalRecibosClientesEntreFechasPorFormaDePago(
                caja.getEmpresa().getId_Empresa(),
                fdp.getId_FormaDePago(),
                caja.getFechaApertura(),
                fechaHasta)
            .subtract(
                reciboService.getTotalRecibosProveedoresEntreFechasPorFormaDePago(
                    caja.getEmpresa().getId_Empresa(),
                    fdp.getId_FormaDePago(),
                    caja.getFechaApertura(),
                    fechaHasta));
    BigDecimal gastosTotal =
        gastoService.getTotalGastosEntreFechasYFormaDePago(
            caja.getEmpresa().getId_Empresa(),
            fdp.getId_FormaDePago(),
            caja.getFechaApertura(),
            fechaHasta);
    return recibosTotal.subtract(gastosTotal);
  }

  @Override
  public Map<Long, BigDecimal> getTotalesDeFormaDePago(long idCaja) {
    Caja caja = cajaRepository.findById(idCaja);
    Map<Long, BigDecimal> totalesPorFomaDePago = new HashMap<>();
    formaDePagoService
        .getFormasDePago(caja.getEmpresa())
        .forEach(
            fdp -> {
              BigDecimal total = this.getTotalMovimientosPorFormaDePago(caja, fdp);
              if (total.compareTo(BigDecimal.ZERO) != 0) {
                totalesPorFomaDePago.put(fdp.getId_FormaDePago(), total);
              }
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
      Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta) {
    List<MovimientoCaja> movimientos = new ArrayList<>();
    gastoService
        .getGastosEntreFechasYFormaDePago(empresa, formaDePago, desde, hasta)
        .forEach(gasto -> movimientos.add(new MovimientoCaja(gasto)));
    reciboService
        .getRecibosEntreFechasPorFormaDePago(desde, hasta, formaDePago, empresa)
        .forEach(recibo -> movimientos.add(new MovimientoCaja(recibo)));
    Collections.sort(movimientos);
    return movimientos;
  }

  @Override
  @Transactional
  public void reabrirCaja(long idCaja, BigDecimal saldoAperturaNuevo) {
    Caja caja = getCajaPorId(idCaja);
    Caja ultimaCaja = this.getUltimaCaja(caja.getEmpresa().getId_Empresa());
    if (ultimaCaja == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_caja_no_existente"));
    }
    if (caja.getId_Caja() == ultimaCaja.getId_Caja()) {
      caja.setSaldoSistema(null);
      caja.setSaldoApertura(saldoAperturaNuevo);
      caja.setSaldoReal(null);
      caja.setEstado(EstadoCaja.ABIERTA);
      caja.setUsuarioCierraCaja(null);
      caja.setFechaCierre(null);
      this.actualizar(caja);
    } else {
      throw new EntityNotFoundException(
          RESOURCE_BUNDLE.getString("mensaje_caja_re_apertura_no_valida"));
    }
  }

  @Override
  public Caja encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(
      long idEmpresa, Date fecha) {
    return cajaRepository.encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(
        idEmpresa, fecha);
  }

  @Override
  @Transactional
  public int actualizarSaldoSistema(Caja caja, BigDecimal monto) {
    return cajaRepository.actualizarSaldoSistema(caja.getId_Caja(), monto);
  }
}
