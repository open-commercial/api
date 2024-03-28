package sic.aspect;

import io.jsonwebtoken.Claims;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sic.exception.ForbiddenException;
import sic.modelo.Rol;
import sic.service.IAuthService;
import java.util.List;
import java.util.Locale;

@Aspect
@Component
public class AuthAspect {

  private final IAuthService authService;
  private final MessageSource messageSource;

  @Autowired
  public AuthAspect(IAuthService authService, MessageSource messageSource) {
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @Before("@annotation(AccesoRolesPermitidos)")
  public void autorizarAcceso(AccesoRolesPermitidos AccesoRolesPermitidos) {
    var request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    String authorizationHeader = request.getHeader("Authorization");
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Rol[] rolesRequeridos = AccesoRolesPermitidos.value();
    var rolesDelUsuario = claims.get("roles", List.class);
    boolean accesoDenegado = true;
    for (Rol rolRequerido : rolesRequeridos) {
      if (rolesDelUsuario.contains(rolRequerido.toString())) accesoDenegado = false;
    }
    if (accesoDenegado)
      throw new ForbiddenException(
          messageSource.getMessage("mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
  }
}
