package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sic.config.Views;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"username", "email"})
@ToString(exclude = {"roles", "password"})
@JsonView(Views.Comprador.class)
@JsonIgnoreProperties({
  "token",
  "passwordRecoveryKey",
  "passwordRecoveryKeyExpirationDate",
  "eliminado"
})
public class Usuario implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_Usuario")
  private long idUsuario;

  @Column(nullable = false)
  @NotEmpty(message = "{mensaje_usuario_vacio_username}")
  private String username;

  @Column(nullable = false)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NotEmpty(message = "{mensaje_usuario_vacio_password}")
  private String password;

  @Column(nullable = false)
  @NotEmpty(message = "{mensaje_usuario_vacio_nombre}")
  private String nombre;

  @Column(nullable = false)
  @NotEmpty(message = "{mensaje_usuario_vacio_apellido}")
  private String apellido;

  @Column(nullable = false)
  @Email(message = "{mensaje_usuario_invalido_email}")
  private String email;

  private long idSucursalPredeterminada;

  private String passwordRecoveryKey;

  private LocalDateTime passwordRecoveryKeyExpirationDate;

  @ElementCollection(targetClass = Rol.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "rol", joinColumns = @JoinColumn(name = "id_Usuario"))
  @Enumerated(EnumType.STRING)
  @Column(name = "nombre")
  @NotEmpty(message = "{mensaje_usuario_no_selecciono_rol}")
  private List<Rol> roles;

  @JsonView(Views.Administrador.class)
  private boolean habilitado;

  private boolean eliminado;
}
