package sic.service;

import io.jsonwebtoken.Claims;
import sic.modelo.Rol;

import java.util.List;

public interface AuthService {

  String generarJWT(Long idUsuario, List<Rol> rolesDeUsuario);

  boolean esAuthorizationHeaderValido(String authorizationHeader);

  boolean esJWTValido(String token);

  boolean noEsTokenExcluido(String token);

  Claims getClaimsDelToken(String authorizationHeader);

  void validarRecaptcha(String recaptcha);

  void excluirTokenAcceso(String authorizationHeader);
}
