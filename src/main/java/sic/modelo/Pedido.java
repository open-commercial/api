package sic.modelo;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.*;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import sic.controller.Views;
import sic.modelo.dto.UbicacionDTO;

@Entity
@Table(name = "pedido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroPedido", "sucursal"})
@ToString(exclude = {"facturas", "renglones"})
@JsonView(Views.Comprador.class)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id_Pedido",
    scope = Pedido.class)
@JsonIgnoreProperties({"cliente", "usuario", "sucursal", "tipoDeEnvio"})
public class Pedido implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id_Pedido;

  private long nroPedido;

  @NotNull(message = "{mensaje_pedido_fecha_vacia}")
  private LocalDateTime fecha;

  // @FutureOrPresent(message = "{mensaje_fecha_vencimiento_invalida}")
  private LocalDate fechaVencimiento;

  @Column(nullable = false)
  private String observaciones;

  @ManyToOne
  @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
  @NotNull(message = "{mensaje_pedido_sucursal_vacia}")
  private Sucursal sucursal;

  @Embedded
  private UbicacionDTO detalleEnvio;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TipoDeEnvio tipoDeEnvio;

  private boolean eliminado;

  @ManyToOne
  @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
  @NotNull(message = "{mensaje_pedido_cliente_vacio}")
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @NotNull(message = "{mensaje_pedido_usuario_vacio}")
  private Usuario usuario;

  @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
  @JsonProperty(access = Access.WRITE_ONLY)
  private List<Factura> facturas;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "id_Pedido")
  @JsonProperty(access = Access.WRITE_ONLY)
  @NotEmpty(message = "{mensaje_pedido_renglones_vacios}")
  private List<RenglonPedido> renglones;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_subtotal_negativo}")
  private BigDecimal subTotal;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_recargo_porcentaje_negativo}")
  private BigDecimal recargoPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_regargo_neto_negativo}")
  private BigDecimal recargoNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_descuento_porcentaje_negativo}")
  @DecimalMax(value = "100", message = "{mensaje_descuento_porcentaje_superior_100}")
  private BigDecimal descuentoPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_descuento_neto_negativo}")
  private BigDecimal descuentoNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_pedido_total_estimado_negativo}")
  private BigDecimal totalEstimado;

  @Transient
  private BigDecimal totalActual;

  @Enumerated(EnumType.STRING)
  private EstadoPedido estado;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_cantidad_de_productos_negativa}", inclusive = false)
  private BigDecimal cantidadArticulos;

  @JsonGetter("nombreSucursal")
  public String getNombreSucursal() {
    return sucursal.getNombre();
  }

  @JsonGetter("nombreFiscalCliente")
  public String getNombreFiscalCliente() {
    return cliente.getNombreFiscal();
  }

  @JsonGetter("nombreUsuario")
  public String getNombreUsuario() {
    return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
  }

  @JsonGetter("idViajante")
  public Long getIdViajante() {
    return (cliente.getViajante() != null) ? cliente.getViajante().getId_Usuario() : null;
  }

  @JsonGetter("nombreViajante")
  public String getNombreViajante() {
    return (cliente.getViajante() != null) ? cliente.getNombreViajante() :  null;
  }

  @JsonGetter("detalleEnvio")
  public String getEnvio() {
    return (detalleEnvio.getCalle() != null ? detalleEnvio.getCalle() + " " : "")
        + (detalleEnvio.getNumero() != null ? detalleEnvio.getNumero() + " " : "")
        + (detalleEnvio.getPiso() != null ? detalleEnvio.getPiso() + " " : "")
        + (detalleEnvio.getDepartamento() != null ? detalleEnvio.getDepartamento() + " " : "")
        + (detalleEnvio.getNombreLocalidad() != null ? detalleEnvio.getNombreLocalidad() + " " : "")
        + (detalleEnvio.getNombreProvincia() != null ? detalleEnvio.getNombreProvincia() : "");
  }
}
