package sic.modelo.dto;

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
  private String estanteria;
  private String estante;
  private Long idSucursal;
  private String nombreSucursal;
}
