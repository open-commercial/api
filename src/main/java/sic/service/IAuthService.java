package sic.service;

import io.jsonwebtoken.Claims;
import sic.modelo.Aplicacion;
import sic.modelo.Rol;

import java.util.Set;

public interface IAuthService {

  String generarToken(long idUsuario, Aplicacion aplicacion, Set<Rol> rolesDeUsuario);

  boolean esAuthorizationHeaderValido(String authorizationHeader);

  boolean esTokenValido(String authorizationHeader);

  Claims getClaimsDelToken(String authorizationHeader);

  void validarRecaptcha(String recaptcha);
}
