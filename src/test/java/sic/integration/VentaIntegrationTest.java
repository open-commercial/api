package sic.integration;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
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
import sic.builder.EmpresaBuilder;
import sic.builder.FormaDePagoBuilder;
import sic.builder.LocalidadBuilder;
import sic.builder.MedidaBuilder;
import sic.builder.ProductoBuilder;
import sic.builder.ProveedorBuilder;
import sic.builder.RubroBuilder;
import sic.builder.TransportistaBuilder;
import sic.builder.UsuarioBuilder;
import sic.modelo.*;
import sic.modelo.dto.*;
import sic.repository.UsuarioRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class VentaIntegrationTest {

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

  @Test
  public void shouldCrearFacturaVentaA() {
    this.shouldCrearPorductos(new String[] {"1", "2"});
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
    BigDecimal subTotalBruto =
        subTotal
            .add(recargo_neto)
            .subtract(descuento_neto)
            .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaVentaDTO facturaVentaB = new FacturaVentaDTO();
    facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
    facturaVentaB.setRenglones(renglones);
    facturaVentaB.setSubTotal(subTotal);
    facturaVentaB.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaB.setRecargoNeto(recargo_neto);
    facturaVentaB.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaB.setDescuentoNeto(descuento_neto);
    facturaVentaB.setSubTotalBruto(subTotalBruto);
    facturaVentaB.setIva105Neto(iva_105_netoFactura);
    facturaVentaB.setIva21Neto(iva_21_netoFactura);
    facturaVentaB.setTotal(total);
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    Transportista transportista =
        restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
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
        facturaVentaB,
        FacturaVenta[].class);
    this.debeHaberStock(1l, 4, 2l, 3);
  }

  @Test
  public void shouldCrearFacturaVentaB() {
    this.shouldCrearPorductos(new String[] {"1", "2"});
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
                + TipoDeComprobante.FACTURA_B
                + "&movimiento="
                + Movimiento.VENTA
                + "&cantidad=5"
                + "&descuentoPorcentaje=20",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon?"
                + "idProducto="
                + productoDos.getId_Producto()
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_B
                + "&movimiento="
                + Movimiento.VENTA
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
    BigDecimal subTotalBruto =
        subTotal
            .add(recargo_neto)
            .subtract(descuento_neto)
            .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaVentaDTO facturaVentaB = new FacturaVentaDTO();
    facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
    facturaVentaB.setRenglones(renglones);
    facturaVentaB.setSubTotal(subTotal);
    facturaVentaB.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaB.setRecargoNeto(recargo_neto);
    facturaVentaB.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaB.setDescuentoNeto(descuento_neto);
    facturaVentaB.setSubTotalBruto(subTotalBruto);
    facturaVentaB.setIva105Neto(iva_105_netoFactura);
    facturaVentaB.setIva21Neto(iva_21_netoFactura);
    facturaVentaB.setTotal(total);
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    Transportista transportista =
        restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
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
        facturaVentaB,
        FacturaVenta[].class);
    this.debeHaberStock(1l, 5, 2l, 4);
  }

  @Test
  public void shouldCrearFacturaVentaC() {
    this.shouldCrearPorductos(new String[] {"1", "2"});
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
                            + TipoDeComprobante.FACTURA_C
                            + "&movimiento="
                            + Movimiento.VENTA
                            + "&cantidad=5"
                            + "&descuentoPorcentaje=20",
                    RenglonFactura.class);
    RenglonFactura renglonDos =
            restTemplate.getForObject(
                    apiPrefix
                            + "/facturas/renglon?"
                            + "idProducto="
                            + productoDos.getId_Producto()
                            + "&tipoDeComprobante="
                            + TipoDeComprobante.FACTURA_C
                            + "&movimiento="
                            + Movimiento.VENTA
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
    BigDecimal subTotalBruto =
            subTotal
                    .add(recargo_neto)
                    .subtract(descuento_neto)
                    .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaVentaDTO facturaVentaC = new FacturaVentaDTO();
    facturaVentaC.setTipoComprobante(TipoDeComprobante.FACTURA_C);
    facturaVentaC.setRenglones(renglones);
    facturaVentaC.setSubTotal(subTotal);
    facturaVentaC.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaC.setRecargoNeto(recargo_neto);
    facturaVentaC.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaC.setDescuentoNeto(descuento_neto);
    facturaVentaC.setSubTotalBruto(subTotalBruto);
    facturaVentaC.setIva105Neto(iva_105_netoFactura);
    facturaVentaC.setIva21Neto(iva_21_netoFactura);
    facturaVentaC.setTotal(total);
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    Transportista transportista =
            restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
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
            facturaVentaC,
            FacturaVenta[].class);
    this.debeHaberStock(1l, 5, 2l, 4);
  }

  @Test
  public void shouldCrearFacturaVentaX() {
    this.shouldCrearPorductos(new String[] {"1", "2"});
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
                + TipoDeComprobante.FACTURA_X
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
                + TipoDeComprobante.FACTURA_X
                + "&movimiento="
                + Movimiento.VENTA
                + "&cantidad=3"
                + "&descuentoPorcentaje=5",
            RenglonFactura.class);
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglonUno);
    renglones.add(renglonDos);
    BigDecimal subTotal = BigDecimal.ZERO;
    for (RenglonFactura renglon : renglones) {
      subTotal = subTotal.add(renglon.getImporte());
    }
    BigDecimal descuentoPorcentaje = new BigDecimal("25");
    BigDecimal recargoPorcentaje = new BigDecimal("10");
    BigDecimal descuento_neto =
        subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal recargo_neto =
        subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(descuento_neto);
    FacturaVentaDTO facturaVentaX = new FacturaVentaDTO();
    facturaVentaX.setTipoComprobante(TipoDeComprobante.FACTURA_X);
    facturaVentaX.setRenglones(renglones);
    facturaVentaX.setSubTotal(subTotal);
    facturaVentaX.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaX.setRecargoNeto(recargo_neto);
    facturaVentaX.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaX.setDescuentoNeto(descuento_neto);
    facturaVentaX.setSubTotalBruto(subTotalBruto);
    facturaVentaX.setIva105Neto(BigDecimal.ZERO);
    facturaVentaX.setIva21Neto(BigDecimal.ZERO);
    facturaVentaX.setTotal(subTotalBruto);
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    Transportista transportista =
        restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
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
        facturaVentaX,
        FacturaVenta[].class);
    this.debeHaberStock(1l, 4, 2l, 3);
  }

  @Test
  public void shouldCrearFacturaVentaY() {
    this.shouldCrearPorductos(new String[] {"1", "2"});
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
                            + TipoDeComprobante.FACTURA_Y
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
                            + TipoDeComprobante.FACTURA_Y
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
    BigDecimal subTotalBruto =
            subTotal
                    .add(recargo_neto)
                    .subtract(descuento_neto)
                    .subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    FacturaVentaDTO facturaVentaB = new FacturaVentaDTO();
    facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_Y);
    facturaVentaB.setRenglones(renglones);
    facturaVentaB.setSubTotal(subTotal);
    facturaVentaB.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaB.setRecargoNeto(recargo_neto);
    facturaVentaB.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaB.setDescuentoNeto(descuento_neto);
    facturaVentaB.setSubTotalBruto(subTotalBruto);
    facturaVentaB.setIva105Neto(iva_105_netoFactura);
    facturaVentaB.setIva21Neto(iva_21_netoFactura);
    facturaVentaB.setTotal(total);
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    Transportista transportista =
            restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    EmpresaDTO empresa = restTemplate.getForObject(apiPrefix + "/empresas/1", EmpresaDTO.class);
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
            facturaVentaB,
            FacturaVenta[].class);
    this.debeHaberStock(1l, 4, 2l, 3);
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

  private void shouldCrearFacturaADePedido() {
    RenglonFactura[] renglonesParaFacturar = restTemplate.getForObject(apiPrefix + "/facturas/renglones/pedidos/1"
            + "?tipoDeComprobante=" + TipoDeComprobante.FACTURA_A, RenglonFactura[].class);
    BigDecimal subTotal = renglonesParaFacturar[0].getImporte();
    assertTrue("La importe no es el esperado", renglonesParaFacturar[0].getImporte().compareTo(new BigDecimal("4250")) == 0);
    BigDecimal recargoPorcentaje = BigDecimal.TEN;
    BigDecimal recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    assertTrue("El recargo neto no es el esperado" + recargo_neto.doubleValue(), recargo_neto.compareTo(new BigDecimal("425")) == 0);
    BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
    BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
    if (renglonesParaFacturar[0].getIvaPorcentaje().compareTo(IVA_105) == 0) {
      iva_105_netoFactura = iva_105_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply(renglonesParaFacturar[0].getIvaNeto()));
    } else if (renglonesParaFacturar[0].getIvaPorcentaje().compareTo(IVA_21) == 0) {
      iva_21_netoFactura = iva_21_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply(renglonesParaFacturar[0].getIvaNeto()));
    }
    assertTrue("El iva 10.5 neto no es el esperado", iva_105_netoFactura.compareTo(BigDecimal.ZERO) == 0);
    assertTrue("El iva 21 neto no es el esperado", iva_21_netoFactura.compareTo(new BigDecimal("892.5")) == 0);
    BigDecimal subTotalBruto = subTotal.add(recargo_neto);
    assertTrue("El sub total bruto no es el esperado", subTotalBruto.compareTo(new BigDecimal("4675")) == 0);
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    assertTrue("El total no es el esperado", total.compareTo(new BigDecimal("5567.5")) == 0);
    FacturaVentaDTO facturaVentaA = new FacturaVentaDTO();
    facturaVentaA.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglonesParaFacturar[0]);
    facturaVentaA.setRenglones(renglones);
    facturaVentaA.setSubTotal(subTotal);
    facturaVentaA.setRecargoNeto(recargo_neto);
    facturaVentaA.setSubTotalBruto(subTotalBruto);
    facturaVentaA.setIva105Neto(iva_105_netoFactura);
    facturaVentaA.setIva21Neto(iva_21_netoFactura);
    facturaVentaA.setTotal(total);
    facturaVentaA.setFecha(new Date());
    restTemplate.postForObject(apiPrefix + "/facturas/venta?idPedido=1"
            + "&idsFormaDePago=1"
            + "&montos=" + total
            + "&idCliente=1&idEmpresa=1&idUsuario=2&idTransportista=1", facturaVentaA, FacturaVenta[].class);
  }

  private void shouldCrearFacturaBDePedido() {
    RenglonFactura[] renglonesParaFacturar = restTemplate.getForObject(apiPrefix + "/facturas/renglones/pedidos/1"
            + "?tipoDeComprobante=" + TipoDeComprobante.FACTURA_B, RenglonFactura[].class);
    FacturaVentaDTO facturaVentaB = new FacturaVentaDTO();
    facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
    List<RenglonFactura> renglonesDeFactura = new ArrayList<>();
    renglonesDeFactura.add(renglonesParaFacturar[0]);
    facturaVentaB.setRenglones(renglonesDeFactura);
    facturaVentaB.setSubTotal(new BigDecimal("2210"));
    facturaVentaB.setRecargoPorcentaje(BigDecimal.TEN);
    facturaVentaB.setRecargoNeto(new BigDecimal("221"));
    facturaVentaB.setSubTotalBruto(new BigDecimal("2200"));
    facturaVentaB.setIva105Neto(new BigDecimal("231"));
    facturaVentaB.setIva21Neto(BigDecimal.ZERO);
    facturaVentaB.setTotal(new BigDecimal("2431"));
    facturaVentaB.setFecha(new Date());
    restTemplate.postForObject(apiPrefix + "/facturas/venta?idPedido=1"
            + "&idCliente=1&idEmpresa=1&idUsuario=2&idTransportista=1", facturaVentaB, FacturaVenta[].class);
    RenglonPedidoDTO[] renglonesDelPedido = restTemplate.getForObject(apiPrefix + "/pedidos/1/renglones", RenglonPedidoDTO[].class);
    assertTrue("La cantidad no es la esperada", renglonesDeFactura.get(0).getCantidad().compareTo(renglonesDelPedido[1].getCantidad()) == 0);
    assertTrue("El porcentaje de descuento no es la esperado", renglonesDeFactura.get(0).getDescuentoPorcentaje().compareTo(renglonesDelPedido[1].getDescuentoPorcentaje()) == 0);
    assertTrue("El descuento no es el esperado", renglonesDeFactura.get(0).getDescuentoNeto().compareTo(renglonesDelPedido[1].getDescuentoNeto()) == 0);
  }

  private void shouldCrearReciboCliente(double monto) {
    ReciboDTO r = new ReciboDTO();
    r.setMonto(monto);
    restTemplate.postForObject(
        apiPrefix
            + "/recibos/clientes?"
            + "idUsuario=1"
            + "&idEmpresa=1"
            + "&idCliente=1"
            + "&idFormaDePago=1",
        r,
        Recibo.class);
  }

  private void shouldCrearNotaDebitoCliente() {
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    NotaDebitoDTO notaDebitoCliente = new NotaDebitoDTO();
    List<RenglonNotaDebito> renglonesCalculados =
        Arrays.asList(
            restTemplate.getForObject(
                apiPrefix + "/notas/renglon/debito/recibo/1?monto=100&ivaPorcentaje=21",
                RenglonNotaDebito[].class));
    notaDebitoCliente.setRenglonesNotaDebito(renglonesCalculados);
    notaDebitoCliente.setIva105Neto(BigDecimal.ZERO);
    notaDebitoCliente.setIva21Neto(new BigDecimal("21"));
    notaDebitoCliente.setMontoNoGravado(new BigDecimal("5992.5"));
    notaDebitoCliente.setMotivo("Test alta nota debito - Cheque rechazado");
    notaDebitoCliente.setSubTotalBruto(new BigDecimal("100"));
    notaDebitoCliente.setTotal(new BigDecimal("6113.5"));
    notaDebitoCliente.setUsuario(credencial);
    restTemplate.postForObject(
        apiPrefix + "/notas/debito/empresa/1/usuario/1/recibo/1?idCliente=1&movimiento=VENTA",
        notaDebitoCliente,
        Nota.class);
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  private void shouldCrearNotaCreditoCliente(TipoDeComprobante tipoDeComprobante, long nroSerie, long nroFactura,
                                             long idRenglonFactura, double cantidad) {
    List<FacturaVenta> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix
                    + "/facturas/venta/busqueda/criteria?idEmpresa=1" +
                        "&tipoFactura=" + tipoDeComprobante
                        + "&nroSerie=" + nroSerie
                        + "&nroFactura=" + nroFactura,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {})
            .getBody()
            .getContent();
    List<RenglonNotaCredito> renglonesNotaCredito =
        Arrays.asList(
            restTemplate.getForObject(
                apiPrefix
                    + "/notas/renglon/credito/producto?"
                    + "tipoDeComprobante="
                    + facturasRecuperadas.get(0).getTipoComprobante().name()
                    + "&cantidad=" + cantidad
                    + "&idRenglonFactura=" + idRenglonFactura,
                RenglonNotaCredito[].class));
    NotaCreditoDTO notaCredito = new NotaCreditoDTO();
    notaCredito.setRenglonesNotaCredito(renglonesNotaCredito);
    notaCredito.setSubTotal(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/sub-total?importe="
                + renglonesNotaCredito.get(0).getImporteNeto(),
            BigDecimal.class));
    notaCredito.setRecargoPorcentaje(facturasRecuperadas.get(0).getRecargoPorcentaje());
    notaCredito.setRecargoNeto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/recargo-neto?subTotal="
                + notaCredito.getSubTotal()
                + "&recargoPorcentaje="
                + notaCredito.getRecargoPorcentaje(),
            BigDecimal.class));
    notaCredito.setDescuentoPorcentaje(facturasRecuperadas.get(0).getDescuentoPorcentaje());
    notaCredito.setDescuentoNeto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/descuento-neto?subTotal="
                + notaCredito.getSubTotal()
                + "&descuentoPorcentaje="
                + notaCredito.getDescuentoPorcentaje(),
            BigDecimal.class));
    notaCredito.setIva21Neto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/iva-neto?"
                + "tipoDeComprobante="
                + facturasRecuperadas.get(0).getTipoComprobante().name()
                + "&cantidades="
                + renglonesNotaCredito.get(0).getCantidad()
                + "&ivaPorcentajeRenglones="
                + renglonesNotaCredito.get(0).getIvaPorcentaje()
                + "&ivaNetoRenglones="
                + renglonesNotaCredito.get(0).getIvaNeto()
                + "&ivaPorcentaje=21"
                + "&descuentoPorcentaje="
                + facturasRecuperadas.get(0).getDescuentoPorcentaje()
                + "&recargoPorcentaje="
                + facturasRecuperadas.get(0).getRecargoPorcentaje(),
            BigDecimal.class));
    notaCredito.setIva105Neto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/iva-neto?"
                + "tipoDeComprobante="
                + facturasRecuperadas.get(0).getTipoComprobante().name()
                + "&cantidades="
                + renglonesNotaCredito.get(0).getCantidad()
                + "&ivaPorcentajeRenglones="
                + renglonesNotaCredito.get(0).getIvaPorcentaje()
                + "&ivaNetoRenglones="
                + renglonesNotaCredito.get(0).getIvaNeto()
                + "&ivaPorcentaje=10.5"
                + "&descuentoPorcentaje="
                + facturasRecuperadas.get(0).getDescuentoPorcentaje()
                + "&recargoPorcentaje="
                + facturasRecuperadas.get(0).getRecargoPorcentaje(),
            BigDecimal.class));
    notaCredito.setSubTotalBruto(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/sub-total-bruto?"
                + "tipoDeComprobante="
                + facturasRecuperadas.get(0).getTipoComprobante().name()
                + "&subTotal="
                + notaCredito.getSubTotal()
                + "&recargoNeto="
                + notaCredito.getRecargoNeto()
                + "&descuentoNeto="
                + notaCredito.getDescuentoNeto()
                + "&iva21Neto="
                + notaCredito.getIva21Neto()
                + "&iva105Neto="
                + notaCredito.getIva105Neto(),
            BigDecimal.class));
    notaCredito.setTotal(
        restTemplate.getForObject(
            apiPrefix
                + "/notas/credito/total?subTotalBruto="
                + notaCredito.getSubTotalBruto()
                + "&iva21Neto="
                + notaCredito.getIva21Neto()
                + "&iva105Neto="
                + notaCredito.getIva105Neto(),
            BigDecimal.class));
    notaCredito.setMotivo("Devolución");
    restTemplate.postForObject(
        apiPrefix
            + "/notas/credito/empresa/1/usuario/1/factura/1?idCliente=1&movimiento=VENTA&modificarStock=true",
        notaCredito,
        Nota.class);
    restTemplate.getForObject(apiPrefix + "/notas/2/reporte", byte[].class);
    this.debeHaberStock(1l, 10, 2l, 4);
  }

  @Test
  public void shouldTestSaldoCuentaCorrienteCliente() {
    this.shouldCrearFacturaVentaB();
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("-5992.5"))
            == 0);
    this.shouldCrearReciboCliente(5992.5);
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
                .compareTo(BigDecimal.ZERO)
            == 0);
    this.shouldCrearNotaDebitoCliente();
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("-6113.5"))
            == 0);
    this.shouldCrearReciboCliente(6113.5);
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
                .compareTo(BigDecimal.ZERO)
            == 0);
    this.shouldCrearNotaCreditoCliente(TipoDeComprobante.FACTURA_B, 0l, 1l, 1l, 5);
    assertTrue(
        "El saldo de la cuenta corriente no es el esperado",
        restTemplate
                .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
                .compareTo(new BigDecimal("4114"))
            == 0);
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
        renglonesCuentaCorriente.get(0).getSaldo() == 4114);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(1).getSaldo() == 0);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(2).getSaldo() == -6113.5);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(3).getSaldo() == 0);
    assertTrue(
        "El saldo parcial del renglon no es el esperado",
        renglonesCuentaCorriente.get(4).getSaldo() == -5992.5);
    restTemplate.delete(apiPrefix + "/notas?idsNota=2");
    this.debeHaberStock(1l, 5, 2l, 4);
  }

  @Test
  public void shouldCrearPedido() {
    this.shouldCrearPorductos(new String[]{"1","2"});
    List<NuevoRenglonPedidoDTO> renglonesPedidoDTO = new ArrayList();
    renglonesPedidoDTO.add(NuevoRenglonPedidoDTO.builder()
            .idProductoItem(1l)
            .cantidad(new BigDecimal("5"))
            .descuentoPorcentaje(new BigDecimal("15"))
            .build());
    renglonesPedidoDTO.add(NuevoRenglonPedidoDTO.builder()
            .idProductoItem(2l)
            .cantidad(new BigDecimal("2"))
            .descuentoPorcentaje(BigDecimal.ZERO)
            .build());
    List<RenglonPedidoDTO> renglonesPedido = Arrays.asList(restTemplate.postForObject(apiPrefix + "/pedidos/renglones", renglonesPedidoDTO, RenglonPedidoDTO[].class));
    BigDecimal subTotal = BigDecimal.ZERO;
    for (RenglonPedidoDTO renglon : renglonesPedido) {
      subTotal = subTotal.add(renglon.getSubTotal());
    }
    BigDecimal recargoNeto = subTotal.multiply(new BigDecimal("5")).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal descuentoNeto = subTotal.multiply(new BigDecimal("15")).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal total = subTotal.add(recargoNeto).subtract(descuentoNeto);
    NuevoPedidoDTO nuevoPedidoDTO = NuevoPedidoDTO
            .builder().descuentoNeto(descuentoNeto)
            .descuentoPorcentaje(new BigDecimal("15"))
            .recargoNeto(recargoNeto)
            .recargoPorcentaje(new BigDecimal("5"))
            .fechaVencimiento(new Date())
            .observaciones("Nuevo Pedido Test")
            .renglones(renglonesPedido)
            .subTotal(subTotal)
            .total(total)
            .build();
    PedidoDTO pedidoRecuperado = restTemplate.postForObject(apiPrefix + "/pedidos?"
            + "idEmpresa=1"
            + "&idCliente=1"
            + "&idUsuario=2" , nuevoPedidoDTO, PedidoDTO.class);
    assertTrue("El total estimado no es el esperado", pedidoRecuperado.getTotalEstimado().compareTo(nuevoPedidoDTO.getTotal()) == 0);
    assertEquals(nuevoPedidoDTO.getObservaciones(), pedidoRecuperado.getObservaciones());
    assertEquals(pedidoRecuperado.getEstado(), EstadoPedido.ABIERTO);
    RenglonPedidoDTO[] renglonesDelPedido = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido() + "/renglones", RenglonPedidoDTO[].class);
    for (int i = 0; i < renglonesDelPedido.length; i++) {
      assertTrue("La cantidad no es la esperada", renglonesPedido.get(i).getCantidad().compareTo(renglonesDelPedido[i].getCantidad()) == 0);
      assertTrue("El descuento neto no es el esperado", renglonesPedido.get(i).getDescuentoNeto().compareTo(renglonesDelPedido[i].getDescuentoNeto()) == 0);
      assertTrue("El descuento porcentaje no es el esperado", renglonesPedido.get(i).getDescuentoPorcentaje().compareTo(renglonesDelPedido[i].getDescuentoPorcentaje()) == 0);
      assertTrue("La sub total no es el esperado", renglonesPedido.get(i).getSubTotal().compareTo(renglonesDelPedido[i].getSubTotal()) == 0);
    }
  }

  @Test
  public void shouldFacturarPedido() {
    this.shouldCrearPedido();
    this.shouldCrearFacturaADePedido();
    PedidoDTO pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    List<FacturaVenta> facturasRecuperadas = restTemplate
            .exchange(apiPrefix + "/facturas/venta/busqueda/criteria?"
                            + "idEmpresa=1&nroPedido="
                            + pedidoRecuperado.getNroPedido(), HttpMethod.GET, null,
                    new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
                    })
            .getBody().getContent();
    assertEquals(1, facturasRecuperadas.size(), 0);
    pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    RenglonPedidoDTO[] renglonesDelPedido = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido() + "/renglones", RenglonPedidoDTO[].class);
    RenglonFactura[] renglones = restTemplate.getForObject(apiPrefix + "/facturas/1/renglones", RenglonFactura[].class);
    assertTrue("La cantidad no es la esperada", renglones[0].getCantidad().compareTo(renglonesDelPedido[0].getCantidad()) == 0);
    assertTrue("El descuento porcentaje no es el esperado", renglones[0].getDescuentoPorcentaje().compareTo(renglonesDelPedido[0]
            .getDescuentoPorcentaje()) == 0);
    RenglonFactura[] renglonesParaFacturar = restTemplate.getForObject(apiPrefix + "/facturas/renglones/pedidos/" + pedidoRecuperado.getId_Pedido()
            + "?tipoDeComprobante=" + TipoDeComprobante.FACTURA_B, RenglonFactura[].class);
    BigDecimal subTotal = renglonesParaFacturar[0].getImporte();
    assertTrue("La cantidad no es la esperado", subTotal.compareTo(new BigDecimal("2210")) == 0);
    BigDecimal recargo_neto =  subTotal.multiply(BigDecimal.TEN).divide(CIEN, 15, RoundingMode.HALF_UP); // recargoPorcentaje BigDecimal.TEN
    assertTrue("El recargo neto no es la esperado", recargo_neto.compareTo(new BigDecimal("221")) == 0);
    BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
    BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
    if (renglonesParaFacturar[0].getIvaPorcentaje().compareTo(IVA_105) == 0) {
      iva_105_netoFactura = iva_105_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply((renglonesParaFacturar[0].getIvaNeto()
              .add(renglonesParaFacturar[0].getIvaNeto().multiply(BigDecimal.TEN.divide(CIEN, 15, RoundingMode.HALF_UP))))));
    } else if (renglonesParaFacturar[0].getIvaPorcentaje().compareTo(IVA_21) == 0) {
      iva_21_netoFactura = iva_21_netoFactura.add(renglonesParaFacturar[0].getCantidad().multiply((renglonesParaFacturar[0].getIvaNeto()
              .add(renglonesParaFacturar[0].getIvaNeto().multiply(BigDecimal.TEN.divide(CIEN, 15, RoundingMode.HALF_UP))))));
    }
    assertTrue("El iva 10.5 neto no es la esperado", iva_105_netoFactura.compareTo(new BigDecimal("231")) == 0);
    assertTrue("El iva 21 neto no es la esperado", iva_21_netoFactura.compareTo(BigDecimal.ZERO) == 0);
    BigDecimal subTotalBruto = subTotal.add(recargo_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
    assertTrue("El sub total bruto no es la esperado", subTotalBruto.compareTo(new BigDecimal("2200")) == 0);
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    assertTrue("El recargo neto no es la esperado", total.compareTo(new BigDecimal("2431")) == 0);
    this.shouldCrearFacturaBDePedido();
    facturasRecuperadas = restTemplate.exchange(apiPrefix + "/facturas/venta/busqueda/criteria?"
                    + "idEmpresa=1"
                    + "&nroPedido=" + pedidoRecuperado.getNroPedido(), HttpMethod.GET, null,
            new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {
            }).getBody().getContent();
    assertEquals(2, facturasRecuperadas.size(), 0);
    pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.CERRADO, pedidoRecuperado.getEstado());
    restTemplate.delete(apiPrefix + "/facturas?idFactura=" + facturasRecuperadas.get(0).getId_Factura());
    pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    restTemplate.delete(apiPrefix + "/facturas?idFactura=" + facturasRecuperadas.get(1).getId_Factura());
    pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
  }

