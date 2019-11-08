package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@EqualsAndHashCode(of = "descripcion")
@ToString
@JsonIgnoreProperties({"medida", "rubro", "proveedor"})
@JsonView(Views.Vendedor.class)
public class Producto implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonView(Views.Comprador.class)
  private Long idProducto;

  @JsonView(Views.Comprador.class)
  private String codigo;

  @NotNull(message = "{mensaje_producto_vacio_descripcion}")
  @NotEmpty(message = "{mensaje_producto_vacio_descripcion}")
  @JsonView(Views.Comprador.class)
  private String descripcion;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "idProducto")
  @JsonProperty(access = JsonProperty.Access.READ_WRITE)
  @JsonView(Views.Comprador.class)
  private Set<CantidadEnSucursal> cantidadEnSucursales;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidad_negativa}")
  private BigDecimal cantidadTotalEnSucursales;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_producto_cantidadMinima_negativa}")
  private BigDecimal cantMinima;

  @Transient
  @JsonView(Views.Comprador.class)
  private boolean hayStock;

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

  @JsonView(Views.Comprador.class)
  private boolean oferta;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", inclusive = false, message = "{mensaje_producto_bonificacion_oferta_inferior_0}")
  @DecimalMax(value = "100", inclusive = false, message = "{mensaje_producto_bonificacion_oferta_superior_100}")
  @JsonView(Views.Comprador.class)
  private BigDecimal porcentajeBonificacionOferta;

  @Transient
  @JsonView(Views.Comprador.class)
  private BigDecimal precioListaBonificado;

  @ManyToOne
  @JoinColumn(name = "id_Rubro", referencedColumnName = "id_Rubro")
  @NotNull(message = "{mensaje_producto_vacio_rubro}")
  private Rubro rubro;

  private boolean ilimitado;

  private boolean publico;

  @NotNull(message = "{mensaje_producto_fecha_ultima_modificacion_vacia}")
  private LocalDateTime fechaUltimaModificacion;

  @ManyToOne
  @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
  @NotNull(message = "{mensaje_producto_vacio_proveedor}")
  private Proveedor proveedor;

  @NotNull(message = "{mensaje_producto_vacio_nota}")
  private String nota;

  @NotNull(message = "{mensaje_producto_fecha_alta_vacia}")
  private LocalDateTime fechaAlta;

  @FutureOrPresent(message = "{mensaje_fecha_vencimiento_invalida}")
  private LocalDate fechaVencimiento;

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
