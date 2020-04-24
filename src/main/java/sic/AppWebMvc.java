package sic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sic.interceptor.JwtInterceptor;

@Configuration
public class AppWebMvc implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(this.jwtInterceptor())
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/*/login")
        .excludePathPatterns("/api/*/logout")
        .excludePathPatterns("/api/*/password-recovery")
        .excludePathPatterns("/api/*/registracion")
        .excludePathPatterns("/api/*/pagos/mercado-pago/notificacion")
        .excludePathPatterns("/api/*/productos/*")
        .excludePathPatterns("/api/*/productos/busqueda/criteria");
  }

  @Bean
  public JwtInterceptor jwtInterceptor() {
    return new JwtInterceptor();
  }
}
