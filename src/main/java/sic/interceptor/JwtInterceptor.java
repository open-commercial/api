package sic.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import sic.service.IAuthService;

public class JwtInterceptor extends HandlerInterceptorAdapter {

  @Autowired private IAuthService authService;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getMethod().equals("OPTIONS")) return true;
    final String authorizationHeader = request.getHeader("Authorization");
    authService.validarToken(authorizationHeader);
    request.setAttribute("claims", authService.getClaimsDelToken(authorizationHeader));
    return true;
  }
}
