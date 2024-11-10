package org.opencommercial.integration.model;

import lombok.*;
import org.opencommercial.model.Rol;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"username", "email", "roles"})
public class UsuarioTest {

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
