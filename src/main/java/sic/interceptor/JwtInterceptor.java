package sic.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import sic.exception.UnauthorizedException;
import sic.service.IAuthService;

import java.util.ResourceBundle;

public class JwtInterceptor extends HandlerInterceptorAdapter {

  @Autowired private IAuthService authService;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!request.getMethod().equals("OPTIONS")) {
      final String authorizationHeader = request.getHeader("Authorization");
      if (!authService.esAuthorizationHeaderValido(authorizationHeader)) {
        throw new UnauthorizedException(RESOURCE_BUNDLE.getString("mensaje_error_token_invalido"));
      }
      request.setAttribute("claims", authService.getClaimsDelToken(authorizationHeader));
    }
    return true;
  }
}
