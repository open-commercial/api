package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = "id_Transportista")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransportistaDTO {

  private long id_Transportista;
  private String nombre;
  private UbicacionDTO ubicacion;
  private String web;
  private String telefono;
  private boolean eliminado;
}
