package sic.modelo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import sic.config.Views;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombreFiscal", "idFiscal"})
@ToString
@JsonIgnoreProperties({
  "viajante",
  "credencial",
  "eliminado",
})
@JsonView(Views.Comprador.class)
public class Cliente implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Cliente")
  private long idCliente;
  
  @Transient
  private BigDecimal saldoCuentaCorriente;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_cliente_monto_compra_minima_negativa}")
  @NotNull(message = "{mensaje_cliente_monto_compra_minima_vacia}")
  private BigDecimal montoCompraMinima;

  private String nroCliente;

  @Column(nullable = false)
  @NotBlank(message = "{mensaje_cliente_vacio_nombreFiscal}")
  private String nombreFiscal;

  private String nombreFantasia;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = "{mensaje_cliente_vacio_categoriaIVA}")
  private CategoriaIVA categoriaIVA;

  private Long idFiscal;

  @Email(message = "{mensaje_correo_formato_incorrecto}")
  private String email;

  @NotBlank(message = "{mensaje_cliente_vacio_telefono}")
  private String telefono;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "idUbicacionFacturacion", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacionFacturacion;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "idUbicacionEnvio", referencedColumnName = "idUbicacion")
  @QueryInit("localidad.provincia")
  private Ubicacion ubicacionEnvio;

  private String contacto;

  @NotNull(message = "{mensaje_cliente_fecha_vacia}")
  private LocalDateTime fechaAlta;

  @ManyToOne
  @JoinColumn(name = "id_Usuario_Viajante", referencedColumnName = "id_Usuario")
  private Usuario viajante;

  @ManyToOne
  @JoinColumn(name = "id_Usuario_Credencial")
  private Usuario credencial;

  private boolean eliminado;

  private boolean predeterminado;

  private boolean puedeComprarAPlazo;

  @JsonGetter("idViajante")
  public Long getIdViajante() {
    if (viajante != null) {
      return viajante.getIdUsuario();
    } else {
      return null;
    }
  }

  @JsonGetter("nombreViajante")
  public String getNombreViajante() {
    if (viajante != null) {
      return viajante.getNombre()
          + " "
          + viajante.getApellido()
          + " ("
          + viajante.getUsername()
          + ")";
    } else {
      return null;
    }
  }

  @JsonGetter("idCredencial")
  public Long getIdCredencial() {
    if (credencial != null) {
      return credencial.getIdUsuario();
    } else {
      return null;
    }
  }

  @JsonGetter("nombreCredencial")
  public String getNombreCredencial() {
    if (credencial != null) {
      return credencial.getNombre()
          + " "
          + credencial.getApellido()
          + " ("
          + credencial.getUsername()
          + ")";
    } else {
      return null;
    }
  }

  @JsonGetter("detalleUbicacionDeFacturacion")
  public String getDetalleUbicacionDeFacturacion() {
    return (ubicacionFacturacion != null ? ubicacionFacturacion.toString() : null);
  }

  @JsonGetter("detalleUbicacionDeEnvio")
  public String getDetalleUbicacionDeEnvio() {
    return (ubicacionEnvio != null ? ubicacionEnvio.toString() : null);
  }
}
