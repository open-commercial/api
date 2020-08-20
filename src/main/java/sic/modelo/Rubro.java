package sic.modelo;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import sic.controller.Views;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "rubro")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "nombre")
@ToString
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
