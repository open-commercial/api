package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;

@Entity
@Table(name = "recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = {"numSerie", "numRecibo", "empresa", "fecha"})
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties({"formaDePago", "empresa", "cliente", "usuario", "proveedor"})
public class Recibo implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idRecibo;

  private long numSerie;

  private long numRecibo;

  private String idPagoMercadoPago;

  @NotNull(message = "{mensaje_recibo_fecha_vacia}")
  @PastOrPresent(message = "{mensaje_recibo_fecha_futura_no_permitida}")
  private LocalDateTime fecha;

  private boolean eliminado;

  @Column(nullable = false)
  @NotBlank(message = "{mensaje_recibo_concepto_vacio}")
  private String concepto;

  @ManyToOne
  @JoinColumn(name = "id_FormaDePago", referencedColumnName = "id_FormaDePago")
  @NotNull(message = "{mensaje_recibo_forma_de_pago_vacia}")
  private FormaDePago formaDePago;

  @ManyToOne
  @JoinColumn(name = "id_Empresa", referencedColumnName = "id_Empresa")
  @NotNull(message = "{mensaje_recibo_empresa_vacia}")
  private Empresa empresa;

  @ManyToOne
  @JoinColumn(name = "id_Cliente", referencedColumnName = "id_Cliente")
  private Cliente cliente;

  @ManyToOne
  @JoinColumn(name = "id_Proveedor", referencedColumnName = "id_Proveedor")
  private Proveedor proveedor;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @NotNull(message = "{mensaje_recibo_usuario_vacio}")
  private Usuario usuario;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_recibo_monto_negativo}")
  private BigDecimal monto;

  @JsonGetter("idFormaDePago")
  public long getIdFormaDePago() {
    return formaDePago.getIdFormaDePago();
  }

  @JsonGetter("nombreFormaDePago")
  public String getNombreFormaDePago() {
    return formaDePago.getNombre();
  }

  @JsonGetter("idEmpresa")
  public long getIdEmpresa() {
    return empresa.getIdEmpresa();
  }

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

  @JsonGetter("idCliente")
  public Long getIdCliente() {
    return (cliente != null) ? cliente.getIdCliente() : null;
  }

  @JsonGetter("nombreFiscalCliente")
  public String getNombreFiscalCliente() {
    return (cliente != null) ? cliente.getNombreFiscal() : "";
  }

  @JsonGetter("idProveedor")
  public Long getIdProveedor() {
    return (proveedor != null) ? proveedor.getId_Proveedor() : null;
  }

  @JsonGetter("razonSocialProveedor")
  public String getRazonSocialProveedor() {
    return (proveedor != null) ? proveedor.getRazonSocial() : "";
  }

  @JsonGetter("nombreUsuario")
  public String getNombreUsuario() {
    return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
  }

  @JsonGetter("idViajante")
  public Long getIdViajante() {
    return (cliente != null && cliente.getViajante() != null) ? cliente.getViajante().getId_Usuario() : null;
  }

  @JsonGetter("nombreViajante")
  public String getNombreViajante() {
    return (cliente != null && cliente.getViajante() != null) ? cliente.getNombreViajante() : null;
  }
}
