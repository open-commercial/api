package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import sic.controller.Views;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "cantidadensucursal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idCantidadSucursal", "empresa"})
@ToString
@JsonIgnoreProperties({"empresa", "viajante", "credencial", "eliminado"})
public class CantidadEnSucursal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) private long idCantidadSucursal;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @NotNull//(message = "{mensaje_cliente_vacio_empresa}")
  private Empresa empresa;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  @Transient
  @JsonView(Views.Public.class)
  private boolean hayStock;

  private String estanteria;

  private String estante;

  @JsonGetter("nombreSucursal")
  public String getNombreSucursal() {
    return empresa.getNombre();
  }

}
