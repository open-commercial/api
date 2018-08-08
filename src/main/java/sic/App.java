package sic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import sic.interceptor.JwtInterceptor;
import sic.service.impl.AfipWebServiceSOAPClient;

@SpringBootApplication
@EnableScheduling
public class App extends WebMvcConfigurerAdapter {

    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/*/login");
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

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
