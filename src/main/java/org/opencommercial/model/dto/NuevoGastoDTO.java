package org.opencommercial.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(exclude = {"idSucursal", "idFormaDePago"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NuevoGastoDTO {

  private String concepto;
  private Long idSucursal;
  private Long idFormaDePago;
  private BigDecimal monto;
}
