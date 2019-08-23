package sic.modelo;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

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
@Table(name = "formadepago")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "nombre")
@ToString
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties("empresa")
public class FormaDePago implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id_FormaDePago;

  @NotNull(message = "{mensaje_forma_de_pago_nombre_vacio}")
  @NotEmpty(message = "{mensaje_forma_de_pago_nombre_vacio}")
  @Column(nullable = false)
  private String nombre;

  private boolean afectaCaja;

  private boolean predeterminado;

  private boolean eliminada;

}
