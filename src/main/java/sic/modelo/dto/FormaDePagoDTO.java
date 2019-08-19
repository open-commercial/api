package sic.modelo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id_FormaDePago", "idSucursal", "nombreSucursal"})
@Builder
public class FormaDePagoDTO {

  private long id_FormaDePago;
  private String nombre;
  private boolean afectaCaja;
  private boolean predeterminado;
  private Long idSucursal;
  private String nombreSucursal;
  private boolean eliminada;
}
