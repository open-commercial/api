package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(
    exclude = {
      "id_Transportista",
      "idEmpresa",
      "nombreEmpresa"
    })
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
