package sic.modelo.calculos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.modelo.dto.RenglonPedidoDTO;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NuevosResultadosPedido {

  private List<RenglonPedidoDTO> renglones;
  private BigDecimal descuentoPorcentaje;
  private BigDecimal recargoPorcentaje;
}
