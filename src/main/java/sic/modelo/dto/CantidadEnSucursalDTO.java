package sic.modelo.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idCantidadSucursal", "idSucursal"})
@Builder
public class CantidadEnSucursalDTO {

  private Long idCantidadSucursal;
  private BigDecimal cantidad;
  private String estanteria;
  private String estante;
  private Long idSucursal;
  private String nombreSucursal;
}
