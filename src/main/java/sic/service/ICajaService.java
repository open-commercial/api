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
    
    BigDecimal getTotalQueAfectaCaja(Caja caja);
    
    BigDecimal getSaldoSistemaCajas(long idEmpresa, Long idUsuario, Date desde, Date hasta);
    
    BigDecimal getSaldoRealCajas(long idEmpresa, Long idUsuario, Date desde, Date hasta);
    
    List<MovimientoCaja> getMovimientosPorFormaDePagoEntreFechas(Empresa empresa, FormaDePago formaDePago, Date desde, Date hasta);

    void actualizarSaldoSistema(Recibo recibo, TipoDeOperacion tipoDeOperacion);

    void actualizarSaldoSistema(Gasto gasto, TipoDeOperacion tipoDeOperacion);
}
