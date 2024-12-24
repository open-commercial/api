package org.opencommercial.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.exception.UnauthorizedException;
import org.opencommercial.model.ReCaptchaResponse;
import org.opencommercial.model.Rol;
import org.opencommercial.model.TokenAccesoExcluido;
import org.opencommercial.repository.TokenAccesoExcluidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final RestTemplate restTemplate;
  private final TokenAccesoExcluidoRepository tokenAccesoExcluidoRepository;
  private static final String URL_RECAPTCHA = "https://www.google.com/recaptcha/api/siteverify";
  private static final String BEARER_TOKEN_PREFIX = "Bearer";
  private static final String ALGORITHM_SHA512 = "HmacSHA512";
  private final MessageSource messageSource;

  @Value("${RECAPTCHA_SECRET_KEY}")
  private String recaptchaSecretKey;

  @Value("${RECAPTCHA_TEST_KEY}")
  private String recaptchaTestKey;

  @Value("${API_JWT_KEY}")
  private String jwtSecretKey;

  @Autowired
  public AuthServiceImpl(RestTemplate restTemplate,
                         MessageSource messageSource,
                         TokenAccesoExcluidoRepository tokenAccesoExcluidoRepository) {
    this.restTemplate = restTemplate;
    this.messageSource = messageSource;
    this.tokenAccesoExcluidoRepository = tokenAccesoExcluidoRepository;
  }

  @Override
  public String generarJWT(Long idUsuario, List<Rol> rolesDeUsuario) {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtExpiration = today.plusWeeks(1L).atZone(ZoneId.systemDefault());
    var privateKey = new SecretKeySpec(Base64.getDecoder().decode(jwtSecretKey), ALGORITHM_SHA512);
    return Jwts.builder()
            .issuedAt(Date.from(zdtNow.toInstant()))
            .expiration(Date.from(zdtExpiration.toInstant()))
            .signWith(privateKey, Jwts.SIG.HS512)
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
    var privateKey = new SecretKeySpec(Base64.getDecoder().decode(jwtSecretKey), ALGORITHM_SHA512);
    try {
      Jwts.parser().verifyWith(privateKey).build().parse(token);
      return true;
    } catch (JwtException ex) {
      log.warn(ex.getMessage());
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
      var privateKey = new SecretKeySpec(Base64.getDecoder().decode(jwtSecretKey), ALGORITHM_SHA512);
      return Jwts.parser()
              .verifyWith(privateKey)
              .build()
              .parseSignedClaims(authorizationHeader.substring(7))
              .getPayload();
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
        log.warn(ex.getMessage());
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
      tokenAccesoExcluidoRepository.save(TokenAccesoExcluido.builder().token(token).build());
    }
  }
}
