package sic.modelo.dto;

import lombok.*;

@Data
@EqualsAndHashCode(exclude = {"id_Localidad"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocalidadDTO {

  private long id_Localidad;
  private String nombre;
  private String codigoPostal;
  private ProvinciaDTO provincia;
  private boolean eliminada;

}
