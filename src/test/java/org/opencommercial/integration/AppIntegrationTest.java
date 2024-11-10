package org.opencommercial.integration;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.integration.model.*;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.*;
import org.opencommercial.model.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(locations = "/application.properties")
class AppIntegrationTest {

  String token;
  static final BigDecimal CIEN = new BigDecimal("100");

  @Autowired TestRestTemplate restTemplate;
  @Autowired MessageSource messageSource;

  @Container
  @ServiceConnection
  static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.3.0");

  void iniciarSesionComoAdministrador() {
    this.token = restTemplate.postForEntity("/api/v1/login",
                                            new Credencial("dueño", "dueño123"),
                                            String.class)
                              .getBody();
    assertNotNull(this.token);
  }

  @BeforeEach
  void setup() {
    // Interceptor de RestTemplate para JWT
    var interceptors = new ArrayList<ClientHttpRequestInterceptor>();
    interceptors.add(
            (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
              request.getHeaders().set("Authorization", "Bearer " + token);
              return execution.execute(request, body);
            });
    restTemplate.getRestTemplate().setInterceptors(interceptors);
    restTemplate.getRestTemplate()
                .setErrorHandler(
                    new ResponseErrorHandler() {
                      @Override
                      public boolean hasError(@NotNull ClientHttpResponse response) throws IOException {
                        return (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
                      }

                      @Override
                      public void handleError(@NotNull ClientHttpResponse response) throws IOException {
                        String mensaje = IOUtils.toString(response.getBody(), Charset.defaultCharset());
                        throw new RestClientResponseException(
                                mensaje,
                                response.getStatusCode(),
                                response.getStatusText(),
                                response.getHeaders(),
                                null,
                                Charset.defaultCharset());
                      }
                    });
  }

  @Test
  @DisplayName("Iniciar actividad con una nueva sucursal y un usuario Administrador")
  @Order(1)
  void iniciarActividadComercial() {
    this.token = restTemplate.postForEntity("/api/v1/login",
                    new Credencial("test", "test"),
                    String.class)
            .getBody();
    UsuarioTest usuario =
        UsuarioTest.builder()
            .username("dueño")
            .password("dueño123")
            .nombre("Max")
            .apellido("Power")
            .email("liderDeLaEmpresa@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.ADMINISTRADOR)))
            .build();
    usuario = restTemplate.postForObject("/api/v1/usuarios", usuario, UsuarioTest.class);
    usuario.setHabilitado(true);
    restTemplate.put("/api/v1/usuarios", usuario);
    this.iniciarSesionComoAdministrador();
    var nuevaSucursal =
            NuevaSucursalTest.builder()
                    .nombre("FirstOfAll")
                    .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
                    .email("support@globocorporation.com")
                    .idFiscal(20311023188L)
                    .ubicacion(UbicacionDTO.builder().idLocalidad(1L).idProvincia(1L).build())
                    .build();
    var sucursalRecuperada = restTemplate.postForObject("/api/v1/sucursales", nuevaSucursal, SucursalTest.class);
    assertEquals(nuevaSucursal.getNombre(), sucursalRecuperada.getNombre());
    assertEquals(nuevaSucursal.getLema(), sucursalRecuperada.getLema());
    assertEquals(nuevaSucursal.getCategoriaIVA(), sucursalRecuperada.getCategoriaIVA());
    assertEquals(nuevaSucursal.getIdFiscal(), sucursalRecuperada.getIdFiscal());
    assertEquals(0, sucursalRecuperada.getIngresosBrutos());
    assertEquals(nuevaSucursal.getFechaInicioActividad(), sucursalRecuperada.getFechaInicioActividad());
    assertEquals(nuevaSucursal.getEmail(), sucursalRecuperada.getEmail());
    assertEquals(nuevaSucursal.getTelefono(), sucursalRecuperada.getTelefono());
    assertEquals(nuevaSucursal.getUbicacion().getIdLocalidad(), sucursalRecuperada.getUbicacion().getIdLocalidad());
    ConfiguracionSucursal configuracionSucursal =
        restTemplate.getForObject("/api/v1/configuraciones-sucursal/" + sucursalRecuperada.getIdSucursal(),
            ConfiguracionSucursal.class);
    configuracionSucursal.setPuntoDeRetiro(true);
    configuracionSucursal.setPredeterminada(true);
    restTemplate.put("/api/v1/configuraciones-sucursal", configuracionSucursal);
    configuracionSucursal = restTemplate.getForObject(
            "/api/v1/configuraciones-sucursal/" + sucursalRecuperada.getIdSucursal(),
            ConfiguracionSucursal.class);
    assertTrue(configuracionSucursal.isPuntoDeRetiro());
    assertTrue(configuracionSucursal.isPredeterminada());
    configuracionSucursal.setFacturaElectronicaHabilitada(false);
    configuracionSucursal.setNroPuntoDeVentaAfip(2);
    restTemplate.put("/api/v1/configuraciones-sucursal", configuracionSucursal);
    ConfiguracionSucursal configuracionSucursalActualizada =
        restTemplate.getForObject("/api/v1/configuraciones-sucursal/" + sucursalRecuperada.getIdSucursal(),
            ConfiguracionSucursal.class);
    assertEquals(configuracionSucursal, configuracionSucursalActualizada);
  }

  @Test
  @DisplayName("Abrir caja con $1000 en efectivo y registrar un gasto por $500 con transferencia")
  @Order(2)
  void testEscenarioAbrirCaja() {
    this.iniciarSesionComoAdministrador();
    var cajaAbierta = restTemplate.postForObject(
            "/api/v1/cajas/apertura/sucursales/1?saldoApertura=1000", null, Caja.class);
    assertEquals(EstadoCaja.ABIERTA, cajaAbierta.getEstado());
    assertEquals(new BigDecimal("1000"), cajaAbierta.getSaldoApertura());
    NuevoGastoDTO nuevoGasto =
        NuevoGastoDTO.builder()
            .monto(new BigDecimal("500"))
            .concepto("Pago de Agua")
            .idFormaDePago(1L)
            .idSucursal(1L)
            .build();
    List<SucursalTest> sucursales = Arrays.asList(restTemplate.getForObject("/api/v1/sucursales", SucursalTest[].class));
    assertFalse(sucursales.isEmpty());
    assertEquals(1, sucursales.size());
    restTemplate.postForObject("/api/v1/gastos", nuevoGasto, Gasto.class);
    BusquedaGastoCriteria criteria = BusquedaGastoCriteria.builder().idSucursal(1L).build();
    HttpEntity<BusquedaGastoCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<Gasto> resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/gastos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<Gasto>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    List<Gasto> gastosRecuperados = resultadoBusqueda.getContent();
    assertEquals(1, gastosRecuperados.size());
    assertEquals(new BigDecimal("500.000000000000000"), gastosRecuperados.getFirst().getMonto());
    assertEquals("Pago de Agua", gastosRecuperados.getFirst().getConcepto());
  }

  @Test
  @DisplayName("Comprar productos al proveedor RI con factura A y verificar saldo CC, luego saldar la CC con un cheque de 3ro")
  @Order(3)
  void testEscenarioCompraEscenario1() {
    this.iniciarSesionComoAdministrador();
    var proveedor =
        ProveedorTest.builder()
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .razonSocial("Chamaco S.R.L.")
            .build();
    var proveedorRecuperado = restTemplate.postForObject("/api/v1/proveedores", proveedor, ProveedorTest.class);
    assertEquals(proveedor, proveedorRecuperado);
    RubroTest rubro = RubroTest.builder().nombre("Ferreteria").build();
    RubroTest rubroDadoDeAlta = restTemplate.postForObject("/api/v1/rubros", rubro, RubroTest.class);
    assertEquals(rubro, rubroDadoDeAlta);
    var medida = MedidaTest.builder().nombre("Metro").build();
    var medidaDadaDeAlta = restTemplate.postForObject("/api/v1/medidas", medida, MedidaTest.class);
    assertEquals(medida, medidaDadaDeAlta);
    NuevoProductoDTO nuevoProductoUno =
        NuevoProductoDTO.builder()
            .descripcion("Ventilador de pie")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {put(1L, BigDecimal.TEN);}
                })
            .cantMinima(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .paraCatalogo(true)
            .build();
    NuevoProductoDTO nuevoProductoDos =
        NuevoProductoDTO.builder()
            .descripcion("Reflector led 100w")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {put(1L, new BigDecimal("9"));}
                })
            .cantMinima(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("10.5"))
            .ivaNeto(new BigDecimal("105"))
            .precioLista(new BigDecimal("1105"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .paraCatalogo(true)
            .build();
    NuevoProductoDTO nuevoProductoTres =
        NuevoProductoDTO.builder()
            .descripcion("Canilla Monocomando")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {put(1L, new BigDecimal("10"));}
                })
            .cantMinima(BigDecimal.ONE)
            .precioCosto(new BigDecimal("10859.73"))
            .gananciaPorcentaje(new BigDecimal("11.37"))
            .gananciaNeto(new BigDecimal("1234.751"))
            .precioVentaPublico(new BigDecimal("12094.481"))
            .ivaPorcentaje(new BigDecimal("10.5"))
            .ivaNeto(new BigDecimal("1269.921"))
            .precioLista(new BigDecimal("13364.402"))
            .porcentajeBonificacionPrecio(BigDecimal.TEN)
            .paraCatalogo(true)
            .build();
    SucursalTest sucursal = restTemplate.getForObject("/api/v1/sucursales/1", SucursalTest.class);
    restTemplate.postForObject(
            "/api/v1/productos?idMedida=" + medidaDadaDeAlta.getIdMedida()
            + "&idRubro=" + rubroDadoDeAlta.getIdRubro()
            + "&idProveedor=" + proveedorRecuperado.getIdProveedor()
            + "&idSucursal=" + sucursal.getIdSucursal(),
        nuevoProductoUno,
        Producto.class);
    BusquedaProductoCriteria criteria =
        BusquedaProductoCriteria.builder().descripcion("Ventilador").build();
    HttpEntity<BusquedaProductoCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<ProductoTest> resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/productos/busqueda/criteria/sucursales/1",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    List<ProductoTest> productosRecuperados = resultadoBusqueda.getContent();
    assertEquals("Ventilador de pie", productosRecuperados.getFirst().getDescripcion());
    assertEquals(new BigDecimal("10.000000000000000"), productosRecuperados.getFirst().getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.getFirst().getCantidadReservada());
    assertEquals("Metro", productosRecuperados.getFirst().getNombreMedida());
    assertEquals(new BigDecimal("100.000000000000000"), productosRecuperados.getFirst().getPrecioCosto());
    assertEquals(new BigDecimal("900.000000000000000"), productosRecuperados.getFirst().getGananciaPorcentaje());
    assertEquals(new BigDecimal("900.000000000000000"), productosRecuperados.getFirst().getGananciaNeto());
    assertEquals(new BigDecimal("1000.000000000000000"), productosRecuperados.getFirst().getPrecioVentaPublico());
    assertEquals(new BigDecimal("21.000000000000000"), productosRecuperados.getFirst().getIvaPorcentaje());
    assertEquals(new BigDecimal("210.000000000000000"), productosRecuperados.getFirst().getIvaNeto());
    assertEquals(new BigDecimal("1210.000000000000000"), productosRecuperados.getFirst().getPrecioLista());
    assertEquals("Ferreteria", productosRecuperados.getFirst().getNombreRubro());
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.getFirst().getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("20.000000000000000"), productosRecuperados.getFirst().getPorcentajeBonificacionPrecio());
    assertEquals(new BigDecimal("968.000000000000000"), productosRecuperados.getFirst().getPrecioBonificado());
    restTemplate.postForObject(
            "/api/v1/productos?idMedida=" + medidaDadaDeAlta.getIdMedida()
            + "&idRubro=" + rubroDadoDeAlta.getIdRubro()
            + "&idProveedor=" + proveedorRecuperado.getIdProveedor()
            + "&idSucursal=" + sucursal.getIdSucursal(),
        nuevoProductoDos,
        Producto.class);
    criteria = BusquedaProductoCriteria.builder().descripcion("Reflector").build();
    requestEntity = new HttpEntity<>(criteria);
    resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/productos/busqueda/criteria/sucursales/1",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    productosRecuperados = resultadoBusqueda.getContent();
    assertEquals("Reflector led 100w", productosRecuperados.getFirst().getDescripcion());
    assertEquals(new BigDecimal("9.000000000000000"), productosRecuperados.getFirst().getCantidadTotalEnSucursales());
    assertEquals("Metro", productosRecuperados.getFirst().getNombreMedida());
    assertEquals(new BigDecimal("100.000000000000000"), productosRecuperados.getFirst().getPrecioCosto());
    assertEquals(new BigDecimal("900.000000000000000"), productosRecuperados.getFirst().getGananciaPorcentaje());
    assertEquals(new BigDecimal("900.000000000000000"), productosRecuperados.getFirst().getGananciaNeto());
    assertEquals(new BigDecimal("1000.000000000000000"), productosRecuperados.getFirst().getPrecioVentaPublico());
    assertEquals(new BigDecimal("10.500000000000000"), productosRecuperados.getFirst().getIvaPorcentaje());
    assertEquals(new BigDecimal("105.000000000000000"), productosRecuperados.getFirst().getIvaNeto());
    assertEquals(new BigDecimal("1105.000000000000000"), productosRecuperados.getFirst().getPrecioLista());
    assertEquals("Ferreteria", productosRecuperados.getFirst().getNombreRubro());
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.getFirst().getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("20.000000000000000"), productosRecuperados.getFirst().getPorcentajeBonificacionPrecio());
    assertEquals(new BigDecimal("884.000000000000000"), productosRecuperados.getFirst().getPrecioBonificado());
    restTemplate.postForObject(
            "/api/v1/productos?idMedida=" + medidaDadaDeAlta.getIdMedida()
            + "&idRubro=" + rubroDadoDeAlta.getIdRubro()
            + "&idProveedor=" + proveedorRecuperado.getIdProveedor()
            + "&idSucursal=" + sucursal.getIdSucursal(),
        nuevoProductoTres,
        Producto.class);
    criteria = BusquedaProductoCriteria.builder().descripcion("Canilla").build();
    requestEntity = new HttpEntity<>(criteria);
    resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/productos/busqueda/criteria/sucursales/1",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    productosRecuperados = resultadoBusqueda.getContent();
    assertEquals("Canilla Monocomando", productosRecuperados.getFirst().getDescripcion());
    assertEquals(new BigDecimal("10.000000000000000"), productosRecuperados.getFirst().getCantidadTotalEnSucursales());
    assertEquals("Metro", productosRecuperados.getFirst().getNombreMedida());
    assertEquals(new BigDecimal("10859.730000000000000"), productosRecuperados.getFirst().getPrecioCosto());
    assertEquals(new BigDecimal("11.370000000000000"), productosRecuperados.getFirst().getGananciaPorcentaje());
    assertEquals(new BigDecimal("1234.751000000000000"), productosRecuperados.getFirst().getGananciaNeto());
    assertEquals(new BigDecimal("12094.481000000000000"), productosRecuperados.getFirst().getPrecioVentaPublico());
    assertEquals(new BigDecimal("10.500000000000000"), productosRecuperados.getFirst().getIvaPorcentaje());
    assertEquals(new BigDecimal("1269.921000000000000"), productosRecuperados.getFirst().getIvaNeto());
    assertEquals(new BigDecimal("13364.402000000000000"), productosRecuperados.getFirst().getPrecioLista());
    assertEquals("Ferreteria", productosRecuperados.getFirst().getNombreRubro());
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.getFirst().getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("10.000000000000000"), productosRecuperados.getFirst().getPorcentajeBonificacionPrecio());
    assertEquals(new BigDecimal("12027.961800000000000"), productosRecuperados.getFirst().getPrecioBonificado());
    List<NuevoRenglonFacturaDTO> nuevosRenglones = new ArrayList<>();
    criteria = BusquedaProductoCriteria.builder().build();
    requestEntity = new HttpEntity<>(criteria);
    resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/productos/busqueda/criteria/sucursales/1",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    productosRecuperados = resultadoBusqueda.getContent();
    NuevoRenglonFacturaDTO nuevoRenglon =
        NuevoRenglonFacturaDTO.builder()
            .cantidad(new BigDecimal("4"))
            .idProducto(productosRecuperados.get(2).getIdProducto())
            .bonificacion(new BigDecimal("20"))
            .build();
    nuevosRenglones.add(nuevoRenglon);
    nuevoRenglon =
        NuevoRenglonFacturaDTO.builder()
            .cantidad(new BigDecimal("3"))
            .idProducto(productosRecuperados.get(1).getIdProducto())
            .bonificacion(new BigDecimal("20"))
            .build();
    nuevosRenglones.add(nuevoRenglon);
    NuevaFacturaCompraDTO nuevaFacturaCompraDTO =
        NuevaFacturaCompraDTO.builder()
            .idProveedor(1L)
            .idSucursal(1L)
            .tipoDeComprobante(TipoDeComprobante.FACTURA_A)
            .renglones(nuevosRenglones)
            .recargoPorcentaje(BigDecimal.TEN)
            .descuentoPorcentaje(new BigDecimal("25"))
            .fecha(LocalDateTime.now())
            .build();
    restTemplate.postForObject("/api/v1/facturas/compras", nuevaFacturaCompraDTO, FacturaCompra[].class);
    BusquedaFacturaCompraCriteria criteriaCompra =
        BusquedaFacturaCompraCriteria.builder()
            .idSucursal(1L)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .build();
    HttpEntity<BusquedaFacturaCompraCriteria> requestEntityCompra = new HttpEntity<>(criteriaCompra);
    PaginaRespuestaRest<FacturaCompraTest> resultadoBusquedaCompra =
        restTemplate
            .exchange(
                "/api/v1/facturas/compras/busqueda/criteria",
                HttpMethod.POST,
                requestEntityCompra,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaCompraTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaCompra);
    List<FacturaCompraTest> facturasRecuperadas = resultadoBusquedaCompra.getContent();
    assertEquals(1, facturasRecuperadas.size());
    assertEquals(new BigDecimal("560.000000000000000"), facturasRecuperadas.getFirst().getSubTotal());
    assertEquals(new BigDecimal("56.000000000000000"), facturasRecuperadas.getFirst().getRecargoNeto());
    assertEquals(new BigDecimal("140.000000000000000"), facturasRecuperadas.getFirst().getDescuentoNeto());
    assertEquals(new BigDecimal("476.000000000000000"), facturasRecuperadas.getFirst().getSubTotalBruto());
    assertEquals(new BigDecimal("21.420000000000000"), facturasRecuperadas.getFirst().getIva105Neto());
    assertEquals(new BigDecimal("57.120000000000000"), facturasRecuperadas.getFirst().getIva21Neto());
    assertEquals(new BigDecimal("554.540000000000000"), facturasRecuperadas.getFirst().getTotal());
    assertNotNull(facturasRecuperadas.getFirst().getFechaAlta());
    assertEquals(proveedorRecuperado.getRazonSocial(), facturasRecuperadas.getFirst().getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturasRecuperadas.getFirst().getNombreSucursal());
    assertEquals(new BigDecimal("-554.540000000000000"),
        restTemplate.getForObject("/api/v1/cuentas-corriente/proveedores/1/saldo", BigDecimal.class));
    var recibo =
        ReciboTest.builder()
            .monto(554.54)
            .concepto("Recibo para proveedor")
            .idSucursal(sucursal.getIdSucursal())
            .idProveedor(proveedorRecuperado.getIdProveedor())
            .idFormaDePago(2L)
            .build();
    var reciboRecuperado =
        restTemplate.postForObject("/api/v1/recibos/proveedores", recibo, ReciboTest.class);
    assertEquals(recibo, reciboRecuperado);
    assertEquals(
        0.0,
        restTemplate
            .getForObject("/api/v1/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
            .doubleValue());
    criteria = BusquedaProductoCriteria.builder().build();
    requestEntity = new HttpEntity<>(criteria);
    resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/productos/busqueda/criteria/sucursales/1",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    productosRecuperados = resultadoBusqueda.getContent();
    assertEquals(new BigDecimal("14.000000000000000"), productosRecuperados.get(2).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("12.000000000000000"), productosRecuperados.get(1).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("10.000000000000000"), productosRecuperados.getFirst().getCantidadTotalEnSucursales());
  }

  @Test
  @DisplayName("Dar de alta una nota de credito por una unidad fallada, chequear salgo CC y stock")
  @Order(4)
  void testEscenarioCompraEscenario2() {
    this.iniciarSesionComoAdministrador();
    assertEquals(0.0,
            restTemplate.getForObject("/api/v1/cuentas-corriente/proveedores/1/saldo",
            BigDecimal.class).doubleValue());
    BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder().descripcion("Ventilador").build();
    HttpEntity<BusquedaProductoCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<ProductoTest> resultadoBusqueda =
            restTemplate
                    .exchange(
                            "/api/v1/productos/busqueda/criteria/sucursales/1",
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
                    .getBody();
    assertNotNull(resultadoBusqueda);
    List<ProductoTest> productosRecuperados = resultadoBusqueda.getContent();
    assertEquals("Ventilador de pie", productosRecuperados.getFirst().getDescripcion());
    assertEquals(new BigDecimal("14.000000000000000"), productosRecuperados.getFirst().getCantidadTotalEnSucursales());
    BusquedaFacturaCompraCriteria criteriaCompra =
            BusquedaFacturaCompraCriteria.builder()
                    .idSucursal(1L)
                    .tipoComprobante(TipoDeComprobante.FACTURA_A)
                    .build();
    HttpEntity<BusquedaFacturaCompraCriteria> requestEntityCompra =
            new HttpEntity<>(criteriaCompra);
    PaginaRespuestaRest<FacturaCompra> resultadoBusquedaCompra =
            restTemplate
                    .exchange(
                            "/api/v1/facturas/compras/busqueda/criteria",
                            HttpMethod.POST,
                            requestEntityCompra,
                            new ParameterizedTypeReference<PaginaRespuestaRest<FacturaCompra>>() {})
                    .getBody();
    assertNotNull(resultadoBusquedaCompra);
    List<FacturaCompra> facturasRecuperadas = resultadoBusquedaCompra.getContent();
    assertEquals(1, facturasRecuperadas.size());
    List<RenglonFacturaTest> renglonesFacturaCompra = Arrays.asList(restTemplate.getForObject("/api/v1/facturas/" +
            facturasRecuperadas.getFirst().getIdFactura() + "/renglones", RenglonFacturaTest[].class));
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDeFacturaDTO = NuevaNotaCreditoDeFacturaDTO
            .builder()
            .idFactura(facturasRecuperadas.getFirst().getIdFactura())
            .cantidades(new BigDecimal[]{BigDecimal.ONE})
            .idsRenglonesFactura(new Long[]{renglonesFacturaCompra.getFirst().getIdRenglonFactura()})
            .modificaStock(true)
            .motivo("Unidad Fallada")
            .build();
    var notaCredito = restTemplate.postForObject("/api/v1/notas/credito/factura", nuevaNotaCreditoDeFacturaDTO, NotaCreditoTest.class);
    assertEquals(Movimiento.COMPRA, notaCredito.getMovimiento());
    assertEquals(notaCredito.getIdFacturaCompra(), facturasRecuperadas.getFirst().getIdFactura());
    assertEquals(1L, notaCredito.getIdNota());
    assertEquals(82.28, restTemplate.getForObject("/api/v1/cuentas-corriente/proveedores/1/saldo",
            BigDecimal.class).doubleValue());
    resultadoBusqueda =
            restTemplate
                    .exchange(
                            "/api/v1/productos/busqueda/criteria/sucursales/1",
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
                    .getBody();
    assertNotNull(resultadoBusqueda);
    productosRecuperados = resultadoBusqueda.getContent();
    assertEquals("Ventilador de pie", productosRecuperados.getFirst().getDescripcion());
    assertEquals(new BigDecimal("13.000000000000000"), productosRecuperados.getFirst().getCantidadTotalEnSucursales());
  }

  @Test
  @DisplayName("Actualizar CC segun ND por mora, luego verificar saldo CC")
  @Order(5)
  void testEscenarioNotaDebito() {
    this.iniciarSesionComoAdministrador();
    BusquedaProveedorCriteria criteriaParaProveedores = BusquedaProveedorCriteria.builder().build();
    HttpEntity<BusquedaProveedorCriteria> requestEntityParaProveedores =
        new HttpEntity<>(criteriaParaProveedores);
    PaginaRespuestaRest<Proveedor> resultadoBusquedaProveedor =
        restTemplate
            .exchange(
                "/api/v1/proveedores/busqueda/criteria",
                HttpMethod.POST,
                requestEntityParaProveedores,
                new ParameterizedTypeReference<PaginaRespuestaRest<Proveedor>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaProveedor);
    List<Proveedor> proveedoresRecuperados = resultadoBusquedaProveedor.getContent();
    assertEquals(1, proveedoresRecuperados.size());
    BusquedaReciboCriteria criteriaParaRecibos =
        BusquedaReciboCriteria.builder()
            .idProveedor(proveedoresRecuperados.getFirst().getIdProveedor())
            .build();
    HttpEntity<BusquedaReciboCriteria> requestEntity = new HttpEntity<>(criteriaParaRecibos);
    PaginaRespuestaRest<Recibo> resultadoBusquedaRecibo =
        restTemplate
            .exchange(
                "/api/v1/recibos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<Recibo>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaRecibo);
    List<Recibo> recibosRecuperados = resultadoBusquedaRecibo.getContent();
    assertEquals(1, recibosRecuperados.size());
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
        NuevaNotaDebitoDeReciboDTO.builder()
            .idRecibo(recibosRecuperados.getFirst().getIdRecibo())
            .gastoAdministrativo(new BigDecimal("1500.00"))
            .motivo("No pagamos, la vida es así.")
            .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_A)
            .build();
//    NotaDebito notaDebitoCalculada =
//        restTemplate.postForObject(
//            "/api/v1/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebito.class);
//    NotaDebito notaDebitoGuardada =
        restTemplate.postForObject("/api/v1/notas/debito", nuevaNotaDebitoDeReciboDTO, NotaDebitoTest.class);
    BusquedaNotaCriteria criteriaNota =
            BusquedaNotaCriteria.builder()
                    .idSucursal(1L)
                    .tipoComprobante(TipoDeComprobante.NOTA_DEBITO_A)
                    .build();
    HttpEntity<BusquedaNotaCriteria> requestEntityNota = new HttpEntity<>(criteriaNota);
    PaginaRespuestaRest<NotaDebito> resultadoBusquedaNota =
            restTemplate.exchange("/api/v1/notas/debito/busqueda/criteria", HttpMethod.POST,
                    requestEntityNota, new ParameterizedTypeReference<PaginaRespuestaRest<NotaDebito>>() {})
                    .getBody();
    assertNotNull(resultadoBusquedaNota);
    List<NotaDebito> notasRecuperadas = resultadoBusquedaNota.getContent();
    List<RenglonNotaDebitoTest> renglones =
            Arrays.asList(
                    restTemplate.getForObject(
                            "/api/v1/notas/renglones/debito/" + notasRecuperadas.getFirst().getIdNota(),
                            RenglonNotaDebitoTest[].class));
    assertNotNull(renglones);
    assertEquals(2, renglones.size());
    // renglones
    assertEquals("Nº Recibo 2-1: Recibo para proveedor", renglones.getFirst().getDescripcion());
    assertEquals(new BigDecimal("554.540000000000000"), renglones.getFirst().getMonto());
    assertEquals(new BigDecimal("554.540000000000000"), renglones.getFirst().getImporteBruto());
    assertEquals(new BigDecimal("0E-15"), renglones.getFirst().getIvaPorcentaje());
    assertEquals(new BigDecimal("0E-15"), renglones.getFirst().getIvaNeto());
    assertEquals(new BigDecimal("554.540000000000000"), renglones.getFirst().getImporteNeto());
    assertEquals("Gasto Administrativo", renglones.get(1).getDescripcion());
    assertEquals(new BigDecimal("1239.669421487603306"), renglones.get(1).getMonto());
    assertEquals(new BigDecimal("1239.669421487603306"), renglones.get(1).getImporteBruto());
    assertEquals(new BigDecimal("21.000000000000000"), renglones.get(1).getIvaPorcentaje());
    assertEquals(new BigDecimal("260.330578512396694"), renglones.get(1).getIvaNeto());
    assertEquals(new BigDecimal("1500.000000000000000"), renglones.get(1).getImporteNeto());
  }

  @Test
  @DisplayName("Dar de alta un producto")
  @Order(6)
  void testEscenarioAltaDeProducto() {
    this.iniciarSesionComoAdministrador();
    var medidas = Arrays.asList(restTemplate.getForObject("/api/v1/medidas", MedidaTest[].class));
    assertFalse(medidas.isEmpty());
    assertEquals(1, medidas.size());
    var rubros = Arrays.asList(restTemplate.getForObject("/api/v1/rubros", RubroTest[].class));
    assertFalse(rubros.isEmpty());
    assertEquals(1, rubros.size());
    var sucursales = Arrays.asList(restTemplate.getForObject("/api/v1/sucursales", SucursalTest[].class));
    assertFalse(sucursales.isEmpty());
    assertEquals(1, sucursales.size());
    BusquedaProveedorCriteria criteriaParaProveedores = BusquedaProveedorCriteria.builder().build();
    HttpEntity<BusquedaProveedorCriteria> requestEntityParaProveedores = new HttpEntity<>(criteriaParaProveedores);
    PaginaRespuestaRest<Proveedor> resultadoBusquedaProveedor =
            restTemplate.exchange("/api/v1/proveedores/busqueda/criteria", HttpMethod.POST,
                    requestEntityParaProveedores, new ParameterizedTypeReference<PaginaRespuestaRest<Proveedor>>() {})
                    .getBody();
    assertNotNull(resultadoBusquedaProveedor);
    List<Proveedor> proveedoresRecuperados = resultadoBusquedaProveedor.getContent();
    assertEquals(1, proveedoresRecuperados.size());
    NuevoProductoDTO nuevoProductoCuatro =
        NuevoProductoDTO.builder()
            .descripcion("Corta Papas - Vegetales")
            .cantidadEnSucursal(
                    new HashMap<Long, BigDecimal>() {
                      {put(1L, BigDecimal.TEN);}
                    })
            .cantMinima(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("10.5"))
            .ivaNeto(new BigDecimal("105"))
            .precioLista(new BigDecimal("1105"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .publico(true)
            .paraCatalogo(true)
            .build();
    var productoGuardado =
        restTemplate.postForObject(
            "/api/v1/productos?idMedida=" + medidas.getFirst().getIdMedida()
                + "&idRubro=" + rubros.getFirst().getIdRubro()
                + "&idProveedor=" + proveedoresRecuperados.getFirst().getIdProveedor(),
            nuevoProductoCuatro,
            ProductoTest.class);
    assertNotNull(productoGuardado.getIdProducto());
  }

  @Test
  @DisplayName("Dar de alta un cliente y levantar un pedido con reserva, verificar stock")
  @Order(7)
  void testEscenarioAltaClienteYPedido() {
    this.iniciarSesionComoAdministrador();
    UsuarioTest credencial =
        UsuarioTest.builder()
            .username("elenanocanete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Canete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .habilitado(true)
            .build();
    var credencialDadaDeAlta = restTemplate.postForObject("/api/v1/usuarios", credencial, UsuarioTest.class);
    credencialDadaDeAlta.setHabilitado(true);
    assertEquals(credencial, credencialDadaDeAlta);
    var cliente =
        ClienteTest.builder()
            .montoCompraMinima(new BigDecimal("500"))
            .nombreFiscal("Juan Fernando Canete")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(30703176840L)
            .telefono("3785663322")
            .idCredencial(credencialDadaDeAlta.getIdUsuario())
            .ubicacionFacturacion(UbicacionTest.builder().idProvincia(1L).idLocalidad(1L).build())
            .email("correoparapagos@gmail.com")
            .puedeComprarAPlazo(true)
            .build();
    var clienteRecuperado = restTemplate.postForObject("/api/v1/clientes", cliente, ClienteTest.class);
    assertEquals(cliente, clienteRecuperado);
    var productoUno = restTemplate.getForObject("/api/v1/productos/1/sucursales/1", ProductoTest.class);
    var productoDos = restTemplate.getForObject("/api/v1/productos/2/sucursales/1", ProductoTest.class);
    assertEquals(new BigDecimal("13.000000000000000"), productoUno.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("12.000000000000000"), productoDos.getCantidadTotalEnSucursales());
    List<NuevoRenglonPedidoDTO> renglonesPedidoDTO = new ArrayList<>();
    renglonesPedidoDTO.add(
        NuevoRenglonPedidoDTO.builder()
            .idProductoItem(1L)
            .cantidad(new BigDecimal("5.000000000000000"))
            .build());
    renglonesPedidoDTO.add(
        NuevoRenglonPedidoDTO.builder()
            .idProductoItem(2L)
            .cantidad(new BigDecimal("2.000000000000000"))
            .build());
    PedidoDTO pedidoDTO =
        PedidoDTO.builder()
            .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
            .recargoPorcentaje(new BigDecimal("5"))
            .renglones(renglonesPedidoDTO)
            .idSucursal(1L)
            .idCliente(clienteRecuperado.getIdCliente())
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .build();
    var pedidoRecuperado = restTemplate.postForObject("/api/v1/pedidos", pedidoDTO, PedidoTest.class);
    productoUno = restTemplate.getForObject("/api/v1/productos/1/sucursales/1", ProductoTest.class);
    productoDos = restTemplate.getForObject("/api/v1/productos/2/sucursales/1", ProductoTest.class);
    assertEquals(new BigDecimal("8.000000000000000"), productoUno.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("10.000000000000000"), productoDos.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("5.000000000000000"), productoUno.getCantidadReservada());
    assertEquals(new BigDecimal("2.000000000000000"), productoDos.getCantidadReservada());
    assertEquals(new BigDecimal("5947.200000000000000"), pedidoRecuperado.getTotal());
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
    List<RenglonPedidoTest> renglonesDelPedido =
            Arrays.asList(restTemplate.getForObject(
                    "/api/v1/pedidos/" + pedidoRecuperado.getIdPedido() + "/renglones",
                    RenglonPedidoTest[].class));
    assertEquals(2, renglonesDelPedido.size());
    assertEquals("Ventilador de pie", renglonesDelPedido.getFirst().getDescripcionItem());
    assertEquals("Metro", renglonesDelPedido.getFirst().getMedidaItem());
    assertEquals(new BigDecimal("1210.000000000000000"), renglonesDelPedido.getFirst().getPrecioUnitario());
    assertEquals(new BigDecimal("5.000000000000000"), renglonesDelPedido.getFirst().getCantidad());
    assertEquals(new BigDecimal("20.000000000000000"), renglonesDelPedido.getFirst().getBonificacionPorcentaje());
    assertEquals(new BigDecimal("242.000000000000000"), renglonesDelPedido.getFirst().getBonificacionNeta());
    assertEquals(new BigDecimal("6050.000000000000000000000000000000"), renglonesDelPedido.getFirst().getImporteAnterior());
    assertEquals(new BigDecimal("4840.000000000000000000000000000000"), renglonesDelPedido.getFirst().getImporte());
    assertEquals("Reflector led 100w", renglonesDelPedido.get(1).getDescripcionItem());
    assertEquals("Metro", renglonesDelPedido.get(1).getMedidaItem());
    assertEquals(new BigDecimal("1105.000000000000000"), renglonesDelPedido.get(1).getPrecioUnitario());
    assertEquals(new BigDecimal("2.000000000000000"), renglonesDelPedido.get(1).getCantidad());
    assertEquals(new BigDecimal("20.000000000000000"), renglonesDelPedido.get(1).getBonificacionPorcentaje());
    assertEquals(new BigDecimal("221.000000000000000"), renglonesDelPedido.get(1).getBonificacionNeta());
    assertEquals(new BigDecimal("2210.000000000000000000000000000000"), renglonesDelPedido.get(1).getImporteAnterior());
    assertEquals(new BigDecimal("1768.000000000000000000000000000000"), renglonesDelPedido.get(1).getImporte());
  }

  @Test
  @DisplayName("Modificar el pedido agregando un nuevo producto y cambiando la cantidad de uno ya existente, reservando y verificando stock")
  @Order(8)
  void testEscenarioModificacionPedido() {
    this.iniciarSesionComoAdministrador();
    BusquedaPedidoCriteria criteria = BusquedaPedidoCriteria.builder().idSucursal(1L).build();
    HttpEntity<BusquedaPedidoCriteria> requestEntity = new HttpEntity<>(criteria);
    var resultadoBusquedaPedido =
        restTemplate
            .exchange(
                "/api/v1/pedidos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<PedidoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaPedido);
    var pedidosRecuperados = resultadoBusquedaPedido.getContent();
    assertEquals(1, pedidosRecuperados.size());
    List<RenglonPedidoTest> renglonesPedidos =
        Arrays.asList(
            restTemplate.getForObject(
                "/api/v1/pedidos/" + pedidosRecuperados.getFirst().getIdPedido() + "/renglones",
                RenglonPedidoTest[].class));
    assertNotNull(renglonesPedidos);
    assertEquals(2, renglonesPedidos.size());
    List<NuevoRenglonPedidoDTO> renglonesPedidoDTO = new ArrayList<>();
    renglonesPedidos.forEach(
        renglonPedido ->
            renglonesPedidoDTO.add(
                NuevoRenglonPedidoDTO.builder()
                    .idProductoItem(renglonPedido.getIdProductoItem())
                    .cantidad(renglonPedido.getCantidad())
                    .build()));
    renglonesPedidoDTO.get(1).setCantidad(new BigDecimal("3"));
    renglonesPedidoDTO.add(NuevoRenglonPedidoDTO.builder().idProductoItem(3L).cantidad(BigDecimal.TEN).build());
    PedidoDTO pedidoDTO =
        PedidoDTO.builder()
            .idPedido(pedidosRecuperados.getFirst().getIdPedido())
            .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
            .recargoPorcentaje(new BigDecimal("5"))
            .renglones(renglonesPedidoDTO)
            .idSucursal(1L)
            .idCliente(pedidosRecuperados.getFirst().getCliente().getIdCliente())
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .build();
    restTemplate.put("/api/v1/pedidos", pedidoDTO);
    var productoDos = restTemplate.getForObject("/api/v1/productos/2/sucursales/1", ProductoTest.class);
    assertEquals(new BigDecimal("9.000000000000000"), productoDos.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("3.000000000000000"), productoDos.getCantidadReservada());
    criteria = BusquedaPedidoCriteria.builder().idSucursal(1L).build();
    requestEntity = new HttpEntity<>(criteria);
    resultadoBusquedaPedido =
        restTemplate
            .exchange(
                "/api/v1/pedidos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<PedidoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaPedido);
    pedidosRecuperados = resultadoBusquedaPedido.getContent();
    assertEquals(1, pedidosRecuperados.size());
    assertEquals(EstadoPedido.ABIERTO, pedidosRecuperados.getFirst().getEstado());
    var renglonesDelPedido =
        Arrays.asList(
            restTemplate.getForObject(
                "/api/v1/pedidos/" + pedidosRecuperados.getFirst().getIdPedido() + "/renglones",
                RenglonPedidoTest[].class));
    assertEquals(3, renglonesDelPedido.size());
    assertEquals("Ventilador de pie", renglonesDelPedido.getFirst().getDescripcionItem());
    assertEquals("Metro", renglonesDelPedido.getFirst().getMedidaItem());
    assertEquals(new BigDecimal("1210.000000000000000"), renglonesDelPedido.getFirst().getPrecioUnitario());
    assertEquals(new BigDecimal("5.000000000000000"), renglonesDelPedido.getFirst().getCantidad());
    assertEquals(new BigDecimal("20.000000000000000"), renglonesDelPedido.getFirst().getBonificacionPorcentaje());
    assertEquals(new BigDecimal("242.000000000000000"), renglonesDelPedido.getFirst().getBonificacionNeta());
    assertEquals(new BigDecimal("6050.000000000000000000000000000000"), renglonesDelPedido.getFirst().getImporteAnterior());
    assertEquals(new BigDecimal("4840.000000000000000000000000000000"), renglonesDelPedido.getFirst().getImporte());
    assertEquals("Reflector led 100w", renglonesDelPedido.get(1).getDescripcionItem());
    assertEquals("Metro", renglonesDelPedido.get(1).getMedidaItem());
    assertEquals(new BigDecimal("1105.000000000000000"), renglonesDelPedido.get(1).getPrecioUnitario());
    assertEquals(new BigDecimal("3.000000000000000"), renglonesDelPedido.get(1).getCantidad());
    assertEquals(new BigDecimal("20.000000000000000"), renglonesDelPedido.get(1).getBonificacionPorcentaje());
    assertEquals(new BigDecimal("221.000000000000000"), renglonesDelPedido.get(1).getBonificacionNeta());
    assertEquals(new BigDecimal("3315.000000000000000000000000000000"), renglonesDelPedido.get(1).getImporteAnterior());
    assertEquals(new BigDecimal("2652.000000000000000000000000000000"), renglonesDelPedido.get(1).getImporte());
    assertEquals("Canilla Monocomando", renglonesDelPedido.get(2).getDescripcionItem());
    assertEquals("Metro", renglonesDelPedido.get(2).getMedidaItem());
    assertEquals(new BigDecimal("13364.402000000000000"), renglonesDelPedido.get(2).getPrecioUnitario());
    assertEquals(new BigDecimal("10.000000000000000"), renglonesDelPedido.get(2).getCantidad());
    assertEquals(new BigDecimal("10.000000000000000"), renglonesDelPedido.get(2).getBonificacionPorcentaje());
    assertEquals(new BigDecimal("1336.440200000000000"), renglonesDelPedido.get(2).getBonificacionNeta());
    assertEquals(new BigDecimal("133644.020000000000000000000000000000"), renglonesDelPedido.get(2).getImporteAnterior());
    assertEquals(new BigDecimal("120279.618000000000000000000000000000"), renglonesDelPedido.get(2).getImporte());
  }

  @Test
  @DisplayName("Facturar pedido al cliente RI con factura dividida, luego saldar la CC con efectivo y verificar stock")
  @Order(9)
  void testEscenarioVenta1() {
    this.iniciarSesionComoAdministrador();
    BusquedaPedidoCriteria criteria = BusquedaPedidoCriteria.builder().idSucursal(1L).build();
    HttpEntity<BusquedaPedidoCriteria> requestEntity = new HttpEntity<>(criteria);
    var resultadoBusquedaPedido =
        restTemplate
            .exchange(
                "/api/v1/pedidos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<PedidoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaPedido);
    var pedidosRecuperados = resultadoBusquedaPedido.getContent();
    assertEquals(1, pedidosRecuperados.size());
    var renglones =
        Arrays.asList(
            restTemplate.getForObject(
                    "/api/v1/facturas/ventas/renglones/pedidos/"
                    + pedidosRecuperados.getFirst().getIdPedido()
                    + "?tipoDeComprobante=FACTURA_A",
                RenglonFactura[].class));
    assertEquals("Ventilador de pie", renglones.getFirst().getDescripcionItem());
    assertEquals(new BigDecimal("5.000000000000000"), renglones.getFirst().getCantidad());
    assertEquals(new BigDecimal("1000.000000000000000"), renglones.getFirst().getPrecioUnitario());
    assertEquals(new BigDecimal("20.000000000000000"), renglones.getFirst().getBonificacionPorcentaje());
    assertEquals(new BigDecimal("200.000000000000000"), renglones.getFirst().getBonificacionNeta());
    assertEquals(new BigDecimal("21.000000000000000"), renglones.getFirst().getIvaPorcentaje());
    assertEquals(new BigDecimal("168.000000000000000000000000000000000000000000000"), renglones.getFirst().getIvaNeto());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.getFirst().getGananciaPorcentaje());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.getFirst().getGananciaNeto());
    assertEquals(new BigDecimal("6050.000000000000000000000000000000"), renglones.getFirst().getImporteAnterior());
    assertEquals(new BigDecimal("4000.000000000000000000000000000000"), renglones.getFirst().getImporte());
    assertEquals("Reflector led 100w", renglones.get(1).getDescripcionItem());
    assertEquals(new BigDecimal("3.000000000000000"), renglones.get(1).getCantidad());
    assertEquals(new BigDecimal("1000.000000000000000"), renglones.get(1).getPrecioUnitario());
    assertEquals(new BigDecimal("20.000000000000000"), renglones.get(1).getBonificacionPorcentaje());
    assertEquals(new BigDecimal("200.000000000000000"), renglones.get(1).getBonificacionNeta());
    assertEquals(new BigDecimal("10.500000000000000"), renglones.get(1).getIvaPorcentaje());
    assertEquals(new BigDecimal("84.000000000000000000000000000000000000000000000"), renglones.get(1).getIvaNeto());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.get(1).getGananciaPorcentaje());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.get(1).getGananciaNeto());
    assertEquals(new BigDecimal("3315.000000000000000000000000000000"), renglones.get(1).getImporteAnterior());
    assertEquals(new BigDecimal("2400.000000000000000000000000000000"), renglones.get(1).getImporte());
    assertEquals("Canilla Monocomando", renglones.get(2).getDescripcionItem());
    assertEquals(new BigDecimal("10.000000000000000"), renglones.get(2).getCantidad());
    assertEquals(new BigDecimal("12094.481000000000000"), renglones.get(2).getPrecioUnitario());
    assertEquals(new BigDecimal("10.000000000000000"), renglones.get(2).getBonificacionPorcentaje());
    assertEquals(new BigDecimal("1209.448100000000000"), renglones.get(2).getBonificacionNeta());
    assertEquals(new BigDecimal("10.500000000000000"), renglones.get(2).getIvaPorcentaje());
    assertEquals(new BigDecimal("1142.928454500000000000000000000000000000000000000"), renglones.get(2).getIvaNeto());
    assertEquals(new BigDecimal("11.370000000000000"), renglones.get(2).getGananciaPorcentaje());
    assertEquals(new BigDecimal("1234.751000000000000"), renglones.get(2).getGananciaNeto());
    assertEquals(new BigDecimal("133644.020000000000000000000000000000"), renglones.get(2).getImporteAnterior());
    assertEquals(new BigDecimal("108850.329000000000000000000000000000"), renglones.get(2).getImporte());
    var cliente = restTemplate.getForObject("/api/v1/clientes/1", ClienteTest.class);
    assertNotNull(cliente);
    int[] indices = new int[] {0};
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder()
            .idCliente(1L)
            .idSucursal(1L)
            .tipoDeComprobante(TipoDeComprobante.FACTURA_A)
            .recargoPorcentaje(new BigDecimal("10"))
            .descuentoPorcentaje(new BigDecimal("25"))
            .indices(indices)
            .build();
    var facturas = restTemplate.postForObject(
            "/api/v1/facturas/ventas/pedidos/" + pedidosRecuperados.getFirst().getIdPedido(),
            nuevaFacturaVentaDTO, FacturaVentaTest[].class);
    var productoUno = restTemplate.getForObject("/api/v1/productos/1/sucursales/1", ProductoTest.class);
    assertEquals(new BigDecimal("8.000000000000000"), productoUno.getCantidadTotalEnSucursales());
    var productoDos = restTemplate.getForObject("/api/v1/productos/2/sucursales/1", ProductoTest.class);
    assertEquals(new BigDecimal("9.000000000000000"), productoDos.getCantidadTotalEnSucursales());
    var productoTres = restTemplate.getForObject("/api/v1/productos/3/sucursales/1", ProductoTest.class);
    assertEquals(new BigDecimal("0E-15"), productoTres.getCantidadTotalEnSucursales());
    assertEquals(2, facturas.length);
    assertEquals(TipoDeComprobante.FACTURA_A, facturas[1].getTipoComprobante());
    assertEquals(TipoDeComprobante.FACTURA_X, facturas[0].getTipoComprobante());
    //assertNotEquals(0L, facturas[1].getCae());
    assertNotNull(
        restTemplate.getForObject(
            "/api/v1/facturas/ventas/" + facturas[0].getIdFactura() + "/reporte",
            byte[].class));
    assertNotNull(
        restTemplate.getForObject(
            "/api/v1/facturas/ventas/" + facturas[1].getIdFactura() + "/reporte",
            byte[].class));
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    SucursalTest sucursal = restTemplate.getForObject("/api/v1/sucursales/1", SucursalTest.class);
    assertNotNull(sucursal);
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(cliente.getNombreFiscal(), facturas[1].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[1].getNombreSucursal());
    UsuarioTest credencial = restTemplate.getForObject("/api/v1/usuarios/2", UsuarioTest.class);
    assertNotNull(credencial);
    assertEquals(credencial.getNombre() + " " + credencial.getApellido() + " (" + credencial.getUsername() + ")",
            facturas[0].getNombreUsuario());
    assertEquals(credencial.getNombre() + " " + credencial.getApellido() + " (" + credencial.getUsername() + ")",
            facturas[1].getNombreUsuario());
    assertEquals(new BigDecimal("1600.000000000000000000000000000000"), facturas[0].getSubTotal());
    assertEquals(new BigDecimal("113650.329000000000000000000000000000"), facturas[1].getSubTotal());
    assertEquals(new BigDecimal("160.000000000000000"), facturas[0].getRecargoNeto());
    assertEquals(new BigDecimal("11365.032900000000000"), facturas[1].getRecargoNeto());
    assertEquals(new BigDecimal("400.000000000000000"), facturas[0].getDescuentoNeto());
    assertEquals(new BigDecimal("28412.582250000000000"), facturas[1].getDescuentoNeto());
    assertEquals(BigDecimal.ZERO, facturas[0].getIva105Neto());
    assertEquals(
            new BigDecimal("9929.091863250000000000000000000000000000000000000000000000000000000000000000000"),
            facturas[1].getIva105Neto());
    assertEquals(BigDecimal.ZERO, facturas[0].getIva21Neto());
    assertEquals(new BigDecimal("428.400000000000000000000000000000000000000000000000000000000000"), facturas[1].getIva21Neto());
    assertEquals(new BigDecimal("1360.000000000000000000000000000000"), facturas[0].getSubTotalBruto());
    assertEquals(new BigDecimal("96602.779650000000000000000000000000"), facturas[1].getSubTotalBruto());
    assertEquals(new BigDecimal("1360.000000000000000000000000000000"), facturas[0].getTotal());
    assertEquals(
            new BigDecimal("106960.271513250000000000000000000000000000000000000000000000000000000000000000000"),
            facturas[1].getTotal());
    var renglonesFacturaUno =
        Arrays.asList(
            restTemplate.getForObject(
                "/api/v1/facturas/" + facturas[0].getIdFactura() + "/renglones",
                RenglonFacturaTest[].class));
    var renglonesFacturaDos =
        Arrays.asList(
            restTemplate.getForObject(
                "/api/v1/facturas/" + facturas[1].getIdFactura() + "/renglones",
                RenglonFacturaTest[].class));
    assertEquals(2.0, renglonesFacturaUno.getFirst().getCantidad().doubleValue());
    assertEquals(3.0, renglonesFacturaDos.getFirst().getCantidad().doubleValue());
    assertEquals(3.0, renglonesFacturaDos.get(1).getCantidad().doubleValue());
    assertEquals(10.000000000000000, renglonesFacturaDos.get(2).getCantidad().doubleValue());
    assertEquals(
        -108320.27151325,
        restTemplate
            .getForObject("/api/v1/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
            .doubleValue());
    var recibo =
        ReciboTest.builder()
            .concepto("Recibo Test")
            .monto(108320.27151325)
            .idSucursal(sucursal.getIdSucursal())
            .idCliente(cliente.getIdCliente())
            .idFormaDePago(1L)
            .build();
    var reciboDeFactura = restTemplate.postForObject("/api/v1/recibos/clientes", recibo, ReciboTest.class);
    assertNotNull(
        restTemplate.getForObject(
            "/api/v1/recibos/" + reciboDeFactura.getIdRecibo() + "/reporte", byte[].class));
    assertEquals(recibo, reciboDeFactura);
    assertEquals(
        0.0,
        restTemplate
            .getForObject("/api/v1/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
            .doubleValue());
    criteria = BusquedaPedidoCriteria.builder().idSucursal(1L).build();
    requestEntity = new HttpEntity<>(criteria);
    resultadoBusquedaPedido =
        restTemplate
            .exchange(
                "/api/v1/pedidos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<PedidoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaPedido);
    pedidosRecuperados = resultadoBusquedaPedido.getContent();
    assertEquals(1, pedidosRecuperados.size());
    assertEquals(EstadoPedido.CERRADO, pedidosRecuperados.getFirst().getEstado());
  }

  @Test
  @DisplayName("Dar de alta un transportista, luego crear dos remitos por las facturas anteriores usando ese transportista")
  @Order(10)
  void testEscenarioRemito() {
    this.iniciarSesionComoAdministrador();
    TransportistaDTO transportistaNuevo = new TransportistaDTO();
    UbicacionDTO ubicacionParaTransportista = new UbicacionDTO();
    ubicacionParaTransportista.setIdLocalidad(1L);
    ubicacionParaTransportista.setIdProvincia(1L);
    transportistaNuevo.setUbicacion(ubicacionParaTransportista);
    transportistaNuevo.setNombre("Transportista nuevo");
    transportistaNuevo.setTelefono("3795448866");
    transportistaNuevo.setWeb("transportista.com.ar");
    transportistaNuevo = restTemplate.postForObject("/api/v1/transportistas", transportistaNuevo, TransportistaDTO.class);
    assertEquals("Transportista nuevo", transportistaNuevo.getNombre());
    assertEquals("3795448866", transportistaNuevo.getTelefono());
    assertEquals("transportista.com.ar", transportistaNuevo.getWeb());
    BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity<>(criteria);
    var resultadoBusquedaFactura =
            restTemplate.exchange("/api/v1/facturas/ventas/busqueda/criteria",
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaTest>>() {})
                    .getBody();
    assertNotNull(resultadoBusquedaFactura);
    var facturasVenta = resultadoBusquedaFactura.getContent();
    assertEquals(2, facturasVenta.size());
    BigDecimal[] cantidadesDeBultos = new BigDecimal[]{new BigDecimal("6"), BigDecimal.TEN};
    TipoBulto[] tipoBulto = new TipoBulto[]{TipoBulto.CAJA, TipoBulto.ATADO};
    var remitoResultante =
        restTemplate.postForObject(
            "/api/v1/remitos",
            NuevoRemitoDTO.builder()
                .idFacturaVenta(new long[] {facturasVenta.getFirst().getIdFactura(), facturasVenta.get(1).getIdFactura()})
                .cantidadPorBulto(cantidadesDeBultos)
                .tiposDeBulto(tipoBulto)
                .costoDeEnvio(new BigDecimal("25"))
                .idTransportista(1L)
                .pesoTotalEnKg(new BigDecimal("72"))
                .volumenTotalEnM3(new BigDecimal("118"))
                .build(),
            RemitoTest.class);
    assertNotNull(remitoResultante);
    assertEquals(1L, remitoResultante.getIdRemito());
    assertNotNull(remitoResultante.getFecha());
    assertEquals(2L, remitoResultante.getSerie());
    assertEquals(1L, remitoResultante.getNroRemito());
    assertEquals(1L, remitoResultante.getIdCliente());
    assertEquals("Juan Fernando Canete", remitoResultante.getNombreFiscalCliente());
    assertNotNull(remitoResultante.getNroDeCliente());
    assertEquals(CategoriaIVA.RESPONSABLE_INSCRIPTO, remitoResultante.getCategoriaIVACliente());
    assertEquals(1L, remitoResultante.getIdSucursal());
    assertEquals("FirstOfAll", remitoResultante.getNombreSucursal());
    assertEquals(2L, remitoResultante.getIdUsuario());
    assertEquals("Max Power (dueño)", remitoResultante.getNombreUsuario());
    assertEquals("Corrientes Corrientes", remitoResultante.getDetalleEnvio());
    assertEquals(new BigDecimal("25"), remitoResultante.getCostoDeEnvio());
    assertEquals(new BigDecimal("108320.271513250000000"), remitoResultante.getTotalFacturas());
    assertEquals(new BigDecimal("108345.271513250000000"), remitoResultante.getTotal());
    assertEquals(new BigDecimal("72"), remitoResultante.getPesoTotalEnKg());
    assertEquals(new BigDecimal("118"), remitoResultante.getVolumenTotalEnM3());
    assertEquals(new BigDecimal("16"), remitoResultante.getCantidadDeBultos());
    assertEquals(remitoResultante.getNombreTransportista(), transportistaNuevo.getNombre());
    assertNotNull(restTemplate.getForObject("/api/v1/remitos/" + remitoResultante.getIdRemito() + "/reporte", byte[].class));
    resultadoBusquedaFactura =
            restTemplate
                    .exchange(
                            "/api/v1/facturas/ventas/busqueda/criteria",
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaTest>>() {})
                    .getBody();
    assertNotNull(resultadoBusquedaFactura);
    facturasVenta = resultadoBusquedaFactura.getContent();
    assertEquals(2, facturasVenta.size());
    assertNotNull(facturasVenta.get(1).getRemito());
    assertEquals(1L,facturasVenta.get(1).getRemito().getIdRemito());
    assertNotNull(facturasVenta.getFirst().getRemito());
    assertEquals( 1L,facturasVenta.getFirst().getRemito().getIdRemito());
  }

  @Test
  @DisplayName("Realizar devolucion parcial de productos y verificar saldo CC")
  @Order(11)
  void testEscenarioVenta2() {
    this.iniciarSesionComoAdministrador();
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .tipoComprobante(TipoDeComprobante.FACTURA_X)
            .numSerie(2L)
            .numFactura(1L)
            .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity<>(criteria);
    var resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/facturas/ventas/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    var facturasRecuperadas = resultadoBusqueda.getContent();
    Long[] idsRenglonesFactura = new Long[1];
    idsRenglonesFactura[0] = 3L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("1");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
        NuevaNotaCreditoDeFacturaDTO.builder()
            .idFactura(facturasRecuperadas.getFirst().getIdFactura())
            .idsRenglonesFactura(idsRenglonesFactura)
            .cantidades(cantidades)
            .modificaStock(true)
            .motivo("No funcionan.")
            .build();
    var notaCreditoCalculada =
        restTemplate.postForObject("/api/v1/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoTest.class);
    var notaCreditoGuardada =
        restTemplate.postForObject("/api/v1/notas/credito/factura", nuevaNotaCreditoDTO, NotaCreditoTest.class);
    assertEquals(TipoDeComprobante.NOTA_CREDITO_X, notaCreditoCalculada.getTipoComprobante());
    BusquedaNotaCriteria criteriaNota =
        BusquedaNotaCriteria.builder()
            .idSucursal(1L)
            .tipoComprobante(TipoDeComprobante.NOTA_CREDITO_X)
            .build();
    HttpEntity<BusquedaNotaCriteria> requestEntityNota = new HttpEntity<>(criteriaNota);
    var resultadoBusquedaNota =
        restTemplate
            .exchange(
                "/api/v1/notas/credito/busqueda/criteria",
                HttpMethod.POST,
                requestEntityNota,
                new ParameterizedTypeReference<PaginaRespuestaRest<NotaCreditoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaNota);
    var notasRecuperadas = resultadoBusquedaNota.getContent();
    List<RenglonNotaCreditoTest> renglones =
        Arrays.asList(
            restTemplate.getForObject(
                "/api/v1/notas/renglones/credito/" + notasRecuperadas.getFirst().getIdNota(),
                RenglonNotaCreditoTest[].class));
    assertNotNull(renglones);
    assertEquals(1, renglones.size());
    // renglones
    assertEquals(new BigDecimal("1.000000000000000"), renglones.getFirst().getCantidad());
    assertEquals(new BigDecimal("1000.000000000000000"), renglones.getFirst().getPrecioUnitario());
    assertEquals(new BigDecimal("20.000000000000000"), renglones.getFirst().getDescuentoPorcentaje());
    assertEquals(new BigDecimal("200.000000000000000"), renglones.getFirst().getDescuentoNeto());
    assertEquals(new BigDecimal("21.000000000000000"), renglones.getFirst().getIvaPorcentaje());
    assertEquals(0.0, renglones.getFirst().getIvaNeto().doubleValue());
    assertEquals(new BigDecimal("1000.000000000000000"), renglones.getFirst().getImporte());
    assertEquals(new BigDecimal("800.000000000000000"), renglones.getFirst().getImporteBruto());
    assertEquals(new BigDecimal("800.000000000000000"), renglones.getFirst().getImporteNeto());
    // pie de nota
    assertEquals(new BigDecimal("800.000000000000000000000000000000"), notaCreditoGuardada.getSubTotal());
    assertEquals(new BigDecimal("80.000000000000000"), notaCreditoGuardada.getRecargoNeto());
    assertEquals(new BigDecimal("200.000000000000000"), notaCreditoGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("680.000000000000000000000000000000"), notaCreditoGuardada.getSubTotalBruto());
    assertEquals(0.0, notaCreditoGuardada.getIva21Neto().doubleValue());
    assertEquals(BigDecimal.ZERO, notaCreditoGuardada.getIva105Neto());
    assertEquals(new BigDecimal("680.000000000000000000000000000000"), notaCreditoGuardada.getTotal());
    assertNotNull(restTemplate.getForObject("/api/v1/notas/1/reporte", byte[].class));
    assertEquals(
            655.0,
            restTemplate.getForObject("/api/v1/cuentas-corriente/clientes/1/saldo", BigDecimal.class).doubleValue());
  }

  @Test
  @DisplayName("Registrar un cliente nuevo y enviar un pedido mediante carrito de compra")
  @Order(12)
  void testEscenarioRegistracionYPedidoDelNuevoCliente() {
    RegistracionClienteAndUsuarioDTO registro =
        RegistracionClienteAndUsuarioDTO.builder()
            .apellido("Stark")
            .nombre("Sansa María")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .email("sansa@got.com")
            .telefono("4157899667")
            .password("caraDeMala")
            .recaptcha("111111")
            .nombreFiscal("theRedWolf")
            .build();
    restTemplate.postForObject("/api/v1/registracion", registro, Void.class);
    this.iniciarSesionComoAdministrador();
    UsuarioTest usuario = restTemplate.getForObject("/api/v1/usuarios/4", UsuarioTest.class);
    assertEquals("Sansa María", usuario.getNombre());
    assertEquals("Stark", usuario.getApellido());
    assertTrue(usuario.isHabilitado());
    var cliente = restTemplate.getForObject("/api/v1/clientes/2", ClienteTest.class);
    assertEquals("theRedWolf", cliente.getNombreFiscal());
    assertEquals(0.0, cliente.getMontoCompraMinima().doubleValue());
    assertFalse(cliente.isPuedeComprarAPlazo());
    var cuentaCorrienteCliente =
        restTemplate.getForObject(
            "/api/v1/cuentas-corriente/clientes/" + cliente.getIdCliente(),
            CuentaCorrienteCliente.class);
    assertNotNull(cuentaCorrienteCliente);
    assertEquals(0.0, cuentaCorrienteCliente.getSaldo().doubleValue());
    cliente.setUbicacionFacturacion(UbicacionTest.builder().idLocalidad(2L).idProvincia(2L).build());
    cliente.setPuedeComprarAPlazo(true);
    restTemplate.put("/api/v1/clientes", cliente);
    this.token =
        restTemplate
            .postForEntity(
                "/api/v1/login",
                new Credencial(usuario.getUsername(), "caraDeMala"),
                String.class)
            .getBody();
    assertNotNull(this.token);
    restTemplate.postForObject("/api/v1/carrito-compra/productos/1?cantidad=5", null, ItemCarritoCompra.class);
    restTemplate.postForObject("/api/v1/carrito-compra/productos/2?cantidad=9", null, ItemCarritoCompra.class);
    var item1 = restTemplate.getForObject("/api/v1/carrito-compra/productos/1/sucursales/1", ItemCarritoCompra.class);
    assertNotNull(item1);
    assertEquals(1L, item1.getProducto().getIdProducto().longValue());
    assertEquals(5, item1.getCantidad().doubleValue());
    var item2 = restTemplate.getForObject("/api/v1/carrito-compra/productos/2/sucursales/1", ItemCarritoCompra.class);
    assertNotNull(item2);
    assertEquals(2L, item2.getProducto().getIdProducto().longValue());
    assertEquals(9, item2.getCantidad().doubleValue());
    NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO =
        NuevaOrdenDePagoDTO.builder()
                .idSucursal(1L)
                .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_FACTURACION)
                .build();
    var pedido =
        restTemplate.postForObject(
            "/api/v1/carrito-compra", nuevaOrdenDePagoDTO, PedidoTest.class);
    assertNotNull(pedido);
    assertEquals(14, pedido.getCantidadArticulos().doubleValue());
    assertEquals(new BigDecimal("12796.000000000000000"), pedido.getTotal());
    assertEquals(EstadoPedido.ABIERTO, pedido.getEstado());
    List<RenglonPedidoTest> renglonesDelPedido =
        Arrays.asList(
            restTemplate.getForObject(
                "/api/v1/pedidos/" + pedido.getIdPedido() + "/renglones",
                RenglonPedidoTest[].class));
    assertEquals(2, renglonesDelPedido.size());
    assertEquals("Reflector led 100w", renglonesDelPedido.getFirst().getDescripcionItem());
    assertEquals("Metro", renglonesDelPedido.getFirst().getMedidaItem());
    assertEquals(new BigDecimal("1105.000000000000000"), renglonesDelPedido.getFirst().getPrecioUnitario());
    assertEquals(new BigDecimal("9.000000000000000"), renglonesDelPedido.getFirst().getCantidad());
    assertEquals(new BigDecimal("20.000000000000000"), renglonesDelPedido.getFirst().getBonificacionPorcentaje());
    assertEquals(new BigDecimal("221.000000000000000"), renglonesDelPedido.getFirst().getBonificacionNeta());
    assertEquals(new BigDecimal("9945.000000000000000000000000000000"), renglonesDelPedido.getFirst().getImporteAnterior());
    assertEquals(new BigDecimal("7956.000000000000000000000000000000"), renglonesDelPedido.getFirst().getImporte());
    assertEquals("Ventilador de pie", renglonesDelPedido.get(1).getDescripcionItem());
    assertEquals("Metro", renglonesDelPedido.get(1).getMedidaItem());
    assertEquals(new BigDecimal("1210.000000000000000"), renglonesDelPedido.get(1).getPrecioUnitario());
    assertEquals(new BigDecimal("5.000000000000000"), renglonesDelPedido.get(1).getCantidad());
    assertEquals(new BigDecimal("20.000000000000000"), renglonesDelPedido.get(1).getBonificacionPorcentaje());
    assertEquals(new BigDecimal("242.000000000000000"), renglonesDelPedido.get(1).getBonificacionNeta());
    assertEquals(new BigDecimal("6050.000000000000000000000000000000"), renglonesDelPedido.get(1).getImporteAnterior());
    assertEquals(new BigDecimal("4840.000000000000000000000000000000"), renglonesDelPedido.get(1).getImporte());
  }

  @Test
  @DisplayName("Cerrar caja y verificar movimientos")
  @Order(13)
  void testEscenarioCerrarCaja1() {
    this.iniciarSesionComoAdministrador();
    var sucursales = Arrays.asList(restTemplate.getForObject("/api/v1/sucursales", SucursalTest[].class));
    assertNotNull(sucursales);
    assertEquals(1, sucursales.size());
    var criteriaParaBusquedaCaja = BusquedaCajaCriteria.builder().idSucursal(sucursales.getFirst().getIdSucursal()).build();
    HttpEntity<BusquedaCajaCriteria> requestEntityParaProveedores = new HttpEntity<>(criteriaParaBusquedaCaja);
    var resultadosBusquedaCaja =
        restTemplate
            .exchange(
                "/api/v1/cajas/busqueda/criteria",
                HttpMethod.POST,
                requestEntityParaProveedores,
                new ParameterizedTypeReference<PaginaRespuestaRest<CajaTest>>() {})
            .getBody();
    assertNotNull(resultadosBusquedaCaja);
    var cajasRecuperadas = resultadosBusquedaCaja.getContent();
    assertEquals(1, cajasRecuperadas.size());
    assertEquals(EstadoCaja.ABIERTA, cajasRecuperadas.getFirst().getEstado());
    restTemplate.put(
        "/api/v1/cajas/" + cajasRecuperadas.getFirst().getIdCaja() + "/cierre?monto=5276.66",
        null);
    resultadosBusquedaCaja =
        restTemplate
            .exchange(
                "/api/v1/cajas/busqueda/criteria",
                HttpMethod.POST,
                requestEntityParaProveedores,
                new ParameterizedTypeReference<PaginaRespuestaRest<CajaTest>>() {})
            .getBody();
    assertNotNull(resultadosBusquedaCaja);
    cajasRecuperadas = resultadosBusquedaCaja.getContent();
    assertEquals(1, cajasRecuperadas.size());
    assertEquals(EstadoCaja.CERRADA, cajasRecuperadas.getFirst().getEstado());
    assertEquals(new BigDecimal("1000.000000000000000"), cajasRecuperadas.getFirst().getSaldoApertura());
    List<MovimientoCaja> movimientoCajas =
        Arrays.asList(
            restTemplate.getForObject(
                    "/api/v1/cajas/"
                    + cajasRecuperadas.getFirst().getIdCaja()
                    + "/movimientos?idFormaDePago=1",
                MovimientoCaja[].class));
    assertEquals(2, movimientoCajas.size());
    assertEquals(new BigDecimal("108320.271513250000000"), movimientoCajas.getFirst().getMonto());
    assertEquals(new BigDecimal("-500.000000000000000"), movimientoCajas.get(1).getMonto());
    movimientoCajas =
        Arrays.asList(
            restTemplate.getForObject(
                    "/api/v1/cajas/"
                    + cajasRecuperadas.getFirst().getIdCaja()
                    + "/movimientos?idFormaDePago=2",
                MovimientoCaja[].class));
    assertEquals(1, movimientoCajas.size());
    assertEquals(new BigDecimal("-554.540000000000000"), movimientoCajas.getFirst().getMonto());
    assertEquals(
        new BigDecimal("108820.271513250000000"),
        restTemplate.getForObject(
            "/api/v1/cajas/" + cajasRecuperadas.getFirst().getIdCaja() + "/saldo-afecta-caja",
            BigDecimal.class));
    assertEquals(
        new BigDecimal("108265.731513250000000"),
        restTemplate.getForObject(
            "/api/v1/cajas/" + cajasRecuperadas.getFirst().getIdCaja() + "/saldo-sistema",
            BigDecimal.class));
  }

  @Test
  @DisplayName("Reabrir caja, corregir saldo con un gasto por $750 en efectivo")
  @Order(14)
  void testEscenarioCerrarCaja2() {
    this.iniciarSesionComoAdministrador();
    var sucursales = Arrays.asList(restTemplate.getForObject("/api/v1/sucursales", SucursalTest[].class));
    assertEquals(1, sucursales.size());
    var criteriaParaBusquedaCaja = BusquedaCajaCriteria.builder().idSucursal(sucursales.getFirst().getIdSucursal()).build();
    HttpEntity<BusquedaCajaCriteria> requestEntityParaProveedores = new HttpEntity<>(criteriaParaBusquedaCaja);
    var resultadosBusquedaCaja =
        restTemplate
            .exchange(
                "/api/v1/cajas/busqueda/criteria",
                HttpMethod.POST,
                requestEntityParaProveedores,
                new ParameterizedTypeReference<PaginaRespuestaRest<CajaTest>>() {})
            .getBody();
    assertNotNull(resultadosBusquedaCaja);
    var cajasRecuperadas = resultadosBusquedaCaja.getContent();
    assertEquals(1, cajasRecuperadas.size());
    assertEquals(EstadoCaja.CERRADA, cajasRecuperadas.getFirst().getEstado());
    restTemplate.put(
            "/api/v1/cajas/"
                + cajasRecuperadas.getFirst().getIdCaja() + "/reapertura?monto=1100",
            null);
    resultadosBusquedaCaja =
        restTemplate
            .exchange(
                "/api/v1/cajas/busqueda/criteria",
                HttpMethod.POST,
                requestEntityParaProveedores,
                new ParameterizedTypeReference<PaginaRespuestaRest<CajaTest>>() {})
            .getBody();
    assertNotNull(resultadosBusquedaCaja);
    cajasRecuperadas = resultadosBusquedaCaja.getContent();
    assertEquals(1, cajasRecuperadas.size());
    assertEquals(EstadoCaja.ABIERTA, cajasRecuperadas.getFirst().getEstado());
    assertEquals(new BigDecimal("1100.000000000000000"), cajasRecuperadas.getFirst().getSaldoApertura());
    NuevoGastoDTO nuevoGasto =
        NuevoGastoDTO.builder()
            .concepto("Gasto olvidado")
            .monto(new BigDecimal("750"))
            .idSucursal(1L)
            .idFormaDePago(1L)
            .build();
    var gasto = restTemplate.postForObject("/api/v1/gastos", nuevoGasto, GastoTest.class);
    assertNotNull(gasto);
    List<MovimientoCaja> movimientoCajas =
        Arrays.asList(
            restTemplate.getForObject(
                    "/api/v1/cajas/"
                    + cajasRecuperadas.getFirst().getIdCaja()
                    + "/movimientos?idFormaDePago=1",
                MovimientoCaja[].class));
    assertEquals(3, movimientoCajas.size());
    assertEquals(new BigDecimal("-750.000000000000000"), movimientoCajas.getFirst().getMonto());
    assertEquals(new BigDecimal("108320.271513250000000"), movimientoCajas.get(1).getMonto());
    assertEquals(new BigDecimal("-500.000000000000000"), movimientoCajas.get(2).getMonto());
    movimientoCajas =
        Arrays.asList(
            restTemplate.getForObject(
                    "/api/v1/cajas/"
                    + cajasRecuperadas.getFirst().getIdCaja()
                    + "/movimientos?idFormaDePago=2",
                MovimientoCaja[].class));
    assertEquals(1, movimientoCajas.size());
    assertEquals(new BigDecimal("-554.540000000000000"), movimientoCajas.getFirst().getMonto());
    assertEquals(
        new BigDecimal("108170.271513250000000"),
        restTemplate.getForObject(
            "/api/v1/cajas/" + cajasRecuperadas.getFirst().getIdCaja() + "/saldo-afecta-caja",
            BigDecimal.class));
    assertEquals(
        new BigDecimal("107615.731513250000000"),
        restTemplate.getForObject(
            "/api/v1/cajas/" + cajasRecuperadas.getFirst().getIdCaja() + "/saldo-sistema",
            BigDecimal.class));
  }

  @Test
  @DisplayName("Facturar un pedido, luego intentar cancelarlo sin éxito")
  @Order(15)
  void testEscenarioFacturarPedidoAndIntentarEliminarlo() {
    this.iniciarSesionComoAdministrador();
    BusquedaProductoCriteria productosCriteria = BusquedaProductoCriteria.builder().build();
    HttpEntity<BusquedaProductoCriteria> requestEntityProductos = new HttpEntity<>(productosCriteria);
    var resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/productos/busqueda/criteria/sucursales/1",
                HttpMethod.POST,
                requestEntityProductos,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    List<ProductoTest> productosRecuperados = resultadoBusqueda.getContent();
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.get(2).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("4.000000000000000"),
        productosRecuperados.get(3).getCantidadTotalEnSucursales());
    var pedidoCriteria = BusquedaPedidoCriteria.builder().idSucursal(1L).estadoPedido(EstadoPedido.ABIERTO).build();
    HttpEntity<BusquedaPedidoCriteria> requestEntity = new HttpEntity<>(pedidoCriteria);
    var resultadoBusquedaPedido =
        restTemplate
            .exchange(
                "/api/v1/pedidos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<PedidoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaPedido);
    var pedidosRecuperados = resultadoBusquedaPedido.getContent();
    assertEquals(1, pedidosRecuperados.size());
    var renglones =
        Arrays.asList(
            restTemplate.getForObject(
                    "/api/v1/facturas/ventas/renglones/pedidos/"
                    + pedidosRecuperados.getFirst().getIdPedido()
                    + "?tipoDeComprobante=FACTURA_X",
                RenglonFactura[].class));
    assertEquals("Reflector led 100w", renglones.getFirst().getDescripcionItem());
    assertEquals(new BigDecimal("9.000000000000000"), renglones.getFirst().getCantidad());
    assertEquals(new BigDecimal("1000.000000000000000"), renglones.getFirst().getPrecioUnitario());
    assertEquals(new BigDecimal("20.000000000000000"), renglones.getFirst().getBonificacionPorcentaje());
    assertEquals(new BigDecimal("200.000000000000000"), renglones.getFirst().getBonificacionNeta());
    assertEquals(new BigDecimal("10.500000000000000"), renglones.getFirst().getIvaPorcentaje());
    assertEquals(new BigDecimal("0"), renglones.getFirst().getIvaNeto());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.getFirst().getGananciaPorcentaje());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.getFirst().getGananciaNeto());
    assertEquals(new BigDecimal("9945.000000000000000000000000000000"), renglones.getFirst().getImporteAnterior());
    assertEquals(new BigDecimal("7200.000000000000000000000000000000"), renglones.getFirst().getImporte());
    assertEquals("Ventilador de pie", renglones.get(1).getDescripcionItem());
    assertEquals(new BigDecimal("5.000000000000000"), renglones.get(1).getCantidad());
    assertEquals(new BigDecimal("1000.000000000000000"), renglones.get(1).getPrecioUnitario());
    assertEquals(new BigDecimal("20.000000000000000"), renglones.get(1).getBonificacionPorcentaje());
    assertEquals(new BigDecimal("200.000000000000000"), renglones.get(1).getBonificacionNeta());
    assertEquals(new BigDecimal("21.000000000000000"), renglones.get(1).getIvaPorcentaje());
    assertEquals(new BigDecimal("0"), renglones.get(1).getIvaNeto());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.get(1).getGananciaPorcentaje());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.get(1).getGananciaNeto());
    assertEquals(new BigDecimal("6050.000000000000000000000000000000"), renglones.get(1).getImporteAnterior());
    assertEquals(new BigDecimal("4000.000000000000000000000000000000"), renglones.get(1).getImporte());
    var cliente = restTemplate.getForObject("/api/v1/clientes/2", ClienteTest.class);
    assertNotNull(cliente);
    int[] indices = new int[] {0};
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder()
            .idCliente(pedidosRecuperados.getFirst().getCliente().getIdCliente())
            .idSucursal(pedidosRecuperados.getFirst().getIdSucursal())
            .tipoDeComprobante(TipoDeComprobante.FACTURA_X)
            .recargoPorcentaje(new BigDecimal("10"))
            .descuentoPorcentaje(new BigDecimal("25"))
            .indices(indices)
            .build();
    restTemplate.postForObject("/api/v1/facturas/ventas/pedidos/" + pedidosRecuperados.getFirst().getIdPedido(),
            nuevaFacturaVentaDTO, FacturaVenta[].class);
    pedidoCriteria = BusquedaPedidoCriteria.builder().idSucursal(1L).estadoPedido(EstadoPedido.CERRADO).build();
    requestEntity = new HttpEntity<>(pedidoCriteria);
    resultadoBusquedaPedido =
        restTemplate
            .exchange(
                "/api/v1/pedidos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<PedidoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaPedido);
    pedidosRecuperados = resultadoBusquedaPedido.getContent();
    assertEquals(2, pedidosRecuperados.size());
    assertEquals(EstadoPedido.CERRADO, pedidosRecuperados.getFirst().getEstado());
    resultadoBusqueda =
        restTemplate
            .exchange(
                "/api/v1/productos/busqueda/criteria/sucursales/1",
                HttpMethod.POST,
                requestEntityProductos,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    productosRecuperados = resultadoBusqueda.getContent();
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.get(2).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("4.000000000000000"), productosRecuperados.get(3).getCantidadTotalEnSucursales());
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.put("/api/v1/pedidos/2", null));
    assertNotNull(thrown.getMessage());
    assertTrue(thrown.getMessage()
            .contains(messageSource.getMessage(
                    "mensaje_no_se_puede_cancelar_pedido",
                    new Object[]{EstadoPedido.CERRADO},
                    Locale.getDefault())));
  }

  @Test
  @DisplayName("Actualizar stock de un producto para tener cantidades en dos sucursales")
  @Order(16)
  void testEscenarioActualizarStockParaDosSucursales() {
    this.iniciarSesionComoAdministrador();
    var clienteParaEditar = restTemplate.getForObject("/api/v1/clientes/2", ClienteTest.class);
    clienteParaEditar.setPuedeComprarAPlazo(true);
    restTemplate.put("/api/v1/clientes", clienteParaEditar);
    SucursalTest sucursalNueva =
            SucursalTest.builder()
                    .nombre("Sucursal Centrica")
                    .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
                    .email("sucursalNueva@gmail.com")
                    .idFiscal(30711184569L)
                    .ubicacion(UbicacionTest.builder().idLocalidad(1L).idProvincia(1L).build())
                    .build();
    var sucursalRecuperada = restTemplate.postForObject("/api/v1/sucursales", sucursalNueva, SucursalTest.class);
    assertEquals(sucursalNueva, sucursalRecuperada);
    var configuracionSucursal =
            restTemplate.getForObject(
                    "/api/v1/configuraciones-sucursal/" + sucursalRecuperada.getIdSucursal(),
                    ConfiguracionSucursal.class);
    configuracionSucursal.setPuntoDeRetiro(true);
    restTemplate.put("/api/v1/configuraciones-sucursal", configuracionSucursal);
    configuracionSucursal =
            restTemplate.getForObject(
                    "/api/v1/configuraciones-sucursal/" + sucursalRecuperada.getIdSucursal(),
                    ConfiguracionSucursal.class);
    assertTrue(configuracionSucursal.isPuntoDeRetiro());
    assertFalse(configuracionSucursal.isComparteStock());
    var sucursales = Arrays.asList(restTemplate.getForObject("/api/v1/sucursales", SucursalTest[].class));
    assertFalse(sucursales.isEmpty());
    assertEquals(2, sucursales.size());
    var productosCriteria = BusquedaProductoCriteria.builder().descripcion("Corta Papas - Vegetales").build();
    HttpEntity<BusquedaProductoCriteria> requestEntityProductos = new HttpEntity<>(productosCriteria);
    var resultadoBusqueda =
            restTemplate.exchange(
                            "/api/v1/productos/busqueda/criteria/sucursales/1",
                            HttpMethod.POST,
                            requestEntityProductos,
                            new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
                    .getBody();
    assertNotNull(resultadoBusqueda);
    Set<CantidadEnSucursalDTO> cantidadEnSucursales = new HashSet<>();
    CantidadEnSucursalDTO cantidadEnSucursalUno = CantidadEnSucursalDTO.builder()
            .cantidad(new BigDecimal("40"))
            .idSucursal(1L)
            .build();
    CantidadEnSucursalDTO cantidadEnSucursalDos = CantidadEnSucursalDTO.builder()
            .cantidad(new BigDecimal("100"))
            .idSucursal(2L)
            .build();
    cantidadEnSucursales.add(cantidadEnSucursalUno);
    cantidadEnSucursales.add(cantidadEnSucursalDos);
    ProductoDTO productoParaActualizar =
        ProductoDTO.builder()
            .idProducto(resultadoBusqueda.getContent().getFirst().getIdProducto())
            .cantidadEnSucursales(cantidadEnSucursales)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("10.5"))
            .ivaNeto(new BigDecimal("105"))
            .precioLista(new BigDecimal("1105"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .paraCatalogo(true)
            .build();
    restTemplate.put("/api/v1/productos", productoParaActualizar);
    var productoParaControlarStock =
        restTemplate.getForObject(
            "/api/v1/productos/" + resultadoBusqueda.getContent().getFirst().getIdProducto() + "/sucursales/1",
            ProductoTest.class);
    assertEquals(140, productoParaControlarStock.getCantidadTotalEnSucursales().doubleValue());
  }

  @Test
  @DisplayName("Realizar un pedido que requiera del stock de ambas")
  @Order(17)
  void testEscenarioPedidoConStockDeDosSucursales() {
    this.iniciarSesionComoAdministrador();
    UsuarioTest usuario = restTemplate.getForObject("/api/v1/usuarios/4", UsuarioTest.class);
    this.token =
        restTemplate
            .postForEntity(
                "/api/v1/login",
                new Credencial(usuario.getUsername(), "caraDeMala"),
                String.class)
            .getBody();
    assertNotNull(this.token);
    restTemplate.postForObject("/api/v1/carrito-compra/productos/4?cantidad=50", null, ItemCarritoCompra.class);
    var item1 = restTemplate.getForObject("/api/v1/carrito-compra/productos/4/sucursales/1", ItemCarritoCompra.class);
    assertNotNull(item1);
    assertEquals(4L, item1.getProducto().getIdProducto().longValue());
    assertEquals(50, item1.getCantidad().doubleValue());
    var nuevaOrdenDePagoDTO = NuevaOrdenDePagoDTO.builder()
            .idSucursal(1L)
            .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_FACTURACION)
            .build();
    RestClientResponseException thrown =
            assertThrows(
                    RestClientResponseException.class,
                    () -> restTemplate.postForObject("/api/v1/carrito-compra", nuevaOrdenDePagoDTO, PedidoTest.class));
    assertNotNull(thrown.getMessage());
    assertTrue(thrown.getMessage().contains(messageSource.getMessage("mensaje_pedido_sin_stock",
            null, Locale.getDefault())));
    this.iniciarSesionComoAdministrador();
    ConfiguracionSucursal configuracionSucursal =
            restTemplate.getForObject(
                    "/api/v1/configuraciones-sucursal/2",
                    ConfiguracionSucursal.class);
    configuracionSucursal.setComparteStock(true);
    restTemplate.put("/api/v1/configuraciones-sucursal", configuracionSucursal);
    configuracionSucursal =
            restTemplate.getForObject(
                    "/api/v1/configuraciones-sucursal/2",
                    ConfiguracionSucursal.class);
    assertTrue(configuracionSucursal.isPuntoDeRetiro());
    assertTrue(configuracionSucursal.isComparteStock());
    usuario = restTemplate.getForObject("/api/v1/usuarios/4", UsuarioTest.class);
    this.token = restTemplate.postForEntity(
                    "/api/v1/login",
                    new Credencial(usuario.getUsername(), "caraDeMala"),
                    String.class)
            .getBody();
    assertNotNull(this.token);
    var pedido = restTemplate.postForObject("/api/v1/carrito-compra", nuevaOrdenDePagoDTO, PedidoTest.class);
    assertEquals(new BigDecimal("50.000000000000000") , pedido.getCantidadArticulos());
    this.iniciarSesionComoAdministrador();
    BusquedaProductoCriteria productosCriteria = BusquedaProductoCriteria.builder().descripcion("Corta Papas - Vegetales").build();
    HttpEntity<BusquedaProductoCriteria> requestEntityProductos = new HttpEntity<>(productosCriteria);
    var resultadoBusqueda = restTemplate.exchange(
                    "/api/v1/productos/busqueda/criteria/sucursales/1",
                    HttpMethod.POST,
                    requestEntityProductos,
                    new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    var productoParaControlarStock =
            restTemplate.getForObject(
                    "/api/v1/productos/" + resultadoBusqueda.getContent().getFirst().getIdProducto() + "/sucursales/1",
                    ProductoTest.class);
    assertEquals(90, productoParaControlarStock.getCantidadTotalEnSucursales().doubleValue());
    var cantidadEnSucursalesParaAssert = new ArrayList<>(productoParaControlarStock.getCantidadEnSucursales());
    assertEquals(new BigDecimal("0E-15") , cantidadEnSucursalesParaAssert.getFirst().getCantidad());
    assertEquals(new BigDecimal("90.000000000000000") , cantidadEnSucursalesParaAssert.get(1).getCantidad());
    assertEquals(1L, cantidadEnSucursalesParaAssert.getFirst().getIdSucursal());
    assertEquals(2L, cantidadEnSucursalesParaAssert.get(1).getIdSucursal());
  }

  @Test
  @DisplayName("Facturar el pedido anterior")
  @Order(18)
  void testEscenarioFacturarPedido() {
    this.iniciarSesionComoAdministrador();
    var pedidoCriteria = BusquedaPedidoCriteria.builder().idSucursal(1L).estadoPedido(EstadoPedido.ABIERTO).build();
    HttpEntity<BusquedaPedidoCriteria> requestEntity = new HttpEntity<>(pedidoCriteria);
    var resultadoBusquedaPedido = restTemplate.exchange(
                    "/api/v1/pedidos/busqueda/criteria",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<PaginaRespuestaRest<PedidoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaPedido);
    var pedidosRecuperados = resultadoBusquedaPedido.getContent();
    assertEquals(1, pedidosRecuperados.size());
    var renglones = Arrays.asList(restTemplate.getForObject(
            "/api/v1/facturas/ventas/renglones/pedidos/" + pedidosRecuperados.getFirst().getIdPedido()
                    + "?tipoDeComprobante=FACTURA_X",
            RenglonFactura[].class));
    assertEquals("Corta Papas - Vegetales", renglones.getFirst().getDescripcionItem());
    assertEquals(new BigDecimal("50.000000000000000"), renglones.getFirst().getCantidad());
    assertEquals(new BigDecimal("1000.000000000000000"), renglones.getFirst().getPrecioUnitario());
    assertEquals(new BigDecimal("20.000000000000000"), renglones.getFirst().getBonificacionPorcentaje());
    assertEquals(new BigDecimal("200.000000000000000"), renglones.getFirst().getBonificacionNeta());
    assertEquals(new BigDecimal("10.500000000000000"), renglones.getFirst().getIvaPorcentaje());
    assertEquals(new BigDecimal("0"), renglones.getFirst().getIvaNeto());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.getFirst().getGananciaPorcentaje());
    assertEquals(new BigDecimal("900.000000000000000"), renglones.getFirst().getGananciaNeto());
    assertEquals(new BigDecimal("55250.000000000000000000000000000000"), renglones.getFirst().getImporteAnterior());
    assertEquals(new BigDecimal("40000.000000000000000000000000000000"), renglones.getFirst().getImporte());
    int[] indices = new int[] {0};
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
            NuevaFacturaVentaDTO.builder()
                    .idCliente(pedidosRecuperados.getFirst().getCliente().getIdCliente())
                    .idSucursal(pedidosRecuperados.getFirst().getIdSucursal())
                    .tipoDeComprobante(TipoDeComprobante.FACTURA_X)
                    .recargoPorcentaje(new BigDecimal("10"))
                    .descuentoPorcentaje(new BigDecimal("25"))
                    .indices(indices)
                    .build();
    restTemplate.postForObject("/api/v1/facturas/ventas/pedidos/" + pedidosRecuperados.getFirst().getIdPedido(),
            nuevaFacturaVentaDTO, FacturaVenta[].class);
  }

  @Test
  @DisplayName("Verificar stock y cerrar caja")
  @Order(19)
  void testEscenarioVerificarStockAndCerrarCaja() {
    this.iniciarSesionComoAdministrador();
    BusquedaProductoCriteria productosCriteria = BusquedaProductoCriteria.builder().build();
    HttpEntity<BusquedaProductoCriteria> requestEntityProductos = new HttpEntity<>(productosCriteria);
    var resultadoBusqueda = restTemplate.exchange(
                    "/api/v1/productos/busqueda/criteria/sucursales/1",
                    HttpMethod.POST,
                    requestEntityProductos,
                    new ParameterizedTypeReference<PaginaRespuestaRest<ProductoTest>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    var productosRecuperados = resultadoBusqueda.getContent();
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.getFirst().getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("90.000000000000000"), productosRecuperados.get(1).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("0E-15"), productosRecuperados.get(2).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("4.000000000000000"), productosRecuperados.get(3).getCantidadTotalEnSucursales());
    List<SucursalTest> sucursales = Arrays.asList(restTemplate.getForObject("/api/v1/sucursales", SucursalTest[].class));
    assertEquals(2, sucursales.size());
    BusquedaCajaCriteria criteriaParaBusquedaCaja =
        BusquedaCajaCriteria.builder().idSucursal(sucursales.getFirst().getIdSucursal()).build();
    HttpEntity<BusquedaCajaCriteria> requestEntityParaProveedores = new HttpEntity<>(criteriaParaBusquedaCaja);
    var resultadosBusquedaCaja = restTemplate.exchange(
                    "/api/v1/cajas/busqueda/criteria",
                    HttpMethod.POST,
                    requestEntityParaProveedores,
                    new ParameterizedTypeReference<PaginaRespuestaRest<CajaTest>>() {})
            .getBody();
    assertNotNull(resultadosBusquedaCaja);
    var cajasRecuperadas = resultadosBusquedaCaja.getContent();
    assertEquals(1, cajasRecuperadas.size());
    assertEquals(EstadoCaja.ABIERTA, cajasRecuperadas.getFirst().getEstado());
    restTemplate.put(
        "/api/v1/cajas/" + cajasRecuperadas.getFirst().getIdCaja() + "/cierre?monto=5276.66",
        null);
    requestEntityParaProveedores = new HttpEntity<>(criteriaParaBusquedaCaja);
    resultadosBusquedaCaja =
        restTemplate.exchange(
                "/api/v1/cajas/busqueda/criteria",
                HttpMethod.POST,
                requestEntityParaProveedores,
                new ParameterizedTypeReference<PaginaRespuestaRest<CajaTest>>() {})
            .getBody();
    assertNotNull(resultadosBusquedaCaja);
    cajasRecuperadas = resultadosBusquedaCaja.getContent();
    assertEquals(1, cajasRecuperadas.size());
    assertEquals(EstadoCaja.CERRADA, cajasRecuperadas.getFirst().getEstado());
  }
}
