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
  private String nombreSucursal;
  private boolean eliminado;
}
