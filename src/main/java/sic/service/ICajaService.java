package sic.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaCajaCriteria;

import javax.validation.Valid;

public interface ICajaService {

    void actualizar(@Valid Caja caja);

    void validarMovimiento(LocalDateTime fechaMovimiento, long idEmpresa);
    
    void eliminar(Long idCaja);
    
    Caja getCajaPorId(Long id);

    Page<Caja> buscarCajas(BusquedaCajaCriteria criteria);

    Map<Long, BigDecimal> getTotalesDeFormaDePago(long idCaja);

    Caja getUltimaCaja(long idEmpresa);

    Caja abrirCaja(Empresa empresa, Usuario usuarioApertura, BigDecimal saldoApertura);

    void validarOperacion(@Valid Caja caja);
    
    Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling);
    
    BigDecimal getSaldoQueAfectaCaja(Caja caja);

    BigDecimal getSaldoSistema(Caja caja);

    boolean isUltimaCajaAbierta(long idEmpresa);
    
    BigDecimal getSaldoSistemaCajas(BusquedaCajaCriteria criteria);
    
    BigDecimal getSaldoRealCajas(BusquedaCajaCriteria criteria);
    
    List<MovimientoCaja> getMovimientosPorFormaDePagoEntreFechas(Empresa empresa, FormaDePago formaDePago, LocalDateTime desde, LocalDateTime hasta);

    void reabrirCaja(long idCaja, BigDecimal saldoInicial);

    Caja encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(long idEmpresa, LocalDateTime fecha);

    int actualizarSaldoSistema(Caja caja, BigDecimal monto);

}
