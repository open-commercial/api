package sic.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sic.controller.Views;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "traspaso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonView(Views.Vendedor.class)
@JsonIgnoreProperties({"sucursalOrigen", "sucursalDestino", "renglones"})
public class Traspaso {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idTraspaso;

  private LocalDateTime fechaDeAlta;

  private String nroTraspaso;

  private Long nroPedido;

  @ManyToOne
  @JoinColumn(name = "idSucursalOrigen", referencedColumnName = "idSucursal")
  @NotNull
  private Sucursal sucursalOrigen;

  @ManyToOne
  @JoinColumn(name = "idSucursalDestino", referencedColumnName = "idSucursal")
  @NotNull
  private Sucursal sucursalDestino;

  @ManyToOne
  @JoinColumn(name = "id_Usuario", referencedColumnName = "id_Usuario")
  @NotNull(message = "{mensaje_factura_usuario_vacio}")
  private Usuario usuario;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "idTraspaso")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotEmpty
  private List<RenglonTraspaso> renglones;

  @JsonGetter("idSucursalOrigen")
  public Long getIdSucursalOrigen() {
    return sucursalOrigen.getIdSucursal();
  }

  @JsonGetter("nombreSucursalOrigen")
  public String getNombreSucursalOrigen() {
    return sucursalOrigen.getNombre();
  }

  @JsonGetter("idSucursalDestino")
  public Long getIdSucursalDestino() {
    return sucursalDestino.getIdSucursal();
  }

  @JsonGetter("nombreSucursalDestino")
  public String getNombreSucursalDestino() {
    return sucursalDestino.getNombre();
  }

  @JsonGetter("nombreUsuario")
  public String getNombreUsuario() {
    return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getUsername() + ")";
  }
}
