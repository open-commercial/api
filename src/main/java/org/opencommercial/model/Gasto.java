package org.opencommercial.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.core.annotations.QueryInit;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.opencommercial.config.Views;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
  @JoinColumn(name = "idSucursal")
  @QueryInit("ubicacion.localidad.provincia")
  @NotNull(message = "{mensaje_gasto_sucursal_vacia}")
  private Sucursal sucursal;

  @ManyToOne
  @JoinColumn(name = "id_Usuario")
  @NotNull(message = "{mensaje_gasto_usuario_vacio}")
  private Usuario usuario;

  @ManyToOne
  @JoinColumn(name = "id_FormaDePago")
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
