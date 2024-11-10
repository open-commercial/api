package org.opencommercial.integration.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idProductoItem", "codigoItem"})
@Builder
public class RenglonNotaCreditoTest {

    private long idRenglonNota;
    private long idProductoItem;
    private String codigoItem;
    private String descripcionItem;
    private String medidaItem;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoNeto;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal ivaPorcentaje;
    private BigDecimal ivaNeto;
    private BigDecimal importe; //sin nada
    private BigDecimal importeBruto;  //con descuentos y recargos, sin iva
    private BigDecimal importeNeto; //con descuentos, recargos y con iva
}
