package sic.modelo.calculos;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Comprador.class)
public class Resultados {

  private BigDecimal subTotal;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal descuento;
  private BigDecimal recargoPorcentaje;
  private BigDecimal recargo;
  private BigDecimal subTotalBruto;
  private BigDecimal iva105;
  private BigDecimal iva21;
  private BigDecimal total;
}
