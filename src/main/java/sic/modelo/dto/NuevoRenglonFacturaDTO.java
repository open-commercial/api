package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class NuevoRenglonFacturaDTO {
    private long idProducto;
    private BigDecimal cantidad;
    private BigDecimal bonificacion;
    private boolean renglonMarcado;
}
