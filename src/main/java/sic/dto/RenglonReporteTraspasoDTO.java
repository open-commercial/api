package sic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RenglonReporteTraspasoDTO {

    private String codigo;
    private String descripcion;
    private String sucursalOrigen;
    private String sucursalDestino;
    private BigDecimal cantidad;
    private String medida;
}
