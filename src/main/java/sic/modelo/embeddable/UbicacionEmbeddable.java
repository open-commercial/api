package sic.modelo.embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class UbicacionEmbeddable {

  private String descripcion;
  private Double latitud;
  private Double longitud;
  private String calle;
  private Integer numero;
  private String piso;
  private String departamento;
  private LocalidadEmbeddable localidadEmbeddable;

  @Override
  public String toString() {
    return (calle != null ? calle + " " : "")
        + (numero != null ? numero + " " : "")
        + (piso != null ? piso + " " : "")
        + (departamento != null ? departamento + " " : "")
        + (localidadEmbeddable.getNombreLocalidad() != null
            ? localidadEmbeddable.getNombreLocalidad() + " "
            : "")
        + (localidadEmbeddable.getProvinciaEmbeddable().getNombreProvincia() != null
            ? localidadEmbeddable.getProvinciaEmbeddable().getNombreProvincia()
            : "");
  }
}
