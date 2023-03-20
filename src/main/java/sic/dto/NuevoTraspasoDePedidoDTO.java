package sic.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoTraspasoDePedidoDTO {

  private Map<Long, BigDecimal> idProductoConCantidad;
  private Long nroPedido;
  private Long idSucursalOrigen;
  private Long idSucursalDestino;
  private Long idUsuario;
}
