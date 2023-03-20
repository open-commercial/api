package sic.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
@Builder
public class NuevoRenglonFacturaDTO {

  private long idProducto;

  @Positive(message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  @PositiveOrZero(message = "{mensaje_renglon_bonificacion_porcentaje_negativa}")
  private BigDecimal bonificacion;

  private boolean renglonMarcado;
}
