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
@EqualsAndHashCode(of = "nombre")
@ToString
@JsonIgnoreProperties("eliminada")
@JsonView(Views.Comprador.class)
public class Medida implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Medida")
  private long idMedida;

  @Column(nullable = false)
  @NotNull(message = "{mensaje_medida_vacio_nombre}")
  @NotEmpty(message = "{mensaje_medida_vacio_nombre}")
  private String nombre;

  private boolean eliminada;
}
