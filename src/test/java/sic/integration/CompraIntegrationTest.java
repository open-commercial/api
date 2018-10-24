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

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompraIntegrationTest {

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
    //        ClienteDTO cliente =
    //                ClienteDTO.builder()
    //                        .tipoDeCliente(TipoDeCliente.EMPRESA)
    //                        .bonificacion(BigDecimal.TEN)
    //                        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
    //                        .razonSocial("Peter Parker")
    //                        .telefono("379123452")
    //                        .build();
    //        restTemplate.postForObject(
    //                apiPrefix
    //                        + "/clientes?idEmpresa="
    //                        + empresa.getId_Empresa()
    //                        + "&idLocalidad="
    //                        + localidad.getId_Localidad()
    //                        + "&idUsuarioCredencial="
    //                        + credencial.getId_Usuario(),
    //                cliente,
    //                ClienteDTO.class);
    Transportista transportista =
        new TransportistaBuilder()
            .withEmpresa(empresa)
            .withLocalidad(empresa.getLocalidad())
            .build();
    restTemplate.postForObject(apiPrefix + "/transportistas", transportista, Transportista.class);
    Medida medida = new MedidaBuilder().withEmpresa(empresa).build();
    restTemplate.postForObject(apiPrefix + "/medidas", medida, Medida.class);
    Proveedor proveedor =
        new ProveedorBuilder().withEmpresa(empresa).withLocalidad(empresa.getLocalidad()).build();
    restTemplate.postForObject(apiPrefix + "/proveedores", proveedor, Proveedor.class);
    Rubro rubro = new RubroBuilder().withEmpresa(empresa).build();
    restTemplate.postForObject(apiPrefix + "/rubros", rubro, Rubro.class);
  }

  private void shouldCrearPorductos(String[] codigos) {
    ProductoDTO productoUno =
        new ProductoBuilder()
            .withCodigo(codigos[0])
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
            .withCodigo(codigos[1])
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
    this.debeHaberStock(1l, 10, 2l, 6);
  }

  private void debeHaberStock(
      long idProductoUno,
      double cantidadProductoUno,
      long idProductoDos,
      double cantidadProductoDos) {
    String uri =
        apiPrefix
            + "/productos/disponibilidad-stock?idProducto="
            + idProductoUno
            + ","
            + idProductoDos
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
  public void shouldCrearFacturaCompraA() {
      this.shouldCrearPorductos(new String[]{"4","6"});
      RenglonFactura renglonUno =
              restTemplate.getForObject(
                      apiPrefix
                              + "/facturas/renglon?"
                              + "idProducto=1"
                              + "&tipoDeComprobante="
                              + TipoDeComprobante.FACTURA_A
                              + "&movimiento="
                              + Movimiento.COMPRA
                              + "&cantidad=5"
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
                              + "&cantidad=2"
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
      BigDecimal subTotalBruto =
              subTotal
                      .add(recargo_neto)
                      .subtract(descuento_neto);
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
      String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=1,2&cantidad=15,8";
      Assert.assertTrue(
              restTemplate
                      .exchange(
                              uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Long, Producto>>() {})
                      .getBody()
                      .isEmpty());
  }

  @Test
  public void shouldCrearFacturaCompraB() {
    this.shouldCrearPorductos(new String[] {"4", "6"});
    RenglonFactura renglonUno =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=1"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_B
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=5"
                + "&descuentoPorcentaje=20",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=2"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_B
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=2"
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
    BigDecimal subTotalBruto =
        subTotal
            .add(recargo_neto)
            .subtract(descuento_neto)
            .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaCompraDTO facturaCompraB = new FacturaCompraDTO();
    facturaCompraB.setFecha(new Date());
    facturaCompraB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
    facturaCompraB.setRenglones(renglones);
    facturaCompraB.setSubTotal(subTotal);
    facturaCompraB.setRecargoPorcentaje(recargoPorcentaje);
    facturaCompraB.setRecargoNeto(recargo_neto);
    facturaCompraB.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaCompraB.setDescuentoNeto(descuento_neto);
    facturaCompraB.setSubTotalBruto(subTotalBruto);
    facturaCompraB.setIva105Neto(iva_105_netoFactura);
    facturaCompraB.setIva21Neto(iva_21_netoFactura);
    facturaCompraB.setTotal(total);
    restTemplate.postForObject(
        apiPrefix + "/facturas/compra?" + "idProveedor=1&idEmpresa=1&idUsuario=2&idTransportista=1",
        facturaCompraB,
        FacturaCompraDTO[].class);
    String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=1,2&cantidad=15,8";
    Assert.assertTrue(
        restTemplate
            .exchange(
                uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Long, Producto>>() {})
            .getBody()
            .isEmpty());
  }

  @Test
  public void shouldCrearFacturaCompraC() {
    this.shouldCrearPorductos(new String[] {"4", "6"});
    RenglonFactura renglonUno =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=1"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_C
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=5"
                + "&descuentoPorcentaje=20",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=2"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_C
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=2"
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
    BigDecimal subTotalBruto =
        subTotal
            .add(recargo_neto)
            .subtract(descuento_neto)
            .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaCompraDTO facturaCompraC = new FacturaCompraDTO();
    facturaCompraC.setFecha(new Date());
    facturaCompraC.setTipoComprobante(TipoDeComprobante.FACTURA_C);
    facturaCompraC.setRenglones(renglones);
    facturaCompraC.setSubTotal(subTotal);
    facturaCompraC.setRecargoPorcentaje(recargoPorcentaje);
    facturaCompraC.setRecargoNeto(recargo_neto);
    facturaCompraC.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaCompraC.setDescuentoNeto(descuento_neto);
    facturaCompraC.setSubTotalBruto(subTotalBruto);
    facturaCompraC.setIva105Neto(iva_105_netoFactura);
    facturaCompraC.setIva21Neto(iva_21_netoFactura);
    facturaCompraC.setTotal(total);
    restTemplate.postForObject(
        apiPrefix + "/facturas/compra?" + "idProveedor=1&idEmpresa=1&idUsuario=2&idTransportista=1",
        facturaCompraC,
        FacturaCompraDTO[].class);
    String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=1,2&cantidad=15,8";
    Assert.assertTrue(
        restTemplate
            .exchange(
                uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Long, Producto>>() {})
            .getBody()
            .isEmpty());
  }

  @Test
  public void shouldCrearFacturaCompraX() {
    this.shouldCrearPorductos(new String[] {"4", "6"});
    RenglonFactura renglonUno =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=1"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_X
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=5"
                + "&descuentoPorcentaje=20",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=2"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_X
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=2"
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
    BigDecimal subTotalBruto =
        subTotal
            .add(recargo_neto)
            .subtract(descuento_neto)
            .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaCompraDTO facturaCompraX = new FacturaCompraDTO();
    facturaCompraX.setFecha(new Date());
    facturaCompraX.setTipoComprobante(TipoDeComprobante.FACTURA_X);
    facturaCompraX.setRenglones(renglones);
    facturaCompraX.setSubTotal(subTotal);
    facturaCompraX.setRecargoPorcentaje(recargoPorcentaje);
    facturaCompraX.setRecargoNeto(recargo_neto);
    facturaCompraX.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaCompraX.setDescuentoNeto(descuento_neto);
    facturaCompraX.setSubTotalBruto(subTotalBruto);
    facturaCompraX.setIva105Neto(BigDecimal.ZERO);
    facturaCompraX.setIva21Neto(BigDecimal.ZERO);
    facturaCompraX.setTotal(total);
    restTemplate.postForObject(
        apiPrefix + "/facturas/compra?" + "idProveedor=1&idEmpresa=1&idUsuario=2&idTransportista=1",
        facturaCompraX,
        FacturaCompraDTO[].class);
    String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=1,2&cantidad=15,8";
    Assert.assertTrue(
        restTemplate
            .exchange(
                uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Long, Producto>>() {})
            .getBody()
            .isEmpty());
  }

  private void shouldCrearFacturaCompraB(String[] codigosProducto) {
    this.shouldCrearPorductos(codigosProducto);
    RenglonFactura renglonUno =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=1"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_B
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=5"
                + "&descuentoPorcentaje=20",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto=2"
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_B
                + "&movimiento="
                + Movimiento.COMPRA
                + "&cantidad=2"
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
    BigDecimal subTotalBruto =
        subTotal
            .add(recargo_neto)
            .subtract(descuento_neto)
            .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaCompraDTO facturaCompraB = new FacturaCompraDTO();
    facturaCompraB.setFecha(new Date());
    facturaCompraB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
    facturaCompraB.setRenglones(renglones);
    facturaCompraB.setSubTotal(subTotal);
    facturaCompraB.setRecargoPorcentaje(recargoPorcentaje);
    facturaCompraB.setRecargoNeto(recargo_neto);
    facturaCompraB.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaCompraB.setDescuentoNeto(descuento_neto);
    facturaCompraB.setSubTotalBruto(subTotalBruto);
    facturaCompraB.setIva105Neto(iva_105_netoFactura);
    facturaCompraB.setIva21Neto(iva_21_netoFactura);
    facturaCompraB.setTotal(total);
    restTemplate.postForObject(
        apiPrefix + "/facturas/compra?" + "idProveedor=1&idEmpresa=1&idUsuario=2&idTransportista=1",
        facturaCompraB,
        FacturaCompraDTO[].class);

    String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=1,2&cantidad=15,8";
    Assert.assertTrue(
        restTemplate
            .exchange(
                uri, HttpMethod.GET, null, new ParameterizedTypeReference<Map<Long, Producto>>() {})
            .getBody()
            .isEmpty());
  }

  private void shouldCrearReciboProveedor(double monto) {
    ReciboDTO r = new ReciboDTO();
    r.setMonto(monto);
    restTemplate.postForObject(
        apiPrefix
            + "/recibos/proveedores?"
            + "idUsuario=2&idEmpresa=1&idProveedor=1&idFormaDePago=1",
        r,
        Recibo.class);
  }

  private void shouldCrearNotaDebitoProveedor() {
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    NotaDebitoDTO notaDebito = new NotaDebitoDTO();
    notaDebito.setCAE(0L);
    notaDebito.setFecha(new Date());
    List<RenglonNotaDebito> renglonesCalculados =
        Arrays.asList(
            restTemplate.getForObject(
                apiPrefix + "/notas/renglon/debito/recibo/3?monto=1000&ivaPorcentaje=21",
                RenglonNotaDebito[].class));
    notaDebito.setRenglonesNotaDebito(renglonesCalculados);
    notaDebito.setIva105Neto(BigDecimal.ZERO);
    notaDebito.setIva21Neto(new BigDecimal("21"));
    notaDebito.setMontoNoGravado(new BigDecimal("200"));
    notaDebito.setMotivo("Test alta nota debito - Cheque rechazado");
    notaDebito.setSubTotalBruto(new BigDecimal("100"));
    notaDebito.setTotal(new BigDecimal("321"));
    notaDebito.setUsuario(credencial);
    notaDebito.setTipoComprobante(TipoDeComprobante.NOTA_DEBITO_B);
    restTemplate.postForObject(
        apiPrefix + "/notas/debito/empresa/1/usuario/1/recibo/3?idProveedor=1&movimiento=COMPRA",
        notaDebito,
        NotaDebito.class);
  }

  private void shouldCearNotaCreditoProveedor(
      TipoDeComprobante tipoDeComprobante, long idRenglonFactura, double cantidad) {
    List<RenglonNotaCredito> renglonesNotaCredito =
        Arrays.asList(
            restTemplate.getForObject(
                apiPrefix
                    + "/notas/renglon/credito/producto?"
                    + "tipoDeComprobante="
                    + tipoDeComprobante
                    + "&cantidad="
                    + cantidad
                    + "&idRenglonFactura="
                    + idRenglonFactura,
                RenglonNotaCredito[].class));
    NotaCreditoDTO notaCreditoProveedor = new NotaCreditoDTO();
    notaCreditoProveedor.setRenglonesNotaCredito(renglonesNotaCredito);
    notaCreditoProveedor.setFecha(new Date());
    notaCreditoProveedor.setModificaStock(true);
    notaCreditoProveedor.setSubTotal(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/sub-total?importe="
                + renglonesNotaCredito.get(0).getImporteNeto(),
            BigDecimal.class));
    notaCreditoProveedor.setRecargoPorcentaje(BigDecimal.TEN);
    notaCreditoProveedor.setRecargoNeto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/recargo-neto?subTotal="
                + notaCreditoProveedor.getSubTotal()
                + "&recargoPorcentaje="
                + notaCreditoProveedor.getRecargoPorcentaje(),
            BigDecimal.class));
    notaCreditoProveedor.setDescuentoPorcentaje(new BigDecimal("25"));
    notaCreditoProveedor.setDescuentoNeto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/descuento-neto?subTotal="
                + notaCreditoProveedor.getSubTotal()
                + "&descuentoPorcentaje="
                + notaCreditoProveedor.getDescuentoPorcentaje(),
            BigDecimal.class));
    notaCreditoProveedor.setIva21Neto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/iva-neto?"
                + "tipoDeComprobante="
                + tipoDeComprobante
                + "&cantidades="
                + renglonesNotaCredito.get(0).getCantidad()
                + "&ivaPorcentajeRenglones="
                + renglonesNotaCredito.get(0).getIvaPorcentaje()
                + "&ivaNetoRenglones="
                + renglonesNotaCredito.get(0).getIvaNeto()
                + "&ivaPorcentaje=21"
                + "&descuentoPorcentaje=25"
                + "&recargoPorcentaje=10",
            BigDecimal.class));
    notaCreditoProveedor.setIva105Neto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/iva-neto?"
                + "tipoDeComprobante="
                + tipoDeComprobante
                + "&cantidades="
                + renglonesNotaCredito.get(0).getCantidad()
                + "&ivaPorcentajeRenglones="
                + renglonesNotaCredito.get(0).getIvaPorcentaje()
                + "&ivaNetoRenglones="
                + renglonesNotaCredito.get(0).getIvaNeto()
                + "&ivaPorcentaje=10.5"
                + "&descuentoPorcentaje=25"
                + "&recargoPorcentaje=10",
            BigDecimal.class));
    notaCreditoProveedor.setSubTotalBruto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/sub-total-bruto?"
                + "tipoDeComprobante="
                + tipoDeComprobante
                + "&subTotal="
                + notaCreditoProveedor.getSubTotal()
                + "&recargoNeto="
                + notaCreditoProveedor.getRecargoNeto()
                + "&descuentoNeto="
                + notaCreditoProveedor.getDescuentoNeto()
                + "&iva21Neto="
                + notaCreditoProveedor.getIva21Neto()
                + "&iva105Neto="
                + notaCreditoProveedor.getIva105Neto(),
            BigDecimal.class));
    notaCreditoProveedor.setTotal(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/total?subTotalBruto="
                + notaCreditoProveedor.getSubTotalBruto()
                + "&iva21Neto="
                + notaCreditoProveedor.getIva21Neto()
                + "&iva105Neto="
                + notaCreditoProveedor.getIva105Neto(),
            BigDecimal.class));
    notaCreditoProveedor.setMotivo("Devolución");
    restTemplate.postForObject(
        apiPrefix
            + "/notas/credito/empresa/1/usuario/1/factura/2?idProveedor=1&movimiento=COMPRA&modificarStock=true",
        notaCreditoProveedor,
        NotaCredito.class);
  }

  @Test
  public void shouldTestSaldoCuentaCorrienteProveedor() {
    this.shouldCrearFacturaCompraB(new String[] {"1", "2"});
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(
                    apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("-599.25"))
            == 0);
    this.shouldCrearReciboProveedor(599.25);
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(
                    apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
                .compareTo(BigDecimal.ZERO)
            == 0);
    restTemplate.delete(apiPrefix + "/recibos/1");
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(
                    apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("-599.25"))
            == 0);
    this.shouldCrearReciboProveedor(499.25);
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(
                    apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("-100"))
            == 0);
    this.shouldCrearReciboProveedor(200);
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(
                    apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("100"))
            == 0);
    restTemplate.delete(apiPrefix + "/facturas?idFactura=1");
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(
                    apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("699.25"))
            == 0);
    this.debeHaberStock(1l, 10, 2l, 6);
    List<RenglonCuentaCorriente> renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/1/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(0).getSaldo() == 699.25);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(1).getSaldo() == 499.25);
    this.shouldCrearFacturaCompraB(new String[] {"3", "4"});
    this.shouldCearNotaCreditoProveedor(TipoDeComprobante.FACTURA_B, 3l, 5);
    renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/1/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(0).getSaldo() == 511.4);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(1).getSaldo() == 100.0);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(2).getSaldo() == 699.25);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(3).getSaldo() == 499.25);
    this.shouldCrearNotaDebitoProveedor();
    renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/1/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(0).getSaldo() == 190.40);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(1).getSaldo() == 511.40);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(2).getSaldo() == 100.0);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(3).getSaldo() == 699.25);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(4).getSaldo() == 499.25);
    restTemplate.delete(apiPrefix + "/notas/?idsNota=2");
    renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/1/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(0).getSaldo() == 511.40);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(1).getSaldo() == 100.0);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(2).getSaldo() == 699.25);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(3).getSaldo() == 499.25);
    restTemplate.delete(apiPrefix + "/notas/?idsNota=1");
    renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/1/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(0).getSaldo() == 100.00);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(1).getSaldo() == 699.25);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(2).getSaldo() == 499.25);
  }

}
