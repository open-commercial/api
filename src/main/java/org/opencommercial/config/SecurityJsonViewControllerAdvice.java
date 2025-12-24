package org.opencommercial.config;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.opencommercial.model.Rol;
import org.opencommercial.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class SecurityJsonViewControllerAdvice implements ResponseBodyAdvice<Object> {

  private final AuthService authService;
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String CLAIM_ROLES = "roles";

  @Autowired
  public SecurityJsonViewControllerAdvice(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public boolean supports(@NonNull MethodParameter returnType,
                          @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    return converterType == JacksonJsonHttpMessageConverter.class;
  }

  @Override
  public Map<@NonNull String, @NonNull Object> determineWriteHints(
          Object body,
          @NonNull MethodParameter returnType,
          @NonNull MediaType selectedContentType,
          @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType) {

    if (body == null) {
      return null;
    }

    var httpRequest = this.getCurrentHttpRequest();
    if (httpRequest == null) {
      return null;
    }

    var viewClass = this.determineViewClass(httpRequest);
    if (viewClass != null) {
      return Map.of(JsonView.class.getName(), viewClass);
    }

    return null;
  }

  @Override
  public Object beforeBodyWrite(
          Object body,
          @NonNull MethodParameter returnType,
          @NonNull MediaType selectedContentType,
          @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
          @NonNull ServerHttpRequest request,
          @NonNull ServerHttpResponse response) {
    return body;
  }

  private Class<?> determineViewClass(HttpServletRequest httpServletRequest) {
    var headers = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
    if (headers != null && !headers.isEmpty()) {
      try {
        var claims = authService.getClaimsDelToken(headers);
        var rolesDelUsuario = claims.get(CLAIM_ROLES, List.class);
        if (rolesDelUsuario != null && !rolesDelUsuario.isEmpty()) {
          if (rolesDelUsuario.contains(Rol.ADMINISTRADOR.name())) {
            return Views.Administrador.class;
          } else if (rolesDelUsuario.contains(Rol.ENCARGADO.name())) {
            return Views.Encargado.class;
          } else if (rolesDelUsuario.contains(Rol.VENDEDOR.name())) {
            return Views.Vendedor.class;
          } else if (rolesDelUsuario.contains(Rol.VIAJANTE.name())) {
            return Views.Viajante.class;
          } else if (rolesDelUsuario.contains(Rol.COMPRADOR.name())) {
            return Views.Comprador.class;
          }
        }
      } catch (Exception ex) {
        return Views.Comprador.class;
      }
    }

    return Views.Comprador.class;
  }

  private HttpServletRequest getCurrentHttpRequest() {
    return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .filter(ServletRequestAttributes.class::isInstance)
            .map(ServletRequestAttributes.class::cast)
            .map(ServletRequestAttributes::getRequest)
            .orElse(null);
  }
}

