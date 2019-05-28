package sic.modelo.dto;

import java.io.Serializable;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true,  exclude = {"razonSocialProveedor", "idProveedor"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FacturaCompraDTO extends FacturaDTO implements Serializable {

    private Long idProveedor;
    private String razonSocialProveedor;
    
}
