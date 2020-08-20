package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"idRubro"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RubroDTO {

  private long idRubro;
  private String nombre;
  private boolean eliminado;
  private String imagenHtml;
}
