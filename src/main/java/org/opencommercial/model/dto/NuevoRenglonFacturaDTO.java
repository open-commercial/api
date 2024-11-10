package org.opencommercial.model.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
