package sic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sic.interceptor.JwtInterceptor;
import sic.modelo.Cliente;
import sic.modelo.Ubicacion;
import sic.modelo.dto.ClienteDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.service.impl.AfipWebServiceSOAPClient;
import java.io.IOException;
import java.time.Clock;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class App implements WebMvcConfigurer {

  @Bean
  public JwtInterceptor jwtInterceptor() {
    return new JwtInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(this.jwtInterceptor())
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/*/login")
        .excludePathPatterns("/api/*/logout")
        .excludePathPatterns("/api/*/password-recovery")
        .excludePathPatterns("/api/*/registracion")
        .excludePathPatterns("/api/*/pagos/notificacion")
        .excludePathPatterns("/api/*/productos/*")
        .excludePathPatterns("/api/*/productos/busqueda/criteria");
  }

  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <generatePackage> specified in pom.xml
    marshaller.setContextPaths("afip.wsaa.wsdl", "afip.wsfe.wsdl");
    return marshaller;
  }

  @Bean
  public Module springDataPageModule() {
    return new SimpleModule()
        .addSerializer(
            Page.class,
            new JsonSerializer<Page>() {
              @Override
              public void serialize(Page value, JsonGenerator gen, SerializerProvider serializers)
                  throws IOException {
                gen.writeStartObject();
                gen.writeNumberField("totalElements", value.getTotalElements());
                gen.writeNumberField("totalPages", value.getTotalPages());
                gen.writeNumberField("number", value.getNumber());
                gen.writeNumberField("numberOfElements", value.getNumberOfElements());
                gen.writeNumberField("size", value.getSize());
                gen.writeBooleanField("first", value.isFirst());
                gen.writeBooleanField("last", value.isLast());
                gen.writeFieldName("content");
                serializers.defaultSerializeValue(value.getContent(), gen);
                gen.writeObjectField("sort", value.getSort());
                gen.writeEndObject();
              }
            });
  }

  @Bean
  public AfipWebServiceSOAPClient afipWebServiceSOAPClient(Jaxb2Marshaller marshaller) {
    AfipWebServiceSOAPClient afipClient = new AfipWebServiceSOAPClient();
    afipClient.setMarshaller(marshaller);
    afipClient.setUnmarshaller(marshaller);
    return afipClient;
  }

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
    config.addAllowedMethod(HttpMethod.PUT);
    config.addAllowedMethod(HttpMethod.DELETE);
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
    bean.setOrder(0);
    return bean;
  }

  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    PropertyMap<Ubicacion, UbicacionDTO> ubicacionMapping =
        new PropertyMap<Ubicacion, UbicacionDTO>() {
          protected void configure() {
            map(source.getLocalidad().getNombre(), destination.getNombreLocalidad());
          }
        };
    PropertyMap<Cliente, ClienteDTO> clienteMapping =
        new PropertyMap<Cliente, ClienteDTO>() {
          protected void configure() {
            map(source.getViajante().getNombre(), destination.getNombreViajante());
            map(source.getCredencial().getNombre(), destination.getNombreCredencial());
          }
        };
    modelMapper.addMappings(ubicacionMapping);
    modelMapper.addMappings(clienteMapping);
    return modelMapper;
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

  // https://stackoverflow.com/questions/53968789/not-able-to-access-message-source-in-spring-boot-2-1-1-release
  @Bean
  public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Bean
  @Override
  public LocalValidatorFactoryBean getValidator() {
    LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
    factoryBean.setValidationMessageSource(this.messageSource());
    return factoryBean;
  }

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
