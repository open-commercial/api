package sic.modelo.dto;

import java.io.Serializable;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true,  exclude = {"razonSocialProveedor", "idProveedor"})
@Builder
public class FacturaCompraDTO extends FacturaDTO implements Serializable {

    private Long idProveedor;
    private String razonSocialProveedor;
    
}
