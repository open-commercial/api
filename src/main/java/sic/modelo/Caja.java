package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.controller.Views;

@Entity
@Table(name = "caja")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idCaja", "sucursal"})
@ToString(exclude = {"usuarioAbreCaja", "usuarioCierraCaja"})
@JsonIgnoreProperties({"sucursal", "usuarioAbreCaja", "usuarioCierraCaja", "eliminada"})
@JsonView(Views.Comprador.class)
public class Caja implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Caja")
  private long idCaja;

  @NotNull(message = "{mensaje_caja_fecha_apertura_vacia}")
  private LocalDateTime fechaApertura;

  private LocalDateTime fechaCierre;

  @ManyToOne
  @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
  @NotNull(message = "{mensaje_caja_sucursal_vacia}")
  private Sucursal sucursal;

  @OneToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @NotNull(message = "{mensaje_caja_usuario_vacio}")
  private Usuario usuarioAbreCaja;

  @OneToOne
  @JoinColumn(name = "id_UsuarioCierra", referencedColumnName = "id_Usuario")
  private Usuario usuarioCierraCaja;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EstadoCaja estado;

  @Column(precision = 25, scale = 15)
  private BigDecimal saldoApertura;

  @Column(precision = 25, scale = 15)
  private BigDecimal saldoSistema;

  @Column(precision = 25, scale = 15)
  private BigDecimal saldoReal;

  private boolean eliminada;

  @JsonGetter("idSucursal")
  public Long getIdSucursal() {
    return sucursal.getIdSucursal();
  }

  @JsonGetter("nombreSucursal")
  public String getNombreSucursal() {
    return sucursal.getNombre();
  }

  @JsonGetter("idUsuarioAbreCaja")
  public Long getIdUsuarioAbreCaja() {
    return usuarioAbreCaja.getIdUsuario();
  }

  @JsonGetter("nombreUsuarioAbreCaja")
  public String getNombreUsuarioAbreCaja() {
    if (usuarioAbreCaja != null) {
      return usuarioAbreCaja.getNombre()
        + " "
        + usuarioAbreCaja.getApellido()
        + " ("
        + usuarioAbreCaja.getUsername()
        + ")";
    } else {
      return null;
    }
  }

  @JsonGetter("idUsuarioCierraCaja")
  public Long getIdUsuarioCierraCaja() {
    return (usuarioCierraCaja != null) ? usuarioCierraCaja.getIdUsuario() : null;
  }

  @JsonGetter("nombreUsuarioCierraCaja")
  public String getNombreUsuarioCierraCaja() {
    if (usuarioCierraCaja != null) {
      return usuarioCierraCaja.getNombre()
        + " "
        + usuarioCierraCaja.getApellido()
        + " ("
        + usuarioCierraCaja.getUsername()
        + ")";
    } else {
      return null;
    }
  }

}
