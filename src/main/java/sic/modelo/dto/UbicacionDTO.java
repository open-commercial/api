package sic.modelo.dto;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(
    exclude = {
      "idUbicacion",
      "idLocalidad",
      "nombreLocalidad",
      "codigoPostal",
      "idProvincia",
      "nombreProvincia"
    })
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class UbicacionDTO implements Serializable {

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
  private BigDecimal costoDeEnvio;
  private String codigoPostal;
  private Long idProvincia;
  private String nombreProvincia;

  @Override
  public String toString() {
    return (calle != null ? calle + " " : "")
      + (numero != null ? numero + " ": "")
      + (piso != null ? piso + " ": "")
      + (departamento != null ? departamento + " ": "")
      + (nombreLocalidad != null ?  nombreLocalidad+ " " : "")
      + (nombreProvincia != null ? nombreProvincia : "");
  }
}
