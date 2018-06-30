package sic.aspect;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sic.controller.ForbiddenException;
import sic.modelo.Rol;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ResourceBundle;

@Aspect
@Component
public class AuthAspect {

  @Value("${SIC_JWT_KEY}")
  private String secretkey;

  @Before("@annotation(AccesoRolesPermitidos)")
  public void autorizarAcceso(AccesoRolesPermitidos AccesoRolesPermitidos) {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    String token = request.getHeader("Authorization");
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    Rol[] rolesRequeridos = AccesoRolesPermitidos.value();
    List rolesDelUsuario = claims.get("roles", List.class);
    boolean accesoDenegado = true;
    for (Rol rolRequerido : rolesRequeridos) {
      if (rolesDelUsuario.contains(rolRequerido.toString())) accesoDenegado = false;
    }
    if (accesoDenegado)
      throw new ForbiddenException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_rol_no_valido"));
  }
}
