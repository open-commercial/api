package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.opencommercial.config.Views;

import java.io.Serializable;

@Entity
@Table(name = "rubro")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "nombre")
@ToString(exclude = {"imagenHtml"})
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties({"eliminado"})
public class Rubro implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Rubro")
  private long idRubro;

  @Column(nullable = false)
  @NotNull(message = "{mensaje_rubro_nombre_vacio}")
  @NotEmpty(message = "{mensaje_rubro_nombre_vacio}")
  private String nombre;

  private boolean eliminado;

  @Column(length = 10000)
  @Length(max = 10000, message = "{mensaje_rubro_imagen_html_error}")
  private String imagenHtml;
}
