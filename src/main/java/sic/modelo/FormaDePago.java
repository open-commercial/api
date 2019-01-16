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
import com.querydsl.core.annotations.QueryInit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "formadepago")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre", "empresa"})
@ToString
@JsonIgnoreProperties("empresa")
public class FormaDePago implements Serializable {

  @Id
  @GeneratedValue
  private long id_FormaDePago;

  @Column(nullable = false)
  private String nombre;

  private boolean afectaCaja;

  private boolean predeterminado;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  private Empresa empresa;

  private boolean eliminada;

  @JsonGetter("idEmpresa")
  public Long getIdEmpresa() {
    return empresa.getId_Empresa();
  }

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

}