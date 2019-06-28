package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"id_Rubro"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RubroDTO {

  private long id_Rubro;
  private String nombre;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminado;
}
