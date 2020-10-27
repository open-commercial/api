package sic.service;

import io.jsonwebtoken.Claims;
import sic.modelo.Rol;
import java.util.List;

public interface IAuthService {

  String generarJWT(long idUsuario, List<Rol> rolesDeUsuario);

  boolean esAuthorizationHeaderValido(String authorizationHeader);

  boolean esJWTValido(String token);

  boolean noEsTokenExcluido(String token);

  Claims getClaimsDelJWT(String authorizationHeader);

  void validarRecaptcha(String recaptcha);

  void excluirTokenAcceso(String authorizationHeader);
}
