package sic.modelo.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class FacturaCompraDTO extends FacturaDTO implements Serializable {

    private Long idProveedor;

    private String razonSocialProveedor;
    
}
