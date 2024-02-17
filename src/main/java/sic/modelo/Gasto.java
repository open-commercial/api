package sic.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.config.Views;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "gasto")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nroGasto", "sucursal"})
@ToString
@JsonIgnoreProperties({"eliminado", "usuario"})
@JsonView(Views.Comprador.class)
public class Gasto implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Gasto")
  private long idGasto;

  private long nroGasto;

  @NotNull(message = "{mensaje_gasto_fecha_vacia}")
  private LocalDateTime fecha;

  @Column(nullable = false)
  @NotNull
  @NotEmpty(message = "{mensaje_gasto_concepto_vacio}")
  private String concepto;

  @ManyToOne
  @JoinColumn(name = "idSucursal", referencedColumnName = "idSucursal")
  @QueryInit("ubicacion.localidad.provincia")
  @NotNull(message = "{mensaje_gasto_sucursal_vacia}")
  private Sucursal sucursal;

  @OneToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @NotNull(message = "{mensaje_gasto_usuario_vacio}")
  private Usuario usuario;

  @OneToOne
  @JoinColumn(name = "id_FormaDePago", referencedColumnName = "id_FormaDePago")
  @NotNull(message = "{mensaje_gasto_forma_de_pago_vacia}")
  private FormaDePago formaDePago;

  @Column(precision = 25, scale = 15)
  @DecimalMin(value = "0", message = "{mensaje_gasto_negativo_cero}")
  private BigDecimal monto;

  private boolean eliminado;

  @JsonGetter("idSucursal")
  public Long getIdSucursal() {
    return sucursal.getIdSucursal();
  }

  @JsonGetter("nombreSucursal")
  public String getNombreSucursal() {
    return sucursal.getNombre();
  }

  @JsonGetter("idFormaDePago")
  public Long getIdFormaDePago() {
    return formaDePago.getIdFormaDePago();
  }

  @JsonGetter("nombreFormaDePago")
  public String getNombreFormaDePago() {
    return formaDePago.getNombre();
  }

  @JsonGetter("idUsuario")
  public Long getIdCredencial() {
    return usuario.getIdUsuario();
  }

  @JsonGetter("nombreUsuario")
  public String getNombreUsuario() {
    return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
  }
}
