package sic.model;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "idSucursal")
@Builder
public class CantidadEnSucursal {

  private Long idCantidadEnSucursal;
  private BigDecimal cantidad;
  private Long idSucursal;
  private String nombreSucursal;
}
