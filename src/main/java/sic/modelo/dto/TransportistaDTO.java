package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(
    exclude = {
      "idTransportista",
      "idEmpresa",
      "nombreEmpresa"
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransportistaDTO {

  private long idTransportista;
  private String nombre;
  private UbicacionDTO ubicacion;
  private String web;
  private String telefono;
  private Long idEmpresa;
  private String nombreEmpresa;
  private boolean eliminado;
}
