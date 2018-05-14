package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;

public interface ICajaService {

    void actualizar(Caja caja);
    
    void eliminar(Long idCaja);
    
    Caja getCajaPorId(Long id);

    Page<Caja> getCajasCriteria(BusquedaCajaCriteria criteria);

    Map<Long, BigDecimal> getTotalesDeFormaDePago(long idCaja);

    Caja getUltimaCaja(long id_Empresa);

    int getUltimoNumeroDeCaja(long id_Empresa);

    Caja guardar(Caja caja);

    void validarCaja(Caja caja);
    
    Caja cerrarCaja(long idCaja, BigDecimal monto, Long idUsuario, boolean scheduling);
    
    BigDecimal getSaldoQueAfectaCaja(Caja caja);

    BigDecimal getSaldoSistema(Caja caja);

    boolean isUltimaCajaAbierta(long idEmpresa);
    
    BigDecimal getSaldoSistemaCajas(BusquedaCajaCriteria criteria);
    
    BigDecimal getSaldoRealCajas(BusquedaCajaCriteria criteria);
    
    List<MovimientoCaja> getMovimientosPorFormaDePagoEntreFechas(Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta);

    void reabrirCaja(long idCaja, BigDecimal saldoInicial, long idUsuario);

}
