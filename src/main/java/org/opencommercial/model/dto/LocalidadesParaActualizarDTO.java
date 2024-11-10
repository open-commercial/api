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
public class LocalidadesParaActualizarDTO {

    private long[] idLocalidad;
    private BigDecimal costoDeEnvio;
    private Boolean envioGratuito;
}