//      @Test
//      public void testRecibo() {
//          this.token = restTemplate.postForEntity(apiPrefix + "/login", new Credencial("test",
//   "test"), String.class).getBody();
//          Localidad localidad = new LocalidadBuilder().build();
//          localidad.getProvincia().setPais(restTemplate.postForObject(apiPrefix + "/paises",
//   localidad.getProvincia().getPais(), Pais.class));
//          localidad.setProvincia(restTemplate.postForObject(apiPrefix + "/provincias",
//   localidad.getProvincia(), Provincia.class));
//          localidad = restTemplate.postForObject(apiPrefix + "/localidades", localidad,
//   Localidad.class);
//          Empresa empresa = new EmpresaBuilder()
//                  .withLocalidad(localidad)
//                  .build();
//          empresa = restTemplate.postForObject(apiPrefix + "/empresas", empresa, Empresa.class);
//          FormaDePago formaDePago = new FormaDePagoBuilder()
//                  .withAfectaCaja(false)
//                  .withEmpresa(empresa)
//                  .withPredeterminado(true)
//                  .withNombre("Efectivo")
//                  .build();
//          restTemplate.postForObject(apiPrefix + "/formas-de-pago", formaDePago,
//   FormaDePago.class);
//          UsuarioDTO credencial = UsuarioDTO.builder()
//                  .username("marce")
//                  .password("marce123")
//                  .nombre("Marcelo")
//                  .apellido("Rockefeller")
//                  .email("marce.r@gmail.com")
//                  .roles(new ArrayList<>(Arrays.asList(Rol.COMPRADOR)))
//                  .build();
//          credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial,
//   UsuarioDTO.class);
//          ClienteDTO cliente = ClienteDTO.builder()
//                  .tipoDeCliente(TipoDeCliente.EMPRESA)
//                  .bonificacion(BigDecimal.TEN)
//                  .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
//                  .razonSocial("Peter Parker")
//                  .telefono("3791234532")
//                  .build();
//          cliente = restTemplate.postForObject(apiPrefix + "/clientes?idEmpresa=" +
//   empresa.getId_Empresa()
//                          + "&idLocalidad=" + localidad.getId_Localidad()
//                          + "&idUsuarioCredencial=" + credencial.getId_Usuario(),
//                  cliente, ClienteDTO.class);
//          Transportista transportista = new TransportistaBuilder()
//                  .withEmpresa(empresa)
//                  .withLocalidad(empresa.getLocalidad())
//                  .build();
//          transportista = restTemplate.postForObject(apiPrefix + "/transportistas", transportista,
//   Transportista.class);
//          Medida medida = new MedidaBuilder().withEmpresa(empresa).build();
//          medida = restTemplate.postForObject(apiPrefix + "/medidas", medida, Medida.class);
//          Proveedor proveedor = new ProveedorBuilder().withEmpresa(empresa)
//                  .withLocalidad(empresa.getLocalidad())
//                  .build();
//          proveedor = restTemplate.postForObject(apiPrefix + "/proveedores", proveedor,
//   Proveedor.class);
//          Rubro rubro = new RubroBuilder().withEmpresa(empresa).build();
//          rubro = restTemplate.postForObject(apiPrefix + "/rubros", rubro, Rubro.class);
//          ProductoDTO productoUno = new ProductoBuilder()
//                  .withCodigo("1")
//                  .withDescripcion("uno")
//                  .withCantidad(BigDecimal.TEN)
//                  .withVentaMinima(BigDecimal.ONE)
//                  .withPrecioCosto(CIEN)
//                  .withGanancia_porcentaje(new BigDecimal("900"))
//                  .withGanancia_neto(new BigDecimal("900"))
//                  .withPrecioVentaPublico(new BigDecimal("1000"))
//                  .withIva_porcentaje(new BigDecimal("21.0"))
//                  .withIva_neto(new BigDecimal("210"))
//                  .withPrecioLista(new BigDecimal("1210"))
//                  .build();
//          ProductoDTO productoDos = new ProductoBuilder()
//                  .withCodigo("2")
//                  .withDescripcion("dos")
//                  .withCantidad(new BigDecimal("6"))
//                  .withVentaMinima(BigDecimal.ONE)
//                  .withPrecioCosto(CIEN)
//                  .withGanancia_porcentaje(new BigDecimal("900"))
//                  .withGanancia_neto(new BigDecimal("900"))
//                  .withPrecioVentaPublico(new BigDecimal("1000"))
//                  .withIva_porcentaje(new BigDecimal("10.5"))
//                  .withIva_neto(new BigDecimal("105"))
//                  .withPrecioLista(new BigDecimal("1105"))
//                  .build();
//          productoUno = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" +
//   medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
//                  + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" +
//   empresa.getId_Empresa(),
//                  productoUno, ProductoDTO.class);
//          productoDos = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" +
//   medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
//                  + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" +
//   empresa.getId_Empresa(),
//                  productoDos, ProductoDTO.class);
//          String uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" +
//   productoUno.getId_Producto() + "," + productoDos.getId_Producto() + "&cantidad=10,6";
//          Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new
//   ParameterizedTypeReference<Map<Long, Producto>>() {}).getBody().isEmpty());
//          RenglonFactura renglonUno = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
//                  + "idProducto=" + productoUno.getId_Producto()
//                  + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_B
//                  + "&movimiento=" + Movimiento.VENTA
//                  + "&cantidad=5"
//                  + "&descuentoPorcentaje=20",
//                  RenglonFactura.class);
//          RenglonFactura renglonDos = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
//                  + "idProducto=" + productoDos.getId_Producto()
//                  + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_B
//                  + "&movimiento=" + Movimiento.VENTA
//                  + "&cantidad=2"
//                  + "&descuentoPorcentaje=0",
//                  RenglonFactura.class);
//          List<RenglonFactura> renglones = new ArrayList<>();
//          renglones.add(renglonUno);
//          renglones.add(renglonDos);
//          int size = renglones.size();
//          BigDecimal[] cantidades = new BigDecimal[size];
//          BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
//          BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
//          int indice = 0;
//          BigDecimal subTotal = BigDecimal.ZERO;
//          for (RenglonFactura renglon : renglones) {
//              subTotal = subTotal.add(renglon.getImporte());
//              cantidades[indice] = renglon.getCantidad();
//              ivaPorcentajeRenglones[indice] = renglon.getIvaPorcentaje();
//              ivaNetoRenglones[indice] = renglon.getIvaNeto();
//              indice++;
//          }
//          BigDecimal descuentoPorcentaje = new BigDecimal("25");
//          BigDecimal recargoPorcentaje = BigDecimal.TEN;
//          BigDecimal descuento_neto = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15,
//   RoundingMode.HALF_UP);
//          BigDecimal recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15,
//   RoundingMode.HALF_UP);
//          indice = cantidades.length;
//          BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
//          BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
//          for (int i = 0; i < indice; i++) {
//              if (ivaPorcentajeRenglones[i].compareTo(IVA_105) == 0) {
//                  iva_105_netoFactura =
//   iva_105_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
//                          .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15,
//   RoundingMode.HALF_UP)))));
//              } else if (ivaPorcentajeRenglones[i].compareTo(IVA_21) == 0) {
//                  iva_21_netoFactura =
//   iva_21_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
//                          .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15,
//   RoundingMode.HALF_UP)))));
//              }
//          }
//          BigDecimal subTotalBruto =
//   subTotal.add(recargo_neto).subtract(descuento_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
//          BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
//          FacturaVentaDTO facturaVentaB = new FacturaVentaDTO();
//          facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
//          facturaVentaB.setRenglones(renglones);
//          facturaVentaB.setSubTotal(subTotal);
//          facturaVentaB.setRecargoPorcentaje(recargoPorcentaje);
//          facturaVentaB.setRecargoNeto(recargo_neto);
//          facturaVentaB.setDescuentoPorcentaje(descuentoPorcentaje);
//          facturaVentaB.setDescuentoNeto(descuento_neto);
//          facturaVentaB.setSubTotalBruto(subTotalBruto);
//          facturaVentaB.setIva105Neto(iva_105_netoFactura);
//          facturaVentaB.setIva21Neto(iva_21_netoFactura);
//          facturaVentaB.setTotal(total);
//          restTemplate.postForObject(apiPrefix + "/facturas/venta?"
//                  + "idCliente=" + cliente.getId_Cliente()
//                  + "&idEmpresa=" + empresa.getId_Empresa()
//                  + "&idUsuario=" + credencial.getId_Usuario()
//                  + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaB,
//   FacturaVenta[].class);
//          assertEquals(-5992.5, restTemplate.getForObject(apiPrefix +
//   "/cuentas-corriente/clientes/1/saldo", Double.class), 0);
//          ProductoDTO productoTres = new ProductoBuilder()
//                  .withCodigo("3")
//                  .withDescripcion("tres")
//                  .withCantidad(new BigDecimal("30"))
//                  .withVentaMinima(BigDecimal.ONE)
//                  .withPrecioCosto(new BigDecimal("200"))
//                  .withGanancia_porcentaje(new BigDecimal("900"))
//                  .withGanancia_neto(new BigDecimal("1800"))
//                  .withPrecioVentaPublico(new BigDecimal("2000"))
//                  .withIva_porcentaje(new BigDecimal("21.0"))
//                  .withIva_neto(new BigDecimal("420"))
//                  .withPrecioLista(new BigDecimal("2420"))
//                  .build();
//          ProductoDTO productoCuatro = new ProductoBuilder()
//                  .withCodigo("4")
//                  .withDescripcion("cuatro")
//                  .withCantidad(new BigDecimal("12"))
//                  .withVentaMinima(BigDecimal.ONE)
//                  .withPrecioCosto(new BigDecimal("200"))
//                  .withGanancia_porcentaje(new BigDecimal("900"))
//                  .withGanancia_neto(new BigDecimal("1800"))
//                  .withPrecioVentaPublico(new BigDecimal("2000"))
//                  .withIva_porcentaje(new BigDecimal("10.5"))
//                  .withIva_neto(new BigDecimal("210"))
//                  .withPrecioLista(new BigDecimal("2210"))
//                  .build();
//          productoTres = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" +
//   medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
//                  + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" +
//   empresa.getId_Empresa(),
//                  productoTres, ProductoDTO.class);
//          productoCuatro = restTemplate.postForObject(apiPrefix + "/productos?idMedida=" +
//   medida.getId_Medida() + "&idRubro=" + rubro.getId_Rubro()
//                  + "&idProveedor=" + proveedor.getId_Proveedor() + "&idEmpresa=" +
//   empresa.getId_Empresa(),
//                  productoCuatro, ProductoDTO.class);
//          uri = apiPrefix + "/productos/disponibilidad-stock?idProducto=" +
//   productoTres.getId_Producto() + "," + productoCuatro.getId_Producto() + "&cantidad=5,2";
//          Assert.assertTrue(restTemplate.exchange(uri, HttpMethod.GET, null, new
//   ParameterizedTypeReference<Map<Long, Producto>>() {}).getBody().isEmpty());
//          RenglonFactura renglonTres = restTemplate.getForObject(apiPrefix + "/facturas/renglon?"
//                  + "idProducto=" + productoTres.getId_Producto()
//                  + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_X
//                  + "&movimiento=" + Movimiento.VENTA
//                  + "&cantidad=5"
//                  + "&descuentoPorcentaje=20",
//                  RenglonFactura.class);
//          RenglonFactura renglonCuatro = restTemplate.getForObject(apiPrefix +
//   "/facturas/renglon?"
//                  + "idProducto=" + productoCuatro.getId_Producto()
//                  + "&tipoDeComprobante=" + TipoDeComprobante.FACTURA_X
//                  + "&movimiento=" + Movimiento.VENTA
//                  + "&cantidad=2"
//                  + "&descuentoPorcentaje=0",
//                  RenglonFactura.class);
//          renglones = new ArrayList<>();
//          renglones.add(renglonTres);
//          renglones.add(renglonCuatro);
//          size = renglones.size();
//          cantidades = new BigDecimal[size];
//          ivaPorcentajeRenglones = new BigDecimal[size];
//          ivaNetoRenglones = new BigDecimal[size];
//          indice = 0;
//          subTotal = BigDecimal.ZERO;
//          for (RenglonFactura renglon : renglones) {
//              subTotal = subTotal.add(renglon.getImporte());
//              cantidades[indice] = renglon.getCantidad();
//              ivaPorcentajeRenglones[indice] = renglon.getIvaPorcentaje();
//              ivaNetoRenglones[indice] = renglon.getIvaNeto();
//              indice++;
//          }
//          descuentoPorcentaje = new BigDecimal("25");
//          recargoPorcentaje = BigDecimal.TEN;
//          descuento_neto = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15,
//   RoundingMode.HALF_UP);
//          recargo_neto = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15,
//   RoundingMode.HALF_UP);
//          indice = cantidades.length;
//          iva_105_netoFactura = BigDecimal.ZERO;
//          iva_21_netoFactura = BigDecimal.ZERO;
//          for (int i = 0; i < indice; i++) {
//              if (ivaPorcentajeRenglones[i].compareTo(IVA_105) == 0) {
//                  iva_105_netoFactura =
//   iva_105_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
//                          .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15,
//   RoundingMode.HALF_UP)))));
//              } else if (ivaPorcentajeRenglones[i].compareTo(IVA_21) == 0) {
//                  iva_21_netoFactura =
//   iva_21_netoFactura.add(cantidades[i].multiply(ivaNetoRenglones[i].subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
//                          .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15,
//   RoundingMode.HALF_UP)))));
//              }
//          }
//          subTotalBruto =
//   subTotal.add(recargo_neto).subtract(descuento_neto).subtract(iva_105_netoFactura.add(iva_21_netoFactura));
//          total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
//          FacturaVentaDTO facturaVentaX = new FacturaVentaDTO();
//          facturaVentaX.setTipoComprobante(TipoDeComprobante.FACTURA_X);
//          facturaVentaX.setRenglones(renglones);
//          facturaVentaX.setSubTotal(subTotal);
//          facturaVentaX.setRecargoPorcentaje(recargoPorcentaje);
//          facturaVentaX.setRecargoNeto(recargo_neto);
//          facturaVentaX.setDescuentoPorcentaje(descuentoPorcentaje);
//          facturaVentaX.setDescuentoNeto(descuento_neto);
//          facturaVentaX.setSubTotalBruto(subTotalBruto);
//          facturaVentaX.setIva105Neto(iva_105_netoFactura);
//          facturaVentaX.setIva21Neto(iva_21_netoFactura);
//          facturaVentaX.setTotal(total);
//          restTemplate.postForObject(apiPrefix + "/facturas/venta?"
//                  + "idCliente=" + cliente.getId_Cliente()
//                  + "&idEmpresa=" + empresa.getId_Empresa()
//                  + "&idUsuario=" + credencial.getId_Usuario()
//                  + "&idTransportista=" + transportista.getId_Transportista(), facturaVentaX,
//   FacturaVenta[].class);
//          assertEquals(-16192.5, restTemplate.getForObject(apiPrefix +
//   "/cuentas-corriente/clientes/1/saldo", Double.class), 0);
//          facturaVentaX = restTemplate.getForObject(apiPrefix + "/facturas/2",
//   FacturaVentaDTO.class);
//          assertTrue("El total no es el esperado", facturaVentaX.getTotal().compareTo(new
//   BigDecimal("10200")) == 0);
//          ReciboDTO r = new ReciboDTO();
//          restTemplate.postForObject(apiPrefix + "/recibos/clientes?"
//                  + "idUsuario=1&idEmpresa=1&idCliente=1&idFormaDePago=1", r, ReciboDTO.class);
//          assertEquals(-1192.5, restTemplate.getForObject(apiPrefix +
//   "/cuentas-corriente/clientes/1/saldo", Double.class), 0);
//          restTemplate.getForObject(apiPrefix + "/recibos/1/reporte", byte[].class);
//          facturaVentaX = restTemplate.getForObject(apiPrefix + "/facturas/2",
//   FacturaVentaDTO.class);
//          assertEquals(TipoDeComprobante.FACTURA_X, facturaVentaX.getTipoComprobante());
//          facturaVentaB = restTemplate.getForObject(apiPrefix + "/facturas/1",
//   FacturaVentaDTO.class);
//          assertEquals(TipoDeComprobante.FACTURA_B, facturaVentaB.getTipoComprobante());
//          r = new ReciboDTO();
//          r.setMonto(2192.5);
//          restTemplate.postForObject(apiPrefix + "/recibos/clientes?"
//                  + "idUsuario=1&idEmpresa=1&idCliente=1&idFormaDePago=1", r, ReciboDTO.class);
//          assertEquals(1000, restTemplate.getForObject(apiPrefix +
//   "/cuentas-corriente/clientes/1/saldo", Double.class), 0);
//          facturaVentaB = restTemplate.getForObject(apiPrefix + "/facturas/1",
//   FacturaVentaDTO.class);
//          assertEquals(TipoDeComprobante.FACTURA_B, facturaVentaB.getTipoComprobante());
//          NotaDebitoDTO notaDebito = new NotaDebitoDTO();
//          notaDebito.setCAE(0L);
//          notaDebito.setFecha(new Date());
//          List<RenglonNotaDebito> renglonesCalculados =
//   Arrays.asList(restTemplate.getForObject(apiPrefix +
//   "/notas/renglon/debito/recibo/2?monto=1000&ivaPorcentaje=21", RenglonNotaDebito[].class));
//          notaDebito.setRenglonesNotaDebito(renglonesCalculados);
//          notaDebito.setIva105Neto(BigDecimal.ZERO);
//          notaDebito.setIva21Neto(new BigDecimal("210"));
//          notaDebito.setMontoNoGravado(new BigDecimal("2192.5"));
//          notaDebito.setMotivo("Test alta nota debito - Cheque rechazado");
//          notaDebito.setSubTotalBruto(new BigDecimal("1000"));
//          notaDebito.setTotal(new BigDecimal("3402.5"));
//          notaDebito.setUsuario(credencial);
//          restTemplate.postForObject(apiPrefix +
//   "/notas/debito/empresa/1/usuario/1/recibo/2?idCliente=1&movimiento=VENTA", notaDebito,
//   NotaDebito.class);
//          assertEquals(-2402.5, restTemplate.getForObject(apiPrefix +
//   "/cuentas-corriente/clientes/1/saldo", Double.class), 0);
//          restTemplate.delete(apiPrefix + "/facturas?idFactura=1");
//          assertEquals(3590, restTemplate.getForObject(apiPrefix +
//   "/cuentas-corriente/clientes/1/saldo", Double.class), 0);
//          restTemplate.delete(apiPrefix + "/notas?idsNota=1");
//          assertEquals(6992.5, restTemplate.getForObject(apiPrefix +
//   "/cuentas-corriente/clientes/1/saldo", Double.class), 0);
//          List<RenglonCuentaCorriente> renglonesCuentaCorriente = restTemplate
//                  .exchange(apiPrefix + "/cuentas-corriente/1/renglones"
//                          + "?pagina=" + 0 + "&tamanio=" + 50, HttpMethod.GET, null,
//                          new
//   ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {
//                  }).getBody().getContent();
//          assertTrue("El saldo parcial del renglon no es el esperado",
//   renglonesCuentaCorriente.get(0).getSaldo() == 6992.5);
//          assertTrue("El saldo parcial del renglon no es el esperado",
//   renglonesCuentaCorriente.get(1).getSaldo() == 4800);
//          assertTrue("El saldo parcial del renglon no es el esperado",
//   renglonesCuentaCorriente.get(2).getSaldo() == -10200);
//      }

}
