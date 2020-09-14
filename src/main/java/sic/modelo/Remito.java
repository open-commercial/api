package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;
import sic.modelo.dto.UbicacionDTO;
import sic.modelo.embeddable.ClienteEmbeddable;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "remito")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"cliente", "usuario", "sucursal", "clienteEmbedded", "renglones"})
@JsonView(Views.Comprador.class)
public class Remito implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long idRemito;

  private boolean eliminado;

  private LocalDateTime fecha;

  @OneToOne(mappedBy="remito")
  @JoinColumn(name="idFactura")
  private FacturaVenta facturaVenta;

  @Column(nullable = false)
  private long serie;

  @Column(nullable = false)
  private long nroRemito;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TipoDeComprobante tipoComprobante;

  @Embedded private ClienteEmbeddable clienteEmbedded;

  @ManyToOne
  @JoinColumn(name = "idCliente", referencedColumnName = "id_Cliente")
  @NotNull(message = "{mensaje_remito_cliente_vacio}")
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
  @NotNull(message = "{mensaje_remito_sucursal_vacia}")
  private Sucursal sucursal;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @NotNull(message = "{mensaje_remito_usuario_vacio}")
  private Usuario usuario;

  @ManyToOne
  @JoinColumn(name = "id_Transportista", referencedColumnName = "id_Transportista")
  private Transportista transportista;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "idRemito")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotEmpty(message = "{mensaje_remito_renglones_vacio}")
  private List<RenglonRemito> renglones;

  @Embedded private UbicacionDTO detalleEnvio;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_remito_total_envio_negativo}")
  private BigDecimal costoDeEnvio;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_remito_total_factura_negativo}")
  private BigDecimal totalFactura;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_remito_total_negativo}")
  private BigDecimal total;

  private BigDecimal pesoTotalEnKg;

  private BigDecimal volumenTotalEnM3;

  private BigDecimal cantidadDeBultos;

  private String observaciones;

  @JsonGetter("idCliente")
  public Long getIdCliente() {
    return cliente.getIdCliente();
  }

  @JsonGetter("nombreFiscalCliente")
  public String getNombreFiscalCliente() {
    return clienteEmbedded.getNombreFiscalCliente();
  }

  @JsonGetter("nroDeCliente")
  public String getNroDeCliente() {
    return clienteEmbedded.getNroCliente();
  }

  @JsonGetter("categoriaIVACliente")
  public CategoriaIVA getCategoriaIVA() {
    return clienteEmbedded.getCategoriaIVACliente();
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

  @JsonGetter("idTransportista")
  public long getIdTransportista() {
    return transportista.getIdTransportista();
  }

  @JsonGetter("nombreTransportista")
  public String getNombreTransportista() {
    return transportista.getNombre();
  }
}
