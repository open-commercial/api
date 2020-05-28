package sic.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RenglonNotaDebito {

    private long idRenglonNotaDebito;
    private String descripcion;
    private BigDecimal monto;
    private BigDecimal importeBruto;
    private BigDecimal ivaPorcentaje;
    private BigDecimal ivaNeto;
    private BigDecimal importeNeto;
}
