package org.opencommercial.integration.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"renglonesNotaCredito"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotaCreditoTest extends NotaTest {

    private boolean modificaStock;
    private List<RenglonNotaCreditoTest> renglonesNotaCredito;
    private BigDecimal subTotal;
    private BigDecimal recargoPorcentaje;
    private BigDecimal recargoNeto;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoNeto;

}
