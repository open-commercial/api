package sic.integration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
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
import sic.modelo.dto.*;
import sic.repository.UsuarioRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StockIntegrationTest {

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
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("marce")
            .password("marce123")
            .nombre("Marcelo")
            .apellido("Rockefeller")
            .email("marce.r@gmail.com")
            .roles(new ArrayList<>(Arrays.asList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
        ClienteDTO.builder()
            .tipoDeCliente(TipoDeCliente.EMPRESA)
            .bonificacion(BigDecimal.TEN)
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .razonSocial("Peter Parker")
            .telefono("379123452")
            .build();
    restTemplate.postForObject(
        apiPrefix
            + "/clientes?idEmpresa="
            + empresa.getId_Empresa()
            + "&idLocalidad="
            + localidad.getId_Localidad()
            + "&idUsuarioCredencial="
            + credencial.getId_Usuario(),
        cliente,
        ClienteDTO.class);
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

  private void shouldCrearPorductos() {
    ProductoDTO productoUno =
        new ProductoBuilder()
            .withCodigo(RandomStringUtils.random(10, false, true))
            .withDescripcion(RandomStringUtils.random(10, true, false))
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
            .withCodigo(RandomStringUtils.random(10, false, true))
            .withDescripcion(RandomStringUtils.random(10, true, false))
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
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
    Rubro rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", Rubro.class);
    ProveedorDTO proveedor =
        restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
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
  }

  private void shouldCrearFacturaVentaA() {
    this.shouldCrearPorductos();
    ProductoDTO productoUno =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
        restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto="
                + productoUno.getId_Producto()
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_A
                + "&movimiento="
                + Movimiento.VENTA
                + "&cantidad=6"
                + "&descuentoPorcentaje=10",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto="
                + productoDos.getId_Producto()
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_A
                + "&movimiento="
                + Movimiento.VENTA
                + "&cantidad=3"
                + "&descuentoPorcentaje=5",
            RenglonFactura.class);
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglonUno);
    renglones.add(renglonDos);
    int size = renglones.size();
    BigDecimal[] cantidades = new BigDecimal[size];
    BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
    BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
    int indice = 0;
    BigDecimal subTotal = BigDecimal.ZERO;
    for (RenglonFactura renglon : renglones) {
      subTotal = subTotal.add(renglon.getImporte());
      cantidades[indice] = renglon.getCantidad();
      ivaPorcentajeRenglones[indice] = renglon.getIvaPorcentaje();
      ivaNetoRenglones[indice] = renglon.getIvaNeto();
      indice++;
    }
    BigDecimal descuentoPorcentaje = new BigDecimal("25");
    BigDecimal recargoPorcentaje = new BigDecimal("10");
    BigDecimal descuento_neto =
        subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal recargo_neto =
        subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    indice = cantidades.length;
    BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
    BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
    for (int i = 0; i < indice; i++) {
      if (ivaPorcentajeRenglones[i].compareTo(IVA_105) == 0) {
        iva_105_netoFactura =
            iva_105_netoFactura.add(
                cantidades[i].multiply(
                    ivaNetoRenglones[i]
                        .subtract(
                            ivaNetoRenglones[i].multiply(
                                descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(
                            ivaNetoRenglones[i].multiply(
                                recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
      } else if (ivaPorcentajeRenglones[i].compareTo(IVA_21) == 0) {
        iva_21_netoFactura =
            iva_21_netoFactura.add(
                cantidades[i].multiply(
                    ivaNetoRenglones[i]
                        .subtract(
                            ivaNetoRenglones[i].multiply(
                                descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(
                            ivaNetoRenglones[i].multiply(
                                recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
      }
    }
    BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto);
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    FacturaVentaDTO facturaVentaA =
        FacturaVentaDTO.builder()
            .razonSocialCliente(cliente.getRazonSocial())
            .nombreUsuario("test test (test)")
            .build();
    facturaVentaA.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    facturaVentaA.setRenglones(renglones);
    facturaVentaA.setSubTotal(subTotal);
    facturaVentaA.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaA.setRecargoNeto(recargo_neto);
    facturaVentaA.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaA.setDescuentoNeto(descuento_neto);
    facturaVentaA.setSubTotalBruto(subTotalBruto);
    facturaVentaA.setIva105Neto(iva_105_netoFactura);
    facturaVentaA.setIva21Neto(iva_21_netoFactura);
    facturaVentaA.setTotal(total);
    Transportista transportista =
        restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix
                + "/facturas/venta?"
                + "idCliente="
                + cliente.getId_Cliente()
                + "&idEmpresa="
                + empresa.getId_Empresa()
                + "&idUsuario="
                + credencial.getId_Usuario()
                + "&idTransportista="
                + transportista.getId_Transportista(),
            facturaVentaA,
            FacturaVentaDTO[].class);
    facturaVentaA.setId_Factura(facturas[0].getId_Factura());
    facturaVentaA.setFecha(facturas[0].getFecha());
    assertEquals(facturaVentaA, facturas[0]);
  }

  public void shouldCrearFacturaCompraA() {
    this.shouldCrearPorductos();
    RenglonFactura renglonUno =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=1"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_A
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=4"
                + "&descuentoPorcentaje=20",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=2"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_A
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=3"
                + "&descuentoPorcentaje=0",
            RenglonFactura.class);
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglonUno);
    renglones.add(renglonDos);
    int size = renglones.size();
    BigDecimal[] cantidades = new BigDecimal[size];
    BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
    BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
    int indice = 0;
    BigDecimal subTotal = BigDecimal.ZERO;
    for (RenglonFactura renglon : renglones) {
      subTotal = subTotal.add(renglon.getImporte());
      cantidades[indice] = renglon.getCantidad();
      ivaPorcentajeRenglones[indice] = renglon.getIvaPorcentaje();
      ivaNetoRenglones[indice] = renglon.getIvaNeto();
      indice++;
    }
    BigDecimal descuentoPorcentaje = new BigDecimal("25");
    BigDecimal recargoPorcentaje = BigDecimal.TEN;
    BigDecimal descuento_neto =
        subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal recargo_neto =
        subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    indice = cantidades.length;
    BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
    BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
    for (int i = 0; i < indice; i++) {
      if (ivaPorcentajeRenglones[i].compareTo(IVA_105) == 0) {
        iva_105_netoFactura =
            iva_105_netoFactura.add(
                cantidades[i].multiply(
                    ivaNetoRenglones[i]
                        .subtract(
                            ivaNetoRenglones[i].multiply(
                                descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(
                            ivaNetoRenglones[i].multiply(
                                recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
      } else if (ivaPorcentajeRenglones[i].compareTo(IVA_21) == 0) {
        iva_21_netoFactura =
            iva_21_netoFactura.add(
                cantidades[i].multiply(
                    ivaNetoRenglones[i]
                        .subtract(
                            ivaNetoRenglones[i].multiply(
                                descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
                        .add(
                            ivaNetoRenglones[i].multiply(
                                recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
      }
    }
    BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto);
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaCompraDTO facturaCompraA = new FacturaCompraDTO();
    facturaCompraA.setFecha(new Date());
    facturaCompraA.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    facturaCompraA.setRenglones(renglones);
    facturaCompraA.setSubTotal(subTotal);
    facturaCompraA.setRecargoPorcentaje(recargoPorcentaje);
    facturaCompraA.setRecargoNeto(recargo_neto);
    facturaCompraA.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaCompraA.setDescuentoNeto(descuento_neto);
    facturaCompraA.setSubTotalBruto(subTotalBruto);
    facturaCompraA.setIva105Neto(iva_105_netoFactura);
    facturaCompraA.setIva21Neto(iva_21_netoFactura);
    facturaCompraA.setTotal(total);
    restTemplate.postForObject(
        apiPrefix + "/facturas/compra?" + "idProveedor=1&idEmpresa=1&idUsuario=2&idTransportista=1",
        facturaCompraA,
        FacturaCompraDTO[].class);
  }

  private void debeHaberStock(double cantidadProductoUno, double cantidadProductoDos) {
        String uri =
                apiPrefix
                        + "/productos/disponibilidad-stock?idProducto=1,2"
                        + "&cantidad="
                        + cantidadProductoUno
                        + ","
                        + cantidadProductoDos;
        Assert.assertTrue(
                restTemplate
                        .exchange(
                                uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Long, Producto>>() {})
                        .getBody()
                        .isEmpty());
    }

  @Test
  public void testCalcularPreciosDeProductosConRegargo() {
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
                + "/productos?idMedida=1&idRubro=1&idProveedor=1&idEmpresa=1",
            productoUno,
            ProductoDTO.class);
    productoDos =
        restTemplate.postForObject(
            apiPrefix
                + "/productos?idMedida=1&idRubro=1&idProveedor=1&idEmpresa=1",
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

  @Test
  public void shouldCrearPorducto() {
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
    Rubro rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", Rubro.class);
    ProveedorDTO proveedor =
        restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
    ProductoDTO productoUno =
        new ProductoBuilder()
            .withCodigo(RandomStringUtils.random(10, false, true))
            .withDescripcion(RandomStringUtils.random(10, true, false))
            .withCantidad(BigDecimal.TEN)
            .withVentaMinima(BigDecimal.ONE)
            .withPrecioCosto(CIEN)
            .withGanancia_porcentaje(new BigDecimal("900"))
            .withGanancia_neto(new BigDecimal("900"))
            .withPrecioVentaPublico(new BigDecimal("1000"))
            .withIva_porcentaje(new BigDecimal("21.0"))
            .withIva_neto(new BigDecimal("210"))
            .withPrecioLista(new BigDecimal("1210"))
            .withNombreEmpresa(empresa.getNombre())
            .withRazonSocialProveedor(proveedor.getRazonSocial())
            .withNombreRubro(rubro.getNombre())
            .withNombreMedida(medida.getNombre())
            .build();
    ProductoDTO productoRecuperado =
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
    productoUno.setFechaAlta(productoRecuperado.getFechaAlta());
    productoUno.setId_Producto(productoRecuperado.getId_Producto());
    productoUno.setFechaUltimaModificacion(productoRecuperado.getFechaUltimaModificacion());
    assertEquals(productoUno, productoRecuperado);
  }


  @Test
  public void shouldModificarProducto() {
    this.shouldCrearPorducto();
    ProductoDTO productoAModificar =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoAModificar.setDescripcion("PRODUCTO MODIFICADO.");
    productoAModificar.setCantidad(new BigDecimal("52"));
    productoAModificar.setCodigo("666");
    restTemplate.put(apiPrefix + "/productos?idMedida=2", productoAModificar);
    productoAModificar = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    assertEquals("PRODUCTO MODIFICADO.", productoAModificar.getDescripcion());
    assertEquals(new BigDecimal("52.000000000000000"), productoAModificar.getCantidad());
    assertEquals("666", productoAModificar.getCodigo());
    assertEquals("Kilo", productoAModificar.getNombreMedida());
  }

  @Test(expected = RestClientResponseException.class)
  public void shouldEliminarProducto() {
    this.shouldCrearPorducto();
    restTemplate.delete(apiPrefix + "/productos?idProducto=1");
    restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
  }

  @Test
  public void shouldCrearYEliminarFacturaVenta() {
    this.shouldCrearFacturaVentaA();
    this.debeHaberStock(4, 3);
    restTemplate.delete(apiPrefix + "/facturas?idFactura=1");
    this.debeHaberStock(10, 6);
  }

  @Test
  public void shouldCrearYEliminarFacturaCompra() {
    this.shouldCrearFacturaCompraA();
    this.debeHaberStock(14, 9);
    restTemplate.delete(apiPrefix + "/facturas?idFactura=1");
    this.debeHaberStock(10, 6);
  }
}
