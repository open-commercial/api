package sic.service.impl;

import io.jsonwebtoken.*;
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
import sic.modelo.*;
import sic.exception.BusinessServiceException;
import sic.repository.TokenAccesoExcluidoRepository;
import sic.service.IAuthService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class AuthServiceImpl implements IAuthService {

  private final RestTemplate restTemplate;
  private final TokenAccesoExcluidoRepository tokenAccesoExcluidoRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String URL_RECAPTCHA = "https://www.google.com/recaptcha/api/siteverify";
  private static final String BEARER_TOKEN_PREFIX = "Bearer";

  private long activeUserID;
  private final MessageSource messageSource;

  @Value("${RECAPTCHA_SECRET_KEY}")
  private String recaptchaSecretKey;

  @Value("${RECAPTCHA_TEST_KEY}")
  private String recaptchaTestKey;

  @Value("${SIC_JWT_KEY}")
  private String jwtSecretKey;

  @Autowired
  public AuthServiceImpl(
      RestTemplate restTemplate,
      MessageSource messageSource,
      TokenAccesoExcluidoRepository tokenAccesoExcluidoRepository) {
    this.restTemplate = restTemplate;
    this.messageSource = messageSource;
    this.tokenAccesoExcluidoRepository = tokenAccesoExcluidoRepository;
  }

  @Override
  public String generarJWT(long idUsuario, List<Rol> rolesDeUsuario) {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    return Jwts.builder()
        .setIssuedAt(Date.from(zdtNow.toInstant()))
        .setExpiration(Date.from(zdtInOneMonth.toInstant()))
        .signWith(SignatureAlgorithm.HS512, jwtSecretKey)
        .claim("idUsuario", idUsuario)
        .claim("roles", rolesDeUsuario)
        .compact();
  }

  @Override
  public boolean esAuthorizationHeaderValido(String authorizationHeader) {
    String token = authorizationHeader.substring(7);
    return authorizationHeader.startsWith(BEARER_TOKEN_PREFIX)
        && this.esJWTValido(token)
        && this.noEsTokenExcluido(token);
  }

  @Override
  public boolean esJWTValido(String token) {
    if (token == null || token.isEmpty()) return false;
    try {
      Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token);
      return true;
    } catch (JwtException ex) {
      logger.error(ex.getMessage());
      return false;
    }
  }

  @Override
  public boolean noEsTokenExcluido(String token) {
    return tokenAccesoExcluidoRepository.findByToken(token) == null;
  }

  @Override
  public Claims getClaimsDelToken(String authorizationHeader) {
    if (this.esAuthorizationHeaderValido(authorizationHeader)) {
      return Jwts.parser()
          .setSigningKey(jwtSecretKey)
          .parseClaimsJws(authorizationHeader.substring(7))
          .getBody();
    } else {
      throw new UnauthorizedException(
          messageSource.getMessage("mensaje_error_token_invalido", null, Locale.getDefault()));
    }
  }

  @Override
  public void validarRecaptcha(String recaptcha) {
    if (!recaptcha.equals(recaptchaTestKey)) {
      String params = "?secret=" + recaptchaSecretKey + "&response=" + recaptcha;
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

  @Override
  public void excluirTokenAcceso(String authorizationHeader) {
    String token = authorizationHeader.substring(7);
    if (tokenAccesoExcluidoRepository.findByToken(token) == null) {
      tokenAccesoExcluidoRepository.save(new TokenAccesoExcluido(0, token));
    }
  }

  @Override
  public void setActiveUserToken(String token) {
     activeUserID = this.getClaimsDelToken(token).get("idUsuario", Integer.class);
  }

  @Override
  public long getActiveUserId() {
    return activeUserID;
  }
}
