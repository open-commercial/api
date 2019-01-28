package sic.modelo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
  private String codigoPostal;

  @ManyToOne
  @JoinColumn(name = "id_Provincia", referencedColumnName = "id_Provincia")
  private Provincia provincia;

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
