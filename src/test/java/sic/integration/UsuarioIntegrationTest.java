package sic.integration;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import sic.builder.*;
import sic.modelo.*;
import sic.modelo.dto.ClienteDTO;
import sic.modelo.dto.UsuarioDTO;
import sic.repository.UsuarioRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UsuarioIntegrationTest {

  @Autowired private UsuarioRepository usuarioRepository;

  @Autowired private TestRestTemplate restTemplate;

  private String token;

  private final String apiPrefix = "/api/v1";

  private static final BigDecimal IVA_21 = new BigDecimal("21");
  private static final BigDecimal IVA_105 = new BigDecimal("10.5");
  private static final BigDecimal CIEN = new BigDecimal("100");

  @Before
  public void setup() {
    String md5Test = "098f6bcd4621d373cade4e832627b4f6";
    usuarioRepository.save(
        new UsuarioBuilder()
            .withUsername("test")
            .withPassword(md5Test)
            .withNombre("test")
            .withApellido("test")
            .withHabilitado(true)
            .build());
    // Interceptor de RestTemplate para JWT
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(
        (ClientHttpRequestInterceptor)
            (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
              request.getHeaders().set("Authorization", "Bearer " + token);
              return execution.execute(request, body);
            });
    restTemplate.getRestTemplate().setInterceptors(interceptors);
    // ErrorHandler para RestTemplate
    restTemplate
        .getRestTemplate()
        .setErrorHandler(
            new ResponseErrorHandler() {
              @Override
              public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus.Series series = response.getStatusCode().series();
                return (HttpStatus.Series.CLIENT_ERROR.equals(series)
                    || HttpStatus.Series.SERVER_ERROR.equals(series));
              }

              @Override
              public void handleError(ClientHttpResponse response) throws IOException {
                String mensaje = IOUtils.toString(response.getBody());
                throw new RestClientResponseException(
                    mensaje,
                    response.getRawStatusCode(),
                    response.getStatusText(),
                    response.getHeaders(),
                    null,
                    Charset.defaultCharset());
              }
            });
    // set enviroment
    this.token =
        restTemplate
            .postForEntity(apiPrefix + "/login", new Credencial("test", "test"), String.class)
            .getBody();
    Localidad localidad = new LocalidadBuilder().build();
    localidad
        .getProvincia()
        .setPais(
            restTemplate.postForObject(
                apiPrefix + "/paises", localidad.getProvincia().getPais(), Pais.class));
    localidad.setProvincia(
        restTemplate.postForObject(
            apiPrefix + "/provincias", localidad.getProvincia(), Provincia.class));
    localidad = restTemplate.postForObject(apiPrefix + "/localidades", localidad, Localidad.class);
    Empresa empresa = new EmpresaBuilder().withLocalidad(localidad).build();
    empresa = restTemplate.postForObject(apiPrefix + "/empresas", empresa, Empresa.class);
    FormaDePago formaDePago =
        new FormaDePagoBuilder()
            .withAfectaCaja(false)
            .withEmpresa(empresa)
            .withPredeterminado(true)
            .withNombre("Efectivo")
            .build();
    restTemplate.postForObject(apiPrefix + "/formas-de-pago", formaDePago, FormaDePago.class);
    Transportista transportista =
        new TransportistaBuilder()
            .withEmpresa(empresa)
            .withLocalidad(empresa.getLocalidad())
            .build();
    restTemplate.postForObject(apiPrefix + "/transportistas", transportista, Transportista.class);
    Medida medidaMetro = new MedidaBuilder().withEmpresa(empresa).build();
    Medida medidaKilo = new MedidaBuilder().withNombre("Kilo").withEmpresa(empresa).build();
    restTemplate.postForObject(apiPrefix + "/medidas", medidaMetro, Medida.class);
    restTemplate.postForObject(apiPrefix + "/medidas", medidaKilo, Medida.class);
    Proveedor proveedor =
        new ProveedorBuilder().withEmpresa(empresa).withLocalidad(empresa.getLocalidad()).build();
    restTemplate.postForObject(apiPrefix + "/proveedores", proveedor, Proveedor.class);
    Rubro rubro = new RubroBuilder().withEmpresa(empresa).build();
    restTemplate.postForObject(apiPrefix + "/rubros", rubro, Rubro.class);
  }

  @Test
  public void shouldCrearUsuario() {
    UsuarioDTO nuevoUsuario = UsuarioDTO.builder()
      .username("wicca")
      .password("Salem123")
      .nombre("Sabrina")
      .apellido("Spellman")
      .email("Witch@gmail.com")
      .roles(Collections.singletonList(Rol.ENCARGADO))
      .habilitado(true)
      .build();
    restTemplate.postForObject(apiPrefix + "/usuarios", nuevoUsuario, UsuarioDTO.class);
    UsuarioDTO usuarioRecuperado = restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    nuevoUsuario.setId_Usuario(usuarioRecuperado.getId_Usuario());
    nuevoUsuario.setPassword(usuarioRecuperado.getPassword());
    nuevoUsuario.setRoles(usuarioRecuperado.getRoles());
    assertEquals(nuevoUsuario, usuarioRecuperado);
  }

  @Test
  public void shouldModificarUsuario() {
    this.shouldCrearUsuario();
    UsuarioDTO usuarioRecuperado = restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    usuarioRecuperado.setUsername("darkmagic");
    Rol[] roles = new Rol[] {Rol.ADMINISTRADOR, Rol.ENCARGADO};
    usuarioRecuperado.setRoles(Arrays.asList(roles));
    restTemplate.put(apiPrefix + "/usuarios", usuarioRecuperado);
    UsuarioDTO usuarioModificado = restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    assertEquals(usuarioRecuperado.getRoles(), usuarioModificado.getRoles());
    assertEquals(usuarioRecuperado.getUsername(), usuarioModificado.getUsername());
  }

  @Test(expected = RestClientResponseException.class)
  public void shouldValidarPermisosUsuarioAlEliminarProveedor() {
    UsuarioDTO nuevoUsuario = UsuarioDTO.builder()
      .username("wicca")
      .password("Salem123")
      .nombre("Sabrina")
      .apellido("Spellman")
      .email("Witch@gmail.com")
      .roles(Collections.singletonList(Rol.VENDEDOR))
      .habilitado(true)
      .build();
    restTemplate.postForObject(apiPrefix + "/usuarios", nuevoUsuario, UsuarioDTO.class);
    this.token =
      restTemplate
        .postForEntity(apiPrefix + "/login", new Credencial("wicca", "Salem123"), String.class)
        .getBody();
    restTemplate.delete(apiPrefix + "/proveedores/1");
  }


}
