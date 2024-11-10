package org.opencommercial.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"idFormaDePago", "idSucursal", "nombreSucursal"})
@Builder
public class FormaDePagoDTO {

  private long idFormaDePago;
  private String nombre;
  private boolean afectaCaja;
  private boolean predeterminado;
  private Long idSucursal;
  private String nombreSucursal;
  private boolean eliminada;
}
