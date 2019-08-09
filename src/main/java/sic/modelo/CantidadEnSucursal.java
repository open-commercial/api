package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "cantidadensucursal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idCantidadSucursal", "empresa"})
@ToString
public class CantidadEnSucursal implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idCantidadSucursal;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @NotNull
  private Empresa empresa;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  private String estanteria;

  private String estante;

  @JsonGetter("idSucursal")
  public Long getIdSucursal() {
    return empresa.getId_Empresa();
  }

  @JsonGetter("nombreSucursal")
  public String getNombreSucursal() {
    return empresa.getNombre();
  }
}
