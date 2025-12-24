package org.opencommercial.model.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NuevoRenglonFacturaDTO {

  private long idProducto;

  @Positive(message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  @PositiveOrZero(message = "{mensaje_renglon_bonificacion_porcentaje_negativa}")
  private BigDecimal bonificacion;

  private boolean renglonMarcado;
}
