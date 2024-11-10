package org.opencommercial.integration.model;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"idRubro"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RubroTest {

  private long idRubro;
  private String nombre;
  private boolean eliminado;
  private String imagenHtml;
}
