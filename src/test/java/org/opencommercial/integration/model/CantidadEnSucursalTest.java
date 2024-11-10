package org.opencommercial.integration.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "idSucursal")
@Builder
public class CantidadEnSucursalTest {

  private Long idCantidadEnSucursal;
  private BigDecimal cantidad;
  private Long idSucursal;
  private String nombreSucursal;
}
