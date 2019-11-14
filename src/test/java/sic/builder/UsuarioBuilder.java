package sic.builder;

import java.time.LocalDateTime;
import java.util.*;

import sic.modelo.Rol;
import sic.modelo.Usuario;

public class UsuarioBuilder {

  private long idUsuario = 0L;
  private String username = "daenta";
  private String password = "LaQueNoArde";
  private String nombre = "Daenerys";
  private String apellido = "Targaryen";
  private String email = "daenerys@gmail.com";
  private String token = null;
  private String passwordRecoveryKey = "";
  private LocalDateTime passwordRecoveryKeyExpirationDate = LocalDateTime.now();
  private List<Rol> roles = Collections.singletonList(Rol.ADMINISTRADOR);
  private boolean habilitado = true;
  private boolean eliminado = false;
  private long idSucursal = 0L;

  public Usuario build() {
    return new Usuario(
      idUsuario,
        username,
        password,
        nombre,
        apellido,
        email,
        token,
      idSucursal,
        passwordRecoveryKey,
        passwordRecoveryKeyExpirationDate,
        roles,
        habilitado,
        eliminado);
  }

  public UsuarioBuilder withIdUsuario(long idUsuario) {
    this.idUsuario = idUsuario;
    return this;
  }

  public UsuarioBuilder withUsername(String username) {
    this.username = username;
    return this;
  }

  public UsuarioBuilder withPassword(String password) {
    this.password = password;
    return this;
  }

  public UsuarioBuilder withNombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  public UsuarioBuilder withApellido(String apellido) {
    this.apellido = apellido;
    return this;
  }

  public UsuarioBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public UsuarioBuilder withToken(String token) {
    this.token = token;
    return this;
  }

  public UsuarioBuilder withidSucursal(long idSucursal) {
    this.idSucursal = idSucursal;
    return this;
  }

  public UsuarioBuilder withPasswordRecoveryKey(String passwordRecoveryKey) {
    this.passwordRecoveryKey = passwordRecoveryKey;
    return this;
  }

  public UsuarioBuilder withPasswordRecoveryKeyExpirationDate(
      LocalDateTime passwordRecoveryKeyExpirationDate) {
    this.passwordRecoveryKeyExpirationDate = passwordRecoveryKeyExpirationDate;
    return this;
  }

  public UsuarioBuilder withRol(ArrayList<Rol> roles) {
    this.roles = roles;
    return this;
  }

  public UsuarioBuilder withHabilitado(boolean habilitado) {
    this.habilitado = habilitado;
    return this;
  }

  public UsuarioBuilder withEliminado(boolean eliminado) {
    this.eliminado = eliminado;
    return this;
  }
}
