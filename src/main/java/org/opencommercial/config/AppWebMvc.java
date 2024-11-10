package org.opencommercial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.opencommercial.interceptor.JwtInterceptor;

@Configuration
public class AppWebMvc implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(this.getJwtInterceptor())
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

  @Bean
  public JwtInterceptor getJwtInterceptor() {
    return new JwtInterceptor();
  }
}
