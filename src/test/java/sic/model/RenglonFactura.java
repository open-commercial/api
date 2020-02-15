package sic.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idProductoItem", "codigoItem"})
@Builder
public class RenglonFactura {

  private long idRenglonFactura;
  private long idProductoItem;
  private String codigoItem;
  private String descripcionItem;
  private String medidaItem;
  private BigDecimal cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal bonificacionPorcentaje;
  private BigDecimal bonificacionNeta;
  private BigDecimal ivaPorcentaje;
  private BigDecimal ivaNeto;
  private BigDecimal gananciaPorcentaje;
  private BigDecimal gananciaNeto;
  private BigDecimal importe;
}
