package org.opencommercial.model.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "idSucursal")
@Builder
public class CantidadEnSucursalDTO implements Serializable {

  private Long idCantidadEnSucursal;
  private BigDecimal cantidad;
  private Long idSucursal;
  private String nombreSucursal;
}
