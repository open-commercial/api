package sic.modelo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(
    exclude = {
      "id_Transportista",
      "idEmpresa",
      "nombreEmpresa"
    })
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
