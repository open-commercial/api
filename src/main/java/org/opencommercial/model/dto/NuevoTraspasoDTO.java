package org.opencommercial.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoTraspasoDTO {
    
    private Long[] idProducto;
    private BigDecimal[] cantidad;
    private Long idSucursalOrigen;
    private Long idSucursalDestino;
}
