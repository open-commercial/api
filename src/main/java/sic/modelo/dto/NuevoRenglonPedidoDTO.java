package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoRenglonPedidoDTO {

  private long idProductoItem;
  private BigDecimal cantidad;
  private long idCliente;
}
