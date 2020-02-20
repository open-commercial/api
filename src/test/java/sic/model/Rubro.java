package sic.model;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"idRubro"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rubro {

  private long idRubro;
  private String nombre;
  private boolean eliminado;
}
