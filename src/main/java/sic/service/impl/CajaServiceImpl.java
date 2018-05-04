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
import sic.service.ICajaService;

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
import sic.service.BusinessServiceException;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;
import sic.service.IGastoService;
import sic.service.IUsuarioService;
import sic.util.FormatterFechaHora;
import sic.util.Validator;
import sic.repository.CajaRepository;
import sic.service.IReciboService;

@Service
public class CajaServiceImpl implements ICajaService {

    private final CajaRepository cajaRepository;
    private final IFormaDePagoService formaDePagoService;
    private final IGastoService gastoService;
    private final IEmpresaService empresaService;
    private final IUsuarioService usuarioService;
    private final IReciboService reciboService;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public CajaServiceImpl(CajaRepository cajaRepository, IFormaDePagoService formaDePagoService, IGastoService gastoService,
                           IEmpresaService empresaService, IUsuarioService usuarioService,
                           IReciboService reciboService) {
        this.cajaRepository = cajaRepository;
        this.formaDePagoService = formaDePagoService;
        this.gastoService = gastoService;
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
        this.reciboService = reciboService;
    }

    @Override
    public void validarCaja(Caja caja) {
        //Entrada de Datos
        //Requeridos
        if (caja.getFechaApertura() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_fecha_vacia"));
        }
        if (caja.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_empresa_vacia"));
        }
        if (caja.getUsuarioAbreCaja() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_usuario_vacio"));
        }
        //Administrador
        if (!usuarioService.getUsuariosPorRol(Rol.ADMINISTRADOR).contains(caja.getUsuarioAbreCaja())) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_usuario_no_administrador"));
        }
        //Una Caja por dia
        Caja ultimaCaja = this.getUltimaCaja(caja.getEmpresa().getId_Empresa());
        if (ultimaCaja != null && ultimaCaja.getEstado() == EstadoCaja.ABIERTA) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_anterior_abierta"));
        }
        if (ultimaCaja != null && Validator.compararDias(ultimaCaja.getFechaApertura(), caja.getFechaApertura()) >= 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_fecha_apertura_no_valida"));
        }
        //Duplicados        
        if (cajaRepository.findById(caja.getId_Caja()) != null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_duplicada"));
        }
    }

    @Override
    @Transactional
    public Caja guardar(Caja caja) {
        caja.setFechaApertura(new Date());
        this.validarCaja(caja);
        caja.setNroCaja(this.getUltimoNumeroDeCaja(caja.getEmpresa().getId_Empresa()) + 1);
        caja = cajaRepository.save(caja);
        LOGGER.warn("La Caja " + caja + " se guardó correctamente.");
        return caja;
    }

    @Override
    @Transactional
    public void actualizar(Caja caja) {
        cajaRepository.save(caja);
    }

    @Override
    @Transactional
    public void eliminar(Long idCaja) {
        Caja caja = this.getCajaPorId(idCaja);
        if (caja == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_no_existente"));
        }
        caja.setEliminada(true);
        this.actualizar(caja);
    }

    @Override
    public Caja getUltimaCaja(long id_Empresa) {
        return cajaRepository.findTopByEmpresaAndEliminadaOrderByFechaAperturaDesc(empresaService.getEmpresaPorId(id_Empresa), false);
    }

    @Override
    public Caja getCajaPorId(Long idCaja) {
        Caja caja = cajaRepository.findById(idCaja);
        if (caja == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_no_existente"));
        }
        return caja;
    }

    @Override
    public int getUltimoNumeroDeCaja(long idEmpresa) {
        Caja caja = this.getUltimaCaja(idEmpresa);
        if (caja == null) {
            return 0;
        } else {
            return caja.getNroCaja();
        }
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
        //Empresa
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        //Fecha
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_caja_fechas_invalidas"));
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
        if (criteria.getEmpresa() == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_empresa_no_existente"));
        }
        QCaja qcaja = QCaja.caja;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qcaja.empresa.eq(criteria.getEmpresa()).and(qcaja.eliminada.eq(false)));
        if (criteria.isBuscaPorUsuarioApertura() && !criteria.isBuscaPorUsuarioCierre()) {
            builder.and(qcaja.usuarioAbreCaja.eq(criteria.getUsuarioApertura()));
        }
        if (criteria.isBuscaPorUsuarioCierre() && !criteria.isBuscaPorUsuarioApertura()) {
            builder.and(qcaja.usuarioCierraCaja.eq(criteria.getUsuarioCierre()));
        }

        if (criteria.isBuscaPorUsuarioCierre() && criteria.isBuscaPorUsuarioApertura()) {
            builder.and(qcaja.usuarioAbreCaja.eq(criteria.getUsuarioApertura()).and(qcaja.usuarioCierraCaja.eq(criteria.getUsuarioCierre())));
        }
        if (criteria.isBuscaPorFecha()) {
            FormatterFechaHora formateadorFecha = new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
            DateExpression<Date> fDesde = Expressions.dateTemplate(Date.class, "convert({0}, datetime)", formateadorFecha.format(criteria.getFechaDesde()));
            DateExpression<Date> fHasta = Expressions.dateTemplate(Date.class, "convert({0}, datetime)", formateadorFecha.format(criteria.getFechaHasta()));
            builder.and(qcaja.fechaApertura.between(fDesde, fHasta));
        }
        return builder;
    }

    @Override
    @Transactional
    public Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling) {
        Caja cajaACerrar = this.getCajaPorId(idCaja);
        cajaACerrar.setSaldoReal(monto);
        if (scheduling) {
            LocalDateTime fechaCierre = LocalDateTime.ofInstant(cajaACerrar.getFechaApertura().toInstant(), ZoneId.systemDefault());
            fechaCierre = fechaCierre.withHour(23);
            fechaCierre = fechaCierre.withMinute(59);
            fechaCierre = fechaCierre.withSecond(59);
            cajaACerrar.setFechaCierre(Date.from(fechaCierre.atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            cajaACerrar.setFechaCierre(new Date());
        }
        if (idUsuario != null) {
            cajaACerrar.setUsuarioCierraCaja(usuarioService.getUsuarioPorId(idUsuario));
        }
        cajaACerrar.setEstado(EstadoCaja.CERRADA);
        this.actualizar(cajaACerrar);
        LOGGER.warn("La Caja " + cajaACerrar + " se cerró correctamente.");
        return cajaACerrar;
    }

    @Scheduled(cron = "30 0 0 * * *") // Todos los dias a las 00:00:30
    public void cerrarCajas() {
        LOGGER.warn("Cierre automático de Cajas." + LocalDateTime.now());
        List<Empresa> empresas = this.empresaService.getEmpresas();
        empresas.stream().map((empresa) -> this.getUltimaCaja(empresa.getId_Empresa())).filter((ultimaCajaDeEmpresa)
                -> ((ultimaCajaDeEmpresa != null) && (ultimaCajaDeEmpresa.getEstado() == EstadoCaja.ABIERTA))).forEachOrdered((ultimaCajaDeEmpresa) -> {
            LocalDate fechaActual = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth());
            Calendar fechaHoraCaja = new GregorianCalendar();
            fechaHoraCaja.setTime(ultimaCajaDeEmpresa.getFechaApertura());
            LocalDate fechaCaja = LocalDate.of(fechaHoraCaja.get(Calendar.YEAR), fechaHoraCaja.get(Calendar.MONTH) + 1, fechaHoraCaja.get(Calendar.DAY_OF_MONTH));
            if (fechaCaja.compareTo(fechaActual) < 0) {
                this.cerrarCaja(ultimaCajaDeEmpresa.getId_Caja(), this.getTotalQueAfectaCaja(ultimaCajaDeEmpresa), ultimaCajaDeEmpresa.getUsuarioAbreCaja().getId_Usuario(), true);
            }
        });
    }

    @Override
    public BigDecimal getTotalQueAfectaCaja(Caja caja) {
        LocalDateTime ldt = caja.getFechaApertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (caja.getFechaCierre() == null) {
            ldt = ldt.withHour(23);
            ldt = ldt.withMinute(59);
            ldt = ldt.withSecond(59);
        } else {
            ldt = caja.getFechaCierre().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        BigDecimal totalRecibosCliente = reciboService.getTotalRecibosClientesEntreFechas(caja.getEmpresa().getId_Empresa(),
                caja.getFechaApertura(), Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        BigDecimal totalRecibosProveedor = reciboService.getTotalRecibosProveedoresEntreFechas(caja.getEmpresa().getId_Empresa(),
                caja.getFechaApertura(), Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        BigDecimal totalGastos = gastoService.getTotalGastosEntreFechas(caja.getEmpresa().getId_Empresa(),
                caja.getFechaApertura(), Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        return caja.getSaldoInicial().add(totalRecibosCliente).subtract(totalRecibosProveedor).subtract(totalGastos);
    }

    @Override
    public boolean isUltimaCajaAbierta(long idEmpresa) {
        Caja caja = cajaRepository.isUltimaCajaAbierta(idEmpresa);
        return (caja != null) && cajaRepository.isUltimaCajaAbierta(idEmpresa).getEstado().equals(EstadoCaja.ABIERTA);
    }

    private BigDecimal getTotalMovimientosPorFormaDePago(Caja caja, FormaDePago fdp) {
        LocalDateTime ldt = caja.getFechaApertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (caja.getFechaCierre() == null) {
            ldt = ldt.withHour(23);
            ldt = ldt.withMinute(59);
            ldt = ldt.withSecond(59);
        } else {
            ldt = caja.getFechaCierre().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        BigDecimal recibosTotal = reciboService.getTotalRecibosClientesEntreFechasPorFormaDePago(caja.getEmpresa().getId_Empresa(),
                fdp.getId_FormaDePago(), caja.getFechaApertura(), Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()))
                .subtract(reciboService.getTotalRecibosProveedoresEntreFechasPorFormaDePago(caja.getEmpresa().getId_Empresa(),
                        fdp.getId_FormaDePago(), caja.getFechaApertura(), Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())));
        BigDecimal gastosTotal = gastoService.getTotalGastosEntreFechasYFormaDePago(caja.getEmpresa().getId_Empresa(), fdp.getId_FormaDePago(),
                caja.getFechaApertura(), Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
        return recibosTotal.subtract(gastosTotal);
    }

    @Override
    public Map<Long, BigDecimal> getTotalesDeFormaDePago(long idCaja) {
        Caja caja = cajaRepository.findById(idCaja);
        Map<Long, BigDecimal> totalesPorFomaDePago = new HashMap<>();
        formaDePagoService.getFormasDePago(caja.getEmpresa()).forEach(fdp -> {
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
    public List<MovimientoCaja> getMovimientosPorFormaDePagoEntreFechas(Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta) {
        List<MovimientoCaja> movimientos = new ArrayList<>();
        gastoService.getGastosEntreFechasYFormaDePago(empresa, formaDePago, desde, hasta).forEach(gasto -> movimientos.add(new MovimientoCaja(gasto)));
        reciboService.getRecibosEntreFechasPorFormaDePago(desde, hasta, formaDePago, empresa).forEach(recibo -> movimientos.add(new MovimientoCaja(recibo)));
        Collections.sort(movimientos);
        return movimientos;
    }

    @Override
    public void actualizarSaldoSistema(Recibo recibo, TipoDeOperacion tipoDeOperacion) {
        Caja caja = this.getUltimaCaja(recibo.getEmpresa().getId_Empresa());
        if (caja != null && caja.getEstado().equals(EstadoCaja.ABIERTA)) {
            BigDecimal monto = BigDecimal.ZERO;
            if (tipoDeOperacion.equals(TipoDeOperacion.ALTA)) {
                if (recibo.getCliente() != null) {
                    monto = recibo.getMonto();
                } else if (recibo.getProveedor() != null) {
                    monto = recibo.getMonto().negate();
                }
            } else if (tipoDeOperacion.equals(TipoDeOperacion.ELIMINACION)) {
                if (recibo.getCliente() != null) {
                    monto = recibo.getMonto().negate();
                } else if (recibo.getProveedor() != null) {
                    monto = recibo.getMonto();
                }
            }
            cajaRepository.actualizarSaldoSistema(caja.getId_Caja(), monto);
        }
    }

    @Override
    public void actualizarSaldoSistema(Gasto gasto, TipoDeOperacion tipoDeOperacion) {
        Caja caja = this.getUltimaCaja(gasto.getEmpresa().getId_Empresa());
        if (caja != null) {
            BigDecimal monto = BigDecimal.ZERO;
            if (tipoDeOperacion.equals(TipoDeOperacion.ALTA)) {
                monto = gasto.getMonto().negate();
            } else if (tipoDeOperacion.equals(TipoDeOperacion.ELIMINACION)) {
                monto = gasto.getMonto();
            }
            cajaRepository.actualizarSaldoSistema(caja.getId_Caja(), monto);
        }
    }

    @Override
    @Transactional
    public void reabrirCaja(long idCaja, BigDecimal saldoInicialNuevo, long idUsuario) {
        Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
        if (usuario.getRoles().contains(Rol.ADMINISTRADOR)) {
            Caja caja = getCajaPorId(idCaja);
            if (caja.getFechaApertura().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(LocalDate.now())) {
                caja.setUsuarioCierraCaja(null);
                BigDecimal diferenciaSaldosIniciales = caja.getSaldoInicial().subtract(saldoInicialNuevo);
                if (diferenciaSaldosIniciales.signum() == -1) {
                    caja.setSaldoSistema(caja.getSaldoSistema().add(diferenciaSaldosIniciales.abs()));
                } else if (diferenciaSaldosIniciales.signum() == 1) {
                    caja.setSaldoSistema(caja.getSaldoSistema().subtract(diferenciaSaldosIniciales));
                }
                caja.setSaldoInicial(saldoInicialNuevo);
                caja.setSaldoReal(BigDecimal.ZERO);
                caja.setEstado(EstadoCaja.ABIERTA);
                this.actualizar(caja);
            } else {
                throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_caja_re_apertura_no_valida"));
            }
        } else {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_rol_no_valido"));
        }
    }

}
