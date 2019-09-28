package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

@Entity
@Table(name = "renglonpedido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonView(Views.Comprador.class)
public class RenglonPedido implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
  @DecimalMin(value = "0", message = "{mensaje_renglon_bonificacion_porcentaje_negativa}")
  private BigDecimal bonificacionPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_bonificacion_neta_negativa}")
  private BigDecimal bonificacionNeta;

  @Transient
  private BigDecimal importeAnterior;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_renglon_importe_negativo}")
  private BigDecimal importe;
  
}
