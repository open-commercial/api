package org.opencommercial.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonView(Views.Comprador.class)
public class CarritoCompraDTO implements Serializable {
  private BigDecimal cantArticulos;
  private long cantRenglones;
  private BigDecimal total;
}
