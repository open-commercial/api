package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;

@Entity
@Table(name = "formadepago")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "nombre")
@ToString
@JsonView(Views.Comprador.class)
public class FormaDePago implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_FormaDePago")
  private long idFormaDePago;

  @NotNull(message = "{mensaje_forma_de_pago_nombre_vacio}")
  @NotEmpty(message = "{mensaje_forma_de_pago_nombre_vacio}")
  @Column(nullable = false)
  private String nombre;

  private boolean afectaCaja;

  private boolean predeterminado;

  private boolean eliminada;

}
