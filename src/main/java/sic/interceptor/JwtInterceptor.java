package sic.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.HandlerInterceptor;
import sic.exception.UnauthorizedException;
import sic.service.AuthService;
import sic.service.UsuarioService;

import java.util.Locale;

public class JwtInterceptor implements HandlerInterceptor {

  @Autowired private AuthService authService;
  @Autowired private UsuarioService usuarioService;
  @Autowired private MessageSource messageSource;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!request.getMethod().equals("OPTIONS")) {
      final String authorizationHeader = request.getHeader("Authorization");
      if (authorizationHeader == null || !authService.esAuthorizationHeaderValido(authorizationHeader)) {
        throw new UnauthorizedException(
            messageSource.getMessage("mensaje_error_token_invalido", null, Locale.getDefault()));
      }
      long idUsuario = authService.getClaimsDelToken(authorizationHeader).get("idUsuario", Long.class);
      if (!usuarioService.esUsuarioHabilitado(idUsuario)) {
        throw new UnauthorizedException(
            messageSource.getMessage("mensaje_usuario_no_habilitado", null, Locale.getDefault()));
      }
    }
    return true;
  }
}
