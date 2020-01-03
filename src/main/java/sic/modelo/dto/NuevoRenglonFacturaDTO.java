package sic.modelo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NuevoRenglonFacturaDTO {
    private long idProducto;
    private BigDecimal cantidad;
    private BigDecimal bonificacion;
}
