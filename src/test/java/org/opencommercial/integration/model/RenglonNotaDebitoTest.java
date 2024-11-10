package org.opencommercial.integration.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RenglonNotaDebitoTest {

    private long idRenglonNotaDebito;
    private String descripcion;
    private BigDecimal monto;
    private BigDecimal importeBruto;
    private BigDecimal ivaPorcentaje;
    private BigDecimal ivaNeto;
    private BigDecimal importeNeto;
}
