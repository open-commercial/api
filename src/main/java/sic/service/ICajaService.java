package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaCajaCriteria;

import javax.validation.Valid;

public interface ICajaService {

    void actualizar(@Valid Caja caja);

    void validarMovimiento(Date fechaMovimiento, long idSucursal);
    
    void eliminar(Long idCaja);
    
    Caja getCajaPorId(Long id);

    Page<Caja> buscarCajas(BusquedaCajaCriteria criteria);

    Map<Long, BigDecimal> getTotalesDeFormaDePago(long idCaja);

    Caja getUltimaCaja(long idSucursal);

    Caja abrirCaja(Sucursal sucursal, Usuario usuarioApertura, BigDecimal saldoApertura);

    void validarOperacion(@Valid Caja caja);
    
    Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling);
    
    BigDecimal getSaldoQueAfectaCaja(Caja caja);

    BigDecimal getSaldoSistema(Caja caja);

    boolean isUltimaCajaAbierta(long idSucursal);
    
    BigDecimal getSaldoSistemaCajas(BusquedaCajaCriteria criteria);
    
    BigDecimal getSaldoRealCajas(BusquedaCajaCriteria criteria);
    
    List<MovimientoCaja> getMovimientosPorFormaDePagoEntreFechas(Sucursal sucursal, FormaDePago formaDePago, Date desde, Date hasta);

    void reabrirCaja(long idCaja, BigDecimal saldoInicial);

    Caja encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(long idSucursal, Date fecha);

    int actualizarSaldoSistema(Caja caja, BigDecimal monto);

}
