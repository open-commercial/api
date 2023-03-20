package sic.model;

import lombok.*;
import sic.domain.Rol;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"username", "email", "roles"})
public class Usuario {

  private long idUsuario;
  private String username;
  private String password;
  private String nombre;
  private String apellido;
  private String email;
  private String token;
  private long idSucursalPredeterminada;
  private String passwordRecoveryKey;
  private List<Rol> roles;
  private boolean habilitado;
  private boolean eliminado;
}
