package org.opencommercial.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opencommercial.exception.UnauthorizedException;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;

@Component
public class JwtInterceptor implements HandlerInterceptor {

  private final AuthService authService;
  private final UsuarioService usuarioService;
  private final MessageSource messageSource;
  private static final String AUTHORIZATION_HEADER = "Authorization";

  @Autowired
  public JwtInterceptor(AuthService authService, UsuarioService usuarioService, MessageSource messageSource) {
    this.authService = authService;
    this.usuarioService = usuarioService;
    this.messageSource = messageSource;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!request.getMethod().equals("OPTIONS")) {
      final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
      if (authorizationHeader == null || !authService.esAuthorizationHeaderValido(authorizationHeader)) {
        throw new UnauthorizedException(
                messageSource.getMessage("mensaje_error_token_invalido", null, Locale.getDefault()));
      }
      var claims = authService.getClaimsDelToken(authorizationHeader);
      if (!usuarioService.esUsuarioHabilitado(claims.get("idUsuario", Long.class))) {
        throw new UnauthorizedException(
                messageSource.getMessage("mensaje_usuario_no_habilitado", null, Locale.getDefault()));
      }
      if (!authService.usuarioTieneRoles(claims)) {
        throw new UnauthorizedException(
                messageSource.getMessage("mensaje_usuario_sin_roles", null, Locale.getDefault()));
      }
    }
    return true;
  }
}
