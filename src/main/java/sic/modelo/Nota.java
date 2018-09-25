package sic.modelo;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nota")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"fecha", "tipoComprobante", "serie", "nroNota", "empresa"})
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "idNota",
    scope = Nota.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = NotaCredito.class),
  @JsonSubTypes.Type(value = NotaDebito.class)
})
@JsonIgnoreProperties({"cliente", "empresa", "usuario","facturaVenta", "proveedor", "facturaCompra", "recibo"})
public abstract class Nota implements Serializable {

  @JsonGetter(value = "type")
  public String getType() {
    return this.getClass().getSimpleName();
  }

  @Id @GeneratedValue private Long idNota;

  @Column(nullable = false)
  private long serie;

  @Column(nullable = false)
  private long nroNota;

  private boolean eliminada;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TipoDeComprobante tipoComprobante;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date fecha;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  private Empresa empresa;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  private Usuario usuario;

  @ManyToOne
  @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
  private Cliente cliente;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "id_FacturaVenta")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private FacturaVenta facturaVenta;

  @ManyToOne
  @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
  private Proveedor proveedor;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "id_FacturaCompra")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private FacturaCompra facturaCompra;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Movimiento movimiento;

  @Column(nullable = false)
  private String motivo;

  @Column(precision = 25, scale = 15)
  private BigDecimal subTotalBruto;

  @Column(precision = 25, scale = 15)
  private BigDecimal iva21Neto;

  @Column(precision = 25, scale = 15)
  private BigDecimal iva105Neto;

  @Column(precision = 25, scale = 15)
  private BigDecimal total;

  private long CAE;

  @Temporal(TemporalType.TIMESTAMP)
  private Date vencimientoCAE;

  private long numSerieAfip;

  private long numNotaAfip;

  @JsonGetter("idEmpresa")
  public Long getIdEmpresa() {
    return empresa.getId_Empresa();
  }

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

  @JsonGetter("idCliente")
  public Long getIdCliente() {
    if (cliente != null) {
      return cliente.getId_Cliente();
    } else {
      return null;
    }
  }

  @JsonGetter("razonSocialCliente")
  public String getRazonSocialCliente() {
    if (cliente != null) {
      return cliente.getRazonSocial();
    } else {
      return null;
    }
  }

  @JsonGetter("idProveedor")
  public Long getIdProveedor() {
    if (proveedor != null) {
      return proveedor.getId_Proveedor();
    } else {
      return null;
    }
  }

  @JsonGetter("razonSocialProveedor")
  public String getRazonSocialProveedor() {
    if (proveedor != null) {
      return proveedor.getRazonSocial();
    } else {
      return null;
    }
  }

  @JsonGetter("idFacturaVenta")
  public Long getIdFacturaVenta() {
    if (facturaVenta != null) {
      return facturaVenta.getId_Factura();
    } else {
      return null;
    }
  }

  @JsonGetter("idFacturaCompra")
  public Long getIdFacturaCompra() {
    if (facturaCompra != null) {
      return facturaCompra.getId_Factura();
    } else {
      return null;
    }
  }

  @JsonGetter("idUsuario")
  public Long getIdRecibo() {
    return usuario.getId_Usuario();
  }

  @JsonGetter("nombreUsuario")
  public String getNombreUsuario() {
    return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
  }
}
