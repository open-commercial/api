package sic.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sic.controller.UnauthorizedException;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.service.IAuthService;
import sic.service.IUsuarioService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@Service
public class AuthServiceImpl implements IAuthService {

  private final IUsuarioService usuarioService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Value("${SIC_JWT_KEY}")
  private String secretkey;

  @Autowired
  public AuthServiceImpl(IUsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @Override
  public String generarToken(long idUsuario, List<Rol> rolesDeUsuario) {
    Date today = new Date();
    Calendar c = Calendar.getInstance();
    c.setTime(today);
    c.add(Calendar.YEAR, 1);
    Date yearLater = c.getTime();
    return Jwts.builder()
        .setIssuedAt(today)
        .setExpiration(yearLater)
        .signWith(SignatureAlgorithm.HS512, secretkey)
        .claim("idUsuario", idUsuario)
        .claim("roles", rolesDeUsuario)
        .compact();
  }

  @Override
  public boolean esAuthorizationHeaderValido(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return false;
    }
    final long idUsuario;
    idUsuario = (int) this.getClaimsDelToken(authorizationHeader).get("idUsuario");
    Usuario usuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    final String token = authorizationHeader.substring(7); // The part after "Bearer "
    if (!token.equalsIgnoreCase(usuario.getToken())) {
      return false;
    }
    if (!usuario.isHabilitado()) {
      logger.warn(RESOURCE_BUNDLE.getString("mensaje_usuario_no_habilitado"));
      return false;
    }
    return true;
  }

  @Override
  public boolean esTokenValido(String token) {
    if (token == null || token.isEmpty()) return false;
    try {
      Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token);
      return true;
    } catch (JwtException ex) {
      logger.error(ex.getMessage());
      return false;
    }
  }

  @Override
  public Claims getClaimsDelToken(String authorizationHeader) {
    if (authorizationHeader == null
        || !authorizationHeader.startsWith("Bearer ")
        || !this.esTokenValido(authorizationHeader.substring(7))) { // The part after "Bearer "
      throw new UnauthorizedException(RESOURCE_BUNDLE.getString("mensaje_error_token_invalido"));
    }
    return Jwts.parser()
        .setSigningKey(secretkey)
        .parseClaimsJws(authorizationHeader.substring(7))
        .getBody();
  }
}
