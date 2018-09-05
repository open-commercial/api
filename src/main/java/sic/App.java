package sic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import sic.interceptor.JwtInterceptor;
import sic.service.impl.AfipWebServiceSOAPClient;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class App extends WebMvcConfigurerAdapter {

    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.jwtInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/*/login")
                .excludePathPatterns("/api/*/password-recovery")
                .excludePathPatterns("/api/*/public/**");
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
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod(HttpMethod.PUT);
        config.addAllowedMethod(HttpMethod.DELETE);
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
