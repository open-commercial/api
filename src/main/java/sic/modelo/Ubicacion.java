package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import sic.config.Views;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "ubicacion")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idUbicacion", "localidad"})
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties("localidad")
public class Ubicacion implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
  private String calle;

  private Integer numero;

  private String piso;

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

  @JsonGetter("costoDeEnvio")
  public BigDecimal getCostoDeEnvio() {
    if (localidad != null) {
      if (localidad.isEnvioGratuito()) {
        return BigDecimal.ZERO;
      } else {
        return localidad.getCostoEnvio();
      }
    } else {
      return null;
    }
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
    return (calle != null ? calle + " " : "")
        + (numero != null ? numero + " " : "")
        + (piso != null ? piso + " " : "")
        + (departamento != null ? departamento + " " : "")
        + ((descripcion != null && !descripcion.isEmpty()) ? "(" + descripcion + ")" + " " : "")
        + (localidad != null ? localidad.getNombre() + " " : "")
        + (localidad != null ? localidad.getProvincia().getNombre() : "");
  }
}
