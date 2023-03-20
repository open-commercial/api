package sic.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = "idTransportista")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransportistaDTO {

  private long idTransportista;
  private String nombre;
  private UbicacionDTO ubicacion;
  private String web;
  private String telefono;
  private boolean eliminado;
}
