package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "localidad")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
@JsonIgnoreProperties({"provincia", "eliminada"})
public class Localidad implements Serializable {

  @Id @GeneratedValue private long id_Localidad;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false)
  @NotBlank(message = "{mensaje_ubicacion_codigo_postal_vacio}")
  private String codigoPostal;

  @ManyToOne
  @JoinColumn(name = "id_Provincia", referencedColumnName = "id_Provincia")
  private Provincia provincia;

  private boolean envioGratuito;

  @Column(nullable = false, precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_ubicacion_costoEnvio_negativo}")
  private BigDecimal costoEnvio;

  private boolean eliminada;

  @JsonGetter("idProvincia")
  public long getIdProvincia() {
    return provincia.getId_Provincia();
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return provincia.getNombre();
  }
}
