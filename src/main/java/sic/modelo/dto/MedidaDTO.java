package sic.modelo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "id_Medida")
@Builder
public class MedidaDTO {

  private long id_Medida;
  private String nombre;
  private boolean eliminada;
}
