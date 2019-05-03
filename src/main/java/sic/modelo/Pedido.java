package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import sic.modelo.dto.UbicacionDTO;

@Entity
@Table(name = "pedido")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroPedido", "empresa"})
@ToString(exclude = {"facturas", "renglones"})
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id_Pedido",
    scope = Pedido.class)
@JsonIgnoreProperties({"cliente", "usuario", "empresa", "tipoDeEnvio"})
public class Pedido implements Serializable {

  @Id @GeneratedValue private long id_Pedido;

  private long nroPedido;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @NotNull(message = "{mensaje_pedido_fecha_vacia}")
  private Date fecha;

  @Temporal(TemporalType.TIMESTAMP)
  private Date fechaVencimiento;

  @Column(nullable = false)
  private String observaciones;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @NotNull(message = "{mensaje_pedido_empresa_vacia}")
  private Empresa empresa;

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
  @NotEmpty
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

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_pedido_total_actual_negativo}")
  private BigDecimal totalActual;

  @Enumerated(EnumType.STRING)
  private EstadoPedido estado;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_cantidad_de_productos_negativa}", inclusive = false)
  private BigDecimal cantidadArticulos;

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
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
