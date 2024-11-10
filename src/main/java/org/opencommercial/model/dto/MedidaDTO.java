package org.opencommercial.model.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "idMedida")
@Builder
public class MedidaDTO {

  private long idMedida;
  private String nombre;
  private boolean eliminada;
}
