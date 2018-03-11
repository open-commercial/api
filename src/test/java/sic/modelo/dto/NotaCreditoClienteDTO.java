package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.Cliente;
import sic.modelo.FacturaVenta;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaCreditoClienteDTO extends NotaCreditoDTO implements Serializable {
    
    private Cliente cliente;
    
    private FacturaVenta facturaVenta;
    
}
