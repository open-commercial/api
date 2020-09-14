package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

@Entity
@Table(name = "localidad")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
@ToString
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties("provincia")
public class Localidad implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long idLocalidad;

  @Column(nullable = false)
  private String nombre;

  private String codigoPostal;

  @ManyToOne
  @JoinColumn(name = "idProvincia", referencedColumnName = "idProvincia")
  private Provincia provincia;

  private boolean envioGratuito;

  @Column(nullable = false, precision = 25, scale = 15)
  @DecimalMin(value = "0", inclusive = false, message = "{mensaje_ubicacion_costoEnvio_negativo}")
  private BigDecimal costoEnvio;

  @JsonGetter("idProvincia")
  public long getIdProvincia() {
    return provincia.getIdProvincia();
  }

  @JsonGetter("nombreProvincia")
  public String getNombreProvincia() {
    return provincia.getNombre();
  }
}
