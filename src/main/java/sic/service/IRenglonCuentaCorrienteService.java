package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.CuentaCorriente;
import sic.modelo.Factura;
import sic.modelo.Nota;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;

public interface IRenglonCuentaCorrienteService {
    
    RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura factura, boolean eliminado);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo recibo, boolean eliminado);
    
    Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(CuentaCorriente cuentaCorriente, boolean eliminado, Pageable page);
    
    BigDecimal getSaldoCuentaCorriente(long idCuentaCorriente);
    
    Date getFechaUltimoMovimiento(long idCuentaCorriente);
    
}
