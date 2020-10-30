package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;
import sic.modelo.Rol;
import sic.service.IAuthService;
import java.util.List;

@RestControllerAdvice
public class SecurityJsonViewControllerAdvice extends AbstractMappingJacksonResponseBodyAdvice {

  private final IAuthService authService;

  @Autowired
  public SecurityJsonViewControllerAdvice(IAuthService authService) {
    this.authService = authService;
  }

  @Override
  protected void beforeBodyWriteInternal(
      MappingJacksonValue mappingJacksonValue,
      MediaType mediaType,
      MethodParameter methodParameter,
      ServerHttpRequest serverHttpRequest,
      ServerHttpResponse serverHttpResponse) {

    List<String> headers = serverHttpRequest.getHeaders().get("Authorization");
    if (headers != null && !headers.isEmpty()) {
      Claims claims = authService.getClaimsDelToken(headers.get(0));
      List rolesDelUsuario = claims.get("roles", List.class);
      if (rolesDelUsuario != null && !rolesDelUsuario.isEmpty()) {
        if (rolesDelUsuario.contains(Rol.ADMINISTRADOR.name())) {
          mappingJacksonValue.setSerializationView(Views.Administrador.class);
        } else if (rolesDelUsuario.contains(Rol.ENCARGADO.name())) {
          mappingJacksonValue.setSerializationView(Views.Encargado.class);
        } else if (rolesDelUsuario.contains(Rol.VENDEDOR.name())) {
          mappingJacksonValue.setSerializationView(Views.Vendedor.class);
        } else if (rolesDelUsuario.contains(Rol.VIAJANTE.name())) {
          mappingJacksonValue.setSerializationView(Views.Viajante.class);
        } else if (rolesDelUsuario.contains(Rol.COMPRADOR.name())) {
          mappingJacksonValue.setSerializationView(Views.Comprador.class);
        }
      }
    } else {
      mappingJacksonValue.setSerializationView(Views.Comprador.class);
    }
  }
}
