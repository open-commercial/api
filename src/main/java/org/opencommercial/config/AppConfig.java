package org.opencommercial.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.opencommercial.model.Cliente;
import org.opencommercial.model.Ubicacion;
import org.opencommercial.model.dto.ClienteDTO;
import org.opencommercial.model.dto.UbicacionDTO;
import org.opencommercial.service.AfipWebServiceSOAPClient;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Clock;

@Configuration
public class AppConfig {

  @Bean
  public SimpleModule springDataPageModule() {
    return new SimpleModule()
            .addSerializer(
                    Page.class,
                    new ValueSerializer<Page>() {

                      @Override
                      public void serialize(Page value, JsonGenerator gen, SerializationContext sc) throws JacksonException {
                        gen.writeStartObject();
                        gen.writeNumberProperty("totalElements", value.getTotalElements());
                        gen.writeNumberProperty("totalPages", value.getTotalPages());
                        gen.writeNumberProperty("number", value.getNumber());
                        gen.writeNumberProperty("numberOfElements", value.getNumberOfElements());
                        gen.writeNumberProperty("size", value.getSize());
                        gen.writeBooleanProperty("first", value.isFirst());
                        gen.writeBooleanProperty("last", value.isLast());
                        sc.defaultSerializeProperty("content", value.getContent(), gen);
                        gen.writePOJOProperty("sort", value.getSort());
                        gen.writeEndObject();
                      }
                    });
  }

  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <generatePackage> specified in pom.xml
    marshaller.setContextPaths("afip.wsaa.wsdl", "afip.wsfe.wsdl");
    return marshaller;
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
}
