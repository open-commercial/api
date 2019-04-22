package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = {"numSerie", "numRecibo", "empresa", "fecha"})
@JsonIgnoreProperties({"formaDePago", "empresa", "cliente", "usuario", "proveedor"})
public class Recibo implements Serializable {

  @Id
  @GeneratedValue
  private Long idRecibo;

  private long numSerie;

  private long numRecibo;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date fecha;

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
  @DecimalMin(value = "0", message = "{mensaje_monto_negativo}")
  private BigDecimal monto;

  @JsonGetter("nombreFormaDePago")
  public String getNombreFormaDePago() {
    return formaDePago.getNombre();
  }

  @JsonGetter("nombreEmpresa")
  public String getNombreEmpresa() {
    return empresa.getNombre();
  }

  @JsonGetter("idCliente")
  public Long getIdCliente() {
    return (cliente != null) ? cliente.getId_Cliente() : null;
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
