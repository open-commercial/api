package sic.modelo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoRenglonPedidoDTO {

  private long idProductoItem;
  
  @Positive(message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;
}
