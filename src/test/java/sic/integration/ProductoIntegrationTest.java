package sic.integration;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
import sic.modelo.dto.ProductoDTO;
import sic.repository.UsuarioRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductoIntegrationTest {

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
  }

  @Test
  public void testCalcularPreciosDeProductosConRegargo() {
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
    Empresa empresa =
        new EmpresaBuilder()
            .withLocalidad(
                restTemplate.postForObject(apiPrefix + "/localidades", localidad, Localidad.class))
            .build();
    empresa = restTemplate.postForObject(apiPrefix + "/empresas", empresa, Empresa.class);
    FormaDePago formaDePago =
        new FormaDePagoBuilder()
            .withAfectaCaja(false)
            .withEmpresa(empresa)
            .withPredeterminado(true)
            .withNombre("Efectivo")
            .build();
    restTemplate.postForObject(apiPrefix + "/formas-de-pago", formaDePago, FormaDePago.class);
    Medida medida = new MedidaBuilder().withEmpresa(empresa).build();
    medida = restTemplate.postForObject(apiPrefix + "/medidas", medida, Medida.class);
    Proveedor proveedor =
        new ProveedorBuilder()
            .withEmpresa(empresa)
            .withLocalidad(empresa.getLocalidad())
            .build();
    proveedor = restTemplate.postForObject(apiPrefix + "/proveedores", proveedor, Proveedor.class);
    Rubro rubro = new RubroBuilder().withEmpresa(empresa).build();
    rubro = restTemplate.postForObject(apiPrefix + "/rubros", rubro, Rubro.class);
    ProductoDTO productoUno =
        new ProductoBuilder()
            .withCodigo("1")
            .withDescripcion("uno")
            .withCantidad(BigDecimal.TEN)
            .withVentaMinima(BigDecimal.ONE)
            .withPrecioCosto(CIEN)
            .withGanancia_porcentaje(new BigDecimal("900"))
            .withGanancia_neto(new BigDecimal("900"))
            .withPrecioVentaPublico(new BigDecimal("1000"))
            .withIva_porcentaje(new BigDecimal("21.0"))
            .withIva_neto(new BigDecimal("210"))
            .withPrecioLista(new BigDecimal("1210"))
            .build();
    ProductoDTO productoDos =
        new ProductoBuilder()
            .withCodigo("2")
            .withDescripcion("dos")
            .withCantidad(new BigDecimal("6"))
            .withVentaMinima(BigDecimal.ONE)
            .withPrecioCosto(CIEN)
            .withGanancia_porcentaje(new BigDecimal("900"))
            .withGanancia_neto(new BigDecimal("900"))
            .withPrecioVentaPublico(new BigDecimal("1000"))
            .withIva_porcentaje(new BigDecimal("10.5"))
            .withIva_neto(new BigDecimal("105"))
            .withPrecioLista(new BigDecimal("1105"))
            .build();
    productoUno =
        restTemplate.postForObject(
            apiPrefix
                + "/productos?idMedida="
                + medida.getId_Medida()
                + "&idRubro="
                + rubro.getId_Rubro()
                + "&idProveedor="
                + proveedor.getId_Proveedor()
                + "&idEmpresa="
                + empresa.getId_Empresa(),
            productoUno,
            ProductoDTO.class);
    productoDos =
        restTemplate.postForObject(
            apiPrefix
                + "/productos?idMedida="
                + medida.getId_Medida()
                + "&idRubro="
                + rubro.getId_Rubro()
                + "&idProveedor="
                + proveedor.getId_Proveedor()
                + "&idEmpresa="
                + empresa.getId_Empresa(),
            productoDos,
            ProductoDTO.class);
    String uri =
        apiPrefix
            + "/productos/disponibilidad-stock?idProducto="
            + productoUno.getId_Producto()
            + ","
            + productoDos.getId_Producto()
            + "&cantidad=10,6";
    Assert.assertTrue(
        restTemplate
            .exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<Double, Producto>>() {})
            .getBody()
            .isEmpty());
    uri = apiPrefix + "/productos/multiples?idProducto=1,2&descuentoRecargoPorcentaje=10";
    restTemplate.put(uri, null);
    productoUno = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoDos = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertTrue(
        "El precio de costo no sufrió el cambio esperado.",
        productoUno.getPrecioCosto().compareTo(new BigDecimal("110")) == 0);
    assertTrue(
        "La ganacia neta no sufrió el cambio esperado.",
        productoUno.getGananciaNeto().compareTo(new BigDecimal("990")) == 0);
    assertTrue(
        "El pvp no sufrió el cambio esperado.",
        productoUno.getPrecioVentaPublico().compareTo(new BigDecimal("1100")) == 0);
    assertTrue(
        "El IVA neto no sufrió el cambio esperado.",
        productoUno.getIvaNeto().compareTo(new BigDecimal("231")) == 0);
    assertTrue(
        "El precio de lista no sufrió el cambio esperado.",
        productoUno.getPrecioLista().compareTo(new BigDecimal("1331")) == 0);
    assertTrue(
        "El precio de costo no sufrió el cambio esperado.",
        productoDos.getPrecioCosto().compareTo(new BigDecimal("110")) == 0);
    assertTrue(
        "La ganacia neta no sufrió el cambio esperado.",
        productoDos.getGananciaNeto().compareTo(new BigDecimal("990")) == 0);
    assertTrue(
        "El pvp no sufrió no sufrió el cambio esperado.",
        productoDos.getPrecioVentaPublico().compareTo(new BigDecimal("1100")) == 0);
    assertTrue(
        "El IVA neto no sufrió el cambio esperado.",
        productoDos.getIvaNeto().compareTo(new BigDecimal("115.5")) == 0);
    assertTrue(
        "El precio de lista no sufrió el cambio esperado.",
        productoDos.getPrecioLista().compareTo(new BigDecimal("1215.5")) == 0);
  }
}
