package sic.model;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(
    exclude = {
      "idUbicacion",
      "idLocalidad",
      "nombreLocalidad",
      "codigoPostal",
      "idProvincia",
      "nombreProvincia"
    })
public class Ubicacion implements Serializable {

  private long idUbicacion;
  private String descripcion;
  private Double latitud;
  private Double longitud;
  private String calle;
  private Integer numero;
  private String piso;
  private String departamento;
  private Long idLocalidad;
  private String nombreLocalidad;
  private String codigoPostal;
  private Long idProvincia;
  private String nombreProvincia;
}
