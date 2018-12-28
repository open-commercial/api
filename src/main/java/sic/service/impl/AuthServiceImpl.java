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
    // 24hs desde la fecha actual para expiration
    Date today = new Date();
    Calendar c = Calendar.getInstance();
    c.setTime(today);
    c.add(Calendar.DATE, 1);
    Date tomorrow = c.getTime();
    return Jwts.builder()
        .setIssuedAt(today)
        .setExpiration(tomorrow)
        .signWith(SignatureAlgorithm.HS512, secretkey)
        .claim("idUsuario", idUsuario)
        .claim("roles", rolesDeUsuario)
        .compact();
  }

  @Override
  public void validarToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException(RESOURCE_BUNDLE.getString("mensaje_error_token_invalido"));
    }
    final long idUsuario;
    idUsuario = (int) this.getClaimsDelToken(authorizationHeader).get("idUsuario");
    Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
    final String token = authorizationHeader.substring(7); // The part after "Bearer "
    if (!token.equalsIgnoreCase(usuario.getToken())) {
      throw new UnauthorizedException(RESOURCE_BUNDLE.getString("mensaje_error_token_invalido"));
    }
    if (!usuario.isHabilitado()) {
      logger.warn(RESOURCE_BUNDLE.getString("mensaje_usuario_no_habilitado"));
      throw new UnauthorizedException(RESOURCE_BUNDLE.getString("mensaje_error_token_invalido"));
    }
  }

  @Override
  public Claims getClaimsDelToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException(RESOURCE_BUNDLE.getString("mensaje_error_token_invalido"));
    }
    final String token = authorizationHeader.substring(7); // The part after "Bearer "
    try {
      return Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token).getBody();
    } catch (JwtException ex) {
      throw new UnauthorizedException(
          RESOURCE_BUNDLE.getString("mensaje_error_token_invalido"), ex);
    }
  }
}
