package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoTraspasoDTO {

    private Map<Long, BigDecimal> idProductoConCantidad;
    private Long idSucursalOrigen;
    private Long idSucursalDestino;
}
