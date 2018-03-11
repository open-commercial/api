package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sic.modelo.Proveedor;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotaDebitoProveedorDTO extends NotaDebitoDTO implements Serializable {
    
    private Proveedor proveedor;
    
}
