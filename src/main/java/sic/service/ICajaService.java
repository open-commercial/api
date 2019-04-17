package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;

import javax.validation.Valid;

public interface ICajaService {

    void actualizar(Caja caja);

    void validarMovimiento(Date fechaMovimiento, long idEmpresa);
    
    void eliminar(Long idCaja);
    
    Caja getCajaPorId(Long id);

    Page<Caja> getCajasCriteria(BusquedaCajaCriteria criteria);

    Map<Long, BigDecimal> getTotalesDeFormaDePago(long idCaja);

    Caja getUltimaCaja(long idEmpresa);

    Caja abrirCaja(Empresa empresa, Usuario usuarioApertura, BigDecimal saldoApertura);

    void validarCaja(@Valid Caja caja);
    
    Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling);
    
    BigDecimal getSaldoQueAfectaCaja(Caja caja);

    BigDecimal getSaldoSistema(Caja caja);

    boolean isUltimaCajaAbierta(long idEmpresa);
    
    BigDecimal getSaldoSistemaCajas(BusquedaCajaCriteria criteria);
    
    BigDecimal getSaldoRealCajas(BusquedaCajaCriteria criteria);
    
    List<MovimientoCaja> getMovimientosPorFormaDePagoEntreFechas(Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta);

    void reabrirCaja(long idCaja, BigDecimal saldoInicial);

    Caja encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(long idEmpresa, Date fecha);

    int actualizarSaldoSistema(Caja caja, BigDecimal monto);

}
