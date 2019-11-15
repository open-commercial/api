package sic.modelo.calculos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.dto.RenglonPedidoDTO;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevosResultadosPedido {

  private List<RenglonPedidoDTO> renglones;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal recargoPorcentaje;
}
