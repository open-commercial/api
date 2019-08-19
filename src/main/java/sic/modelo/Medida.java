package sic.modelo;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "medida")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "nombre")
@ToString
@JsonIgnoreProperties({"sucursal", "eliminada"})
public class Medida implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id_Medida;

  @Column(nullable = false)
  @NotNull(message = "{mensaje_medida_vacio_nombre}")
  @NotEmpty(message = "{mensaje_medida_vacio_nombre}")
  private String nombre;

  private boolean eliminada;
}
