package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.FacturaCompra;
import sic.modelo.Proveedor;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaCreditoProveedorDTO extends NotaCreditoDTO implements Serializable {
    
    private Proveedor proveedor;
    
    private FacturaCompra facturaCompra;
    
}
