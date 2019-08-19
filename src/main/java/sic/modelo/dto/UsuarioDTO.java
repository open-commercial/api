package sic.modelo.dto;

import lombok.*;
import sic.modelo.Rol;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"username", "email", "roles"})
@Builder
public class UsuarioDTO implements Serializable {

  private long id_Usuario;
  private String username;
  private String password;
  private String nombre;
  private String apellido;
  private String email;
  private String token;
  private long idSucursalPredeterminada;
  private long passwordRecoveryKey;
  private List<Rol> roles;
  private boolean habilitado;
  private boolean eliminado;
}
