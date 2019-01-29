package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryInit;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import java.io.Serializable;
import java.math.BigDecimal;

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

  @QueryInit("provincia")
  private Localidad localidad;

  private String descripcion;

  private Long latitud;

  private Long longitud;

  private String calle;

  private Integer numero;

  private Integer piso;

  private String departamento;

  private Integer codigoPostal;

  private boolean eliminada;

  private boolean envioGratuito;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_ubicacion_costoEnvio_negativo}")
  private BigDecimal costoEnvio;

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
