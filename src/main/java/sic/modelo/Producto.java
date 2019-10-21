package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import javax.validation.constraints.NotEmpty;
import sic.controller.Views;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"descripcion", "empresa"})
@ToString
@JsonView(Views.Vendedor.class)
@JsonIgnoreProperties({"medida", "rubro", "proveedor", "empresa"})
public class Producto implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonView(Views.Comprador.class)
  private long idProducto;

  @JsonView(Views.Comprador.class)
  private String codigo;

  @NotNull(message = "{mensaje_producto_vacio_descripcion}")
  @NotEmpty(message = "{mensaje_producto_vacio_descripcion}")
  @JsonView(Views.Comprador.class)
  private String descripcion;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidad;

  @Transient
  @JsonView(Views.Comprador.class)
  private boolean hayStock;

  @Transient
  @JsonView(Views.Comprador.class)
  private BigDecimal precioBonificado;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidadMinima_negativa}")
  private BigDecimal cantMinima;

  @Column(precision = 25, scale = 15)
  @DecimalMin(
      value = "0",
      inclusive = false,
      message = "{mensaje_producto_cantidad_bulto_invalida}")
  @JsonView(Views.Comprador.class)
  private BigDecimal bulto;

  @ManyToOne
  @JoinColumn(name = "id_Medida", referencedColumnName = "id_Medida")
  @NotNull(message = "{mensaje_producto_vacio_medida}")
  private Medida medida;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_precioCosto_negativo}")
  private BigDecimal precioCosto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_gananciaPorcentaje_negativo}")
  private BigDecimal gananciaPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_gananciaNeto_negativo}")
  private BigDecimal gananciaNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_venta_publico_negativo}")
  private BigDecimal precioVentaPublico;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_IVAPorcentaje_negativo}")
  private BigDecimal ivaPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_IVANeto_negativo}")
  private BigDecimal ivaNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_precioLista_negativo}")
  @JsonView(Views.Comprador.class)
  private BigDecimal precioLista;

  @ManyToOne
  @JoinColumn(name = "id_Rubro", referencedColumnName = "id_Rubro")
  @NotNull(message = "{mensaje_producto_vacio_rubro}")
  private Rubro rubro;

  private boolean ilimitado;

  private boolean publico;

  private boolean destacado;

  private LocalDateTime fechaUltimaModificacion;

  private String estanteria;

  private String estante;

  @ManyToOne
  @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
  @NotNull(message = "{mensaje_producto_vacio_proveedor}")
  private Proveedor proveedor;

  @NotNull(message = "{mensaje_producto_vacio_nota}")
  private String nota;

  private LocalDateTime fechaAlta;

  private LocalDateTime fechaVencimiento;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @NotNull(message = "{mensaje_producto_vacio_empresa}")
  private Empresa empresa;

  private boolean eliminado;

  @JsonView(Views.Comprador.class)
  private String urlImagen;

  @JsonGetter("nombreMedida")
  @JsonView(Views.Comprador.class)
  public String getNombreMedida() {
    return medida.getNombre();
  }

  @JsonGetter("nombreRubro")
  public String getNombreRubro() {
    return rubro.getNombre();
  }

  @JsonGetter("razonSocialProveedor")
  public String getRazonSocialProveedor() {
    return proveedor.getRazonSocial();
  }
}
