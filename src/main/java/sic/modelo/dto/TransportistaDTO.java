package sic.modelo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(
    exclude = {
      "id_Transportista",
      "idEmpresa",
      "nombreEmpresa"
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransportistaDTO {

  private long id_Transportista;
  private String nombre;
  private UbicacionDTO ubicacion;
  private String web;
  private String telefono;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminado;
}
