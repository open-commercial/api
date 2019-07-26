package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "cliente")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idCantidadSucursal", "empresa"})
@ToString
@JsonIgnoreProperties({"empresa", "viajante", "credencial", "eliminado"})
public class CantidadSucursal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) private long idCantidadSucursal;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @NotNull//(message = "{mensaje_cliente_vacio_empresa}")
  private Empresa empresa;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  @JsonGetter("nombreSucursal")
  public String getNombreSucursal() {
    return empresa.getNombre();
  }

}
