package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
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

  @Id
  @GeneratedValue
  private long idUbicacion;

  @ManyToOne
  @JoinColumn(name = "id_Localidad", referencedColumnName = "id_Localidad")
  @QueryInit("provincia")
  private Localidad localidad;

  private String descripcion;

  @DecimalMin(value = "-90", message = "{mensaje_producto_cantidad_negativa}")
  @DecimalMax(value = "90", message = "{mensaje_producto_cantidad_negativa}")
  private Double latitud;

  @DecimalMin(value = "-180", message = "{mensaje_producto_cantidad_negativa}")
  @DecimalMax(value = "180", message = "{mensaje_producto_cantidad_negativa}")
  private Double longitud;

  @Length(max = 255, message = "{mensaje_ubicacion_longitud_calle }")
  @NotBlank
  private String calle;

  private int numero;

  private Integer piso;

  @Length(max = 255, message = "{mensaje_ubicacion_longitud_departamento}")
  private String departamento;

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

  @JsonGetter("codigoPostal")
  public String getCodigoPostal() {
    return (localidad != null) ? localidad.getCodigoPostal() : null;
  }
}
