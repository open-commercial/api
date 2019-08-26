package sic.modelo;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "medida")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre", "empresa"})
@ToString
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties({"empresa", "eliminada"})
public class Medida implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id_Medida;

  @Column(nullable = false)
  @NotNull(message = "{mensaje_medida_vacio_nombre}")
  @NotEmpty(message = "{mensaje_medida_vacio_nombre}")
  private String nombre;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @NotNull(message = "{mensaje_medida_empresa_vacia}")
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
