package sic.modelo.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarritoCompraDTO implements Serializable {
  private BigDecimal cantArticulos;
  private long cantRenglones;
  private BigDecimal subtotal;
  private BigDecimal bonificacionPorcentaje;
  private BigDecimal bonificacionNeto;
  private BigDecimal total;
}
