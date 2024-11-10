package org.opencommercial.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "factura")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "numSerie", "numFactura", "sucursal"})
@ToString(exclude = {"renglones"})
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "idFactura",
    scope = Factura.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@Type(value = FacturaCompra.class), @Type(value = FacturaVenta.class)})
public abstract class Factura implements Serializable {

  // bug: https://jira.spring.io/browse/DATAREST-304
  @JsonGetter(value = "type")
  public String getType() {
    return this.getClass().getSimpleName();
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Factura")
  private long idFactura;

  @ManyToOne
  @JoinColumn(name = "id_Usuario")
  @NotNull(message = "{mensaje_factura_usuario_vacio}")
  private Usuario usuario;

  @NotNull(message = "{mensaje_factura_fecha_vacia}")
  private LocalDateTime fecha;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{mensaje_factura_tipo_factura_vacia}")
  private TipoDeComprobante tipoComprobante;

  private long numSerie;

  private long numFactura;

  // @FutureOrPresent(message = "{mensaje_fecha_vencimiento_invalida}")
  private LocalDate fechaVencimiento;

  @ManyToOne
  @JoinColumn(name = "id_Pedido")
  private Pedido pedido;

  @ManyToOne
  @JoinColumn(name = "id_Transportista")
  private Transportista transportista;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "id_Factura")
  @JsonProperty(access = Access.WRITE_ONLY)
  @NotEmpty(message = "{mensaje_factura_renglones_vacio}")
  private List<RenglonFactura> renglones;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_subtotal_negativo}")
  private BigDecimal subTotal;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_recargo_porcentaje_negativo}")
  private BigDecimal recargoPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_recargo_neto_negativo}")
  private BigDecimal recargoNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_descuento_porcentaje_negativo}")
  @DecimalMax(value = "100", message = "{mensaje_descuento_porcentaje_superior_100}")
  private BigDecimal descuentoPorcentaje;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_descuento_neto_negativo}")
  private BigDecimal descuentoNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_sub_total_bruto}")
  private BigDecimal subTotalBruto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_iva_105_neto_negativo}")
  private BigDecimal iva105Neto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_iva_21_neto_negativo}")
  private BigDecimal iva21Neto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_factura_impuesto_interno_neto}")
  private BigDecimal impuestoInternoNeto;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_total_negativo}")
  private BigDecimal total;

  private String observaciones;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_cantidad_de_productos_no_valida}", inclusive = false)
  private BigDecimal cantidadArticulos;

  @ManyToOne
  @JoinColumn(name = "idSucursal")
  @NotNull(message = "{mensaje_factura_sucursal_vacia}")
  private Sucursal sucursal;

  private boolean eliminada;

  private long cae;

  private LocalDate vencimientoCae;

  private long numSerieAfip;

  private long numFacturaAfip;

  @JsonGetter("subTotalBruto")
  public BigDecimal getSubTotalBrutoSegunTipoDeComprobante() {
    return (this.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || this.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO)
            ? this.subTotalBruto.add(this.iva21Neto).add(this.iva105Neto)
            : this.subTotalBruto;
  }

  @JsonGetter("iva21Neto")
  public BigDecimal getIva21NetoSegunTipoDeComprobante() {
    return (this.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || this.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO)
            ? BigDecimal.ZERO
            : this.iva21Neto;
  }

  @JsonGetter("iva105Neto")
  public BigDecimal getIva105NetoSegunTipoDeComprobante() {
    return (this.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || this.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO)
            ? BigDecimal.ZERO
            : this.iva105Neto;
  }

  @JsonGetter("idPedido")
  public Long getIdPedido() {
    return (this.pedido != null ? this.pedido.getIdPedido() : null);
  }

  @JsonGetter("nroPedido")
  public Long getNroPedido() {
    return (this.pedido != null ? this.pedido.getNroPedido() : null);
  }

  @JsonGetter("idTransportista")
  public Long getIdTransportista() {
    return transportista != null ? transportista.getIdTransportista() : null;
  }

  @JsonGetter("nombreTransportista")
  public String getNombreTransportista() {
    return transportista != null ? transportista.getNombre() : "";
  }

  @JsonGetter("idSucursal")
  public long getIdSucursal() {
    return sucursal.getIdSucursal();
  }

  @JsonGetter("nombreSucursal")
  public String getNombreSucursal() {
    return sucursal.getNombre();
  }

  @JsonGetter("idUsuario")
  public long getIdUsuario() {
    return usuario.getIdUsuario();
  }

  @JsonGetter("nombreUsuario")
  public String getNombreUsuario() {
    return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
  }
}
