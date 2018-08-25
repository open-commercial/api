package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "renglonpedido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "producto")
@ToString
public class RenglonPedido implements Serializable {

  @Id
  @GeneratedValue
  private long id_RenglonPedido;

  @ManyToOne
  @JoinColumn(name = "id_Producto", referencedColumnName = "id_Producto")
  private Producto producto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_descuento_porcentaje_negativo}")
  private BigDecimal descuento_porcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_descuento_neto_negativo}")
  private BigDecimal descuento_neto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_subTotal_negativo}")
  private BigDecimal subTotal;

  @JsonGetter("id_Producto")
  public Long getIdProducto() {
    return producto.getId_Producto();
  }

  @JsonGetter("codigoProducto")
  public String getCodigo() {
    return producto.getCodigo();
  }

  @JsonGetter("descripcionProducto")
  public String getDescripcion() {
    return producto.getDescripcion();
  }

  @JsonGetter("precioDeListaProducto")
  public BigDecimal getPrecioDeLista() {
    return producto.getPrecioLista();
  }
  
}
