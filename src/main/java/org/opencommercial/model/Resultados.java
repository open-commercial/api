package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opencommercial.config.Views;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Comprador.class)
public class Resultados {

  private BigDecimal subTotal;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal descuentoNeto;
  private BigDecimal recargoPorcentaje;
  private BigDecimal recargoNeto;
  private BigDecimal subTotalBruto;
  private BigDecimal iva105Neto;
  private BigDecimal iva21Neto;
  private BigDecimal total;
}
