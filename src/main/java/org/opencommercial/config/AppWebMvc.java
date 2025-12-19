package org.opencommercial.config;

import org.opencommercial.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppWebMvc implements WebMvcConfigurer {

  private final JwtInterceptor jwtInterceptor;

  @Autowired
  public AppWebMvc(JwtInterceptor jwtInterceptor) {
    this.jwtInterceptor = jwtInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/*/login")
            .excludePathPatterns("/api/*/logout")
            .excludePathPatterns("/api/*/password-recovery")
            .excludePathPatterns("/api/*/registracion")
            .excludePathPatterns("/api/*/pagos/mercado-pago/notificacion")
            .excludePathPatterns("/api/*/productos/*/sucursales/*")
            .excludePathPatterns("/api/*/productos/busqueda/criteria/sucursales/*")
            .excludePathPatterns("/api/*/productos/*/sucursales/*/recomendados")
            .excludePathPatterns("/api/*/rubros/*")
            .excludePathPatterns("/api/*/rubros");
  }
}
