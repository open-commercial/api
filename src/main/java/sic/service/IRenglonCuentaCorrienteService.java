package sic.service;

import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import sic.modelo.AjusteCuentaCorriente;
import sic.modelo.CuentaCorriente;
import sic.modelo.Factura;
import sic.modelo.Nota;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;

public interface IRenglonCuentaCorrienteService {
    
    RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura f, boolean eliminado);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota n, boolean eliminado);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo r, boolean eliminado);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeAjusteCuentaCorriente(AjusteCuentaCorriente ajusteCC, boolean eliminado);
    
    Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(CuentaCorriente cuentaCorriente, boolean eliminado, Pageable page);
    
    Slice<RenglonCuentaCorriente> getRenglonesFacturasYNotaDebitoCuentaCorriente(long idCuentaCorriente, Pageable page);
    
    Double getSaldoCuentaCorriente(long idCuentaCorriente);
    
    Date getFechaUltimoMovimiento(long idCuentaCorriente);
    
}
