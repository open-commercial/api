package sic.modelo.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FacturaCompraDTO extends FacturaDTO implements Serializable {
    
    private String razonSocialProveedor = "Little Finger";
    
}
