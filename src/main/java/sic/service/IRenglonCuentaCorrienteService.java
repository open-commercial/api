package sic.service;

import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.RenglonCuentaCorriente;

public interface IRenglonCuentaCorrienteService {
    
    RenglonCuentaCorriente asentarRenglonCuentaCorriente(RenglonCuentaCorriente renglonCuentaCorriente);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(FacturaVenta fv, boolean eliminado);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota n, boolean eliminado);
    
    RenglonCuentaCorriente getRenglonCuentaCorrienteDePago(Pago p, boolean eliminado);
    
}
