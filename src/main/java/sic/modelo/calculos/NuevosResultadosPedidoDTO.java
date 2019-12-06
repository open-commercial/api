package sic.modelo.calculos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevosResultadosPedidoDTO {

  private List<BigDecimal> importes;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal recargoPorcentaje;
}
