package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"id_Localidad", "idProvincia", "nombreProvincia"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocalidadDTO {

  private long id_Localidad;
  private String nombre;
  private String codigoPostal;
  private Long idProvincia;
  private String nombreProvincia;
  private boolean eliminada;

}
