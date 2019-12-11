package sic.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sic.exception.UnauthorizedException;
import sic.modelo.ReCaptchaResponse;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.exception.BusinessServiceException;
import sic.service.IAuthService;
import sic.service.IUsuarioService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class AuthServiceImpl implements IAuthService {

  private final IUsuarioService usuarioService;
  private final RestTemplate restTemplate;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String URL_RECAPTCHA = "https://www.google.com/recaptcha/api/siteverify";
  private final MessageSource messageSource;

  @Value("${RECAPTCHA_SECRET_KEY}")
  private String recaptchaSecretkey;

  @Value("${RECAPTCHA_TEST_KEY}")
  private String recaptchaTestKey;

  @Value("${SIC_JWT_KEY}")
  private String secretkey;

  @Autowired
  public AuthServiceImpl(
      IUsuarioService usuarioService, RestTemplate restTemplate, MessageSource messageSource) {
    this.usuarioService = usuarioService;
    this.restTemplate = restTemplate;
    this.messageSource = messageSource;
  }

  @Override
  public String generarToken(long idUsuario, List<Rol> rolesDeUsuario) {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneYear = today.plusYears(1L).atZone(ZoneId.systemDefault());
    return Jwts.builder()
        .setIssuedAt(Date.from(zdtNow.toInstant()))
        .setExpiration(Date.from(zdtInOneYear.toInstant()))
        .signWith(SignatureAlgorithm.HS512, secretkey)
        .claim("idUsuario", idUsuario)
        .claim("roles", rolesDeUsuario)
            //app
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
      logger.warn(messageSource.getMessage(
        "mensaje_usuario_no_habilitado", null, Locale.getDefault()));
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
      throw new UnauthorizedException(messageSource.getMessage(
        "mensaje_error_token_invalido", null, Locale.getDefault()));
    }
    return Jwts.parser()
        .setSigningKey(secretkey)
        .parseClaimsJws(authorizationHeader.substring(7))
        .getBody();
  }

  @Override
  public void validarRecaptcha(String recaptcha) {
    if (!recaptcha.equals(recaptchaTestKey)) {
      String params = "?secret=" + recaptchaSecretkey + "&response=" + recaptcha;
      ReCaptchaResponse reCaptchaResponse = null;
      try {
        reCaptchaResponse =
            restTemplate
                .exchange(URL_RECAPTCHA + params, HttpMethod.POST, null, ReCaptchaResponse.class)
                .getBody();
      } catch (RestClientException ex) {
        logger.error(ex.getMessage());
      }
      if (reCaptchaResponse == null || !reCaptchaResponse.isSuccess()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_recaptcha_no_valido", null, Locale.getDefault()));
      }
    }
  }
}
