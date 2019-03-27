package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "ubicacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idUbicacion", "localidad"})
@JsonIgnoreProperties("localidad")
public class Ubicacion implements Serializable {

  @Id
  @GeneratedValue
  private long idUbicacion;

  @ManyToOne
  @JoinColumn(name = "idLocalidad", referencedColumnName = "idLocalidad")
  @QueryInit("provincia")
  @NotNull
  private Localidad localidad;

  private String descripcion;

  @DecimalMin(value = "-90", message = "{mensaje_ubicacion_latitud_fuera_de_rango}")
  @DecimalMax(value = "90", message = "{mensaje_ubicacion_latitud_fuera_de_rango}")
  private Double latitud;

  @DecimalMin(value = "-180", message = "{mensaje_ubicacion_longitud_fuera_de_rango}")
  @DecimalMax(value = "180", message = "{mensaje_ubicacion_longitud_fuera_de_rango}")
  private Double longitud;

  @Length(max = 255, message = "{mensaje_ubicacion_longitud_calle}")
  @NotBlank(message = "{mensaje_ubicacion_calle_vacia}")
  private String calle;

  private int numero;

  private Integer piso;

  @Length(max = 255, message = "{mensaje_ubicacion_longitud_departamento}")
  private String departamento;

  @JsonGetter("idLocalidad")
  public Long getIdLocalidad() {
    return (localidad != null) ? localidad.getIdLocalidad() : null;
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return (localidad != null) ? localidad.getNombre() : null;
  }

  @JsonGetter("idProvincia")
  public Long getIdProvincia() {
    return (localidad != null) ? localidad.getProvincia().getIdProvincia() : null;
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return (localidad != null) ? localidad.getProvincia().getNombre() : null;
  }

  @JsonGetter("codigoPostal")
  public String getCodigoPostal() {
    return (localidad != null) ? localidad.getCodigoPostal() : null;
  }

  @Override
  public String toString() {
    return calle
        + " "
        + numero
        + (piso != null ? ", " + piso : "")
        + (departamento != null ? departamento: "")
        + (localidad != null ?  ", " + localidad.getNombre() : " ")
        + " "
        + (localidad != null ? localidad.getProvincia().getNombre() : " ");
  }
}
