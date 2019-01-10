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
@Table(name = "medida")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre", "empresa"})
@ToString
@JsonIgnoreProperties({"empresa", "eliminada"})
public class Medida implements Serializable {

  @Id @GeneratedValue private long id_Medida;

  @Column(nullable = false)
  private String nombre;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  private Empresa empresa;

  private boolean eliminada;

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

  @JsonGetter("idEmpresa")
  public long getIdEmpresa() {
    return empresa.getId_Empresa();
  }
}
