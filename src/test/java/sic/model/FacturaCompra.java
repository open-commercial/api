package sic.model;

import java.io.Serializable;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true,  exclude = {"razonSocialProveedor", "idProveedor"})
@Builder
public class FacturaCompra extends Factura implements Serializable {

    private Long idProveedor;
    private String razonSocialProveedor;
    
}
