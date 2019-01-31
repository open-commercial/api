package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "ubicacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idUbicacion", "localidad"})
@ToString
@JsonIgnoreProperties("localidad")
public class Ubicacion implements Serializable {

  @Id @GeneratedValue private long idUbicacion;

  @QueryInit("provincia")
  private Localidad localidad;

  private String descripcion;

  private Double latitud;

  private Double longitud;

  @NotBlank(message = "{mensaje_ubicacion_calle_vacia}")
  @NotNull(message = "{mensaje_ubicacion_calle_vacia}")
  private String calle;

  @NotNull(message = "{mensaje_ubicacion_numero_vacio}")
  private Integer numero;

  private Integer piso;

  private String departamento;

  private Integer codigoPostal;

  private boolean eliminada;

  @JsonGetter("idLocalidad")
  public Long getIdLocalidad() {
    return (localidad != null) ? localidad.getId_Localidad() : null;
  }

  @JsonGetter("nombreLocalidad")
  public String getNombreLocalidad() {
    return (localidad != null) ? localidad.getNombre() : null;
  }

  @JsonGetter("idProvincia")
  public Long getIdProvincia() {
    return (localidad != null) ? localidad.getProvincia().getId_Provincia() : null;
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return (localidad != null) ? localidad.getProvincia().getNombre() : null;
  }
}
