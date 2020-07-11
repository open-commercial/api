package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RenglonReporteTraspasoDTO {

    private LocalDateTime fecha;
    private String codigoAndDescripcion;
    private String sucursalOrigen;
    private String sucursalDestino;
    private BigDecimal cantidad;
}
