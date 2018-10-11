package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "renglonpedido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RenglonPedido implements Serializable {

  @Id
  @GeneratedValue
  private long id_RenglonPedido;

  private long idProductoItem;

  @Column(nullable = false)
  private String codigoItem;

  @Column(nullable = false)
  private String descripcionItem;

  @Column(nullable = false)
  private String medidaItem;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_precio_unitario_negativo}")
  private BigDecimal precioUnitario;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_descuento_porcentaje_negativo}")
  private BigDecimal descuentoPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_descuento_neto_negativo}")
  private BigDecimal descuentoNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_subTotal_negativo}")
  private BigDecimal subTotal;

  @JsonGetter("id_Producto")
  public Long getIdProducto() {
    return idProductoItem;
  }

  @JsonGetter("codigoProducto")
  public String getCodigo() {
    return codigoItem;
  }

  @JsonGetter("descripcionProducto")
  public String getDescripcion() {
    return descripcionItem;
  }

  @JsonGetter("precioDeListaProducto")
  public BigDecimal getPrecioDeLista() {
    return precioUnitario;
  }
  
}
