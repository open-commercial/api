package sic.modelo;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import sic.controller.Views;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.modelo.embeddable.PrecioProductoEmbeddable;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"idProducto", "descripcion"})
@ToString
@JsonIgnoreProperties({"medida", "rubro", "proveedor", "version"})
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

  @JsonUnwrapped
  @Embedded
  @JsonView(Views.Comprador.class)
  private PrecioProductoEmbeddable precioProducto;

  @JsonUnwrapped
  @Embedded
  @JsonView(Views.Comprador.class)
  private CantidadProductoEmbeddable cantidadProducto;

  @ManyToOne
  @JoinColumn(name = "id_Medida", referencedColumnName = "id_Medida")
  @NotNull(message = "{mensaje_producto_vacio_medida}")
  private Medida medida;

  @ManyToOne
  @JoinColumn(name = "id_Rubro", referencedColumnName = "id_Rubro")
  @NotNull(message = "{mensaje_producto_vacio_rubro}")
  private Rubro rubro;

  @JsonView(Views.Comprador.class)
  @Transient
  private boolean favorito;

  private boolean publico;

  @JsonView(Views.Vendedor.class)
  private boolean paraCatalogo;

  @NotNull(message = "{mensaje_producto_fecha_ultima_modificacion_vacia}")
  private LocalDateTime fechaUltimaModificacion;

  @ManyToOne
  @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
  @NotNull(message = "{mensaje_producto_vacio_proveedor}")
  private Proveedor proveedor;

  @Length(max = 250, message = "{mensaje_producto_longitud_nota}")
  private String nota;

  @NotNull(message = "{mensaje_producto_fecha_alta_vacia}")
  private LocalDateTime fechaAlta;

  //@FutureOrPresent(message = "{mensaje_fecha_vencimiento_invalida}")
  private LocalDate fechaVencimiento;

  private boolean eliminado;

  @Pattern(
      regexp = "^https:\\/\\/res.cloudinary.com\\/.*",
      message = "{mensaje_url_imagen_no_valida}")
  @JsonView(Views.Comprador.class)
  private String urlImagen;

  @Version
  private Long version;

  @JsonGetter("idMedida")
  @JsonView(Views.Comprador.class)
  public Long getIdMedida() {
    return medida.getIdMedida();
  }

  @JsonGetter("nombreMedida")
  @JsonView(Views.Comprador.class)
  public String getNombreMedida() {
    return medida.getNombre();
  }

  @JsonGetter("idRubro")
  @JsonView(Views.Comprador.class)
  public Long getIdRubro() {
    return rubro.getIdRubro();
  }

  @JsonGetter("nombreRubro")
  @JsonView(Views.Comprador.class)
  public String getNombreRubro() {
    return rubro.getNombre();
  }

  @JsonGetter("imagenHtmlRubro")
  @JsonView(Views.Comprador.class)
  public String getImagenHtmlRubro() {
    return rubro.getImagenHtml();
  }

  @JsonGetter("idProveedor")
  public Long getIdProveedor() {
    return proveedor.getIdProveedor();
  }

  @JsonGetter("razonSocialProveedor")
  public String getRazonSocialProveedor() {
    return proveedor.getRazonSocial();
  }
}
