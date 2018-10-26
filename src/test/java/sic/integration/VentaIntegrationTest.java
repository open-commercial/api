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
    this.debeHaberStock(10, 6);
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

  private void shouldCrearFacturaADePedido() {
    RenglonFactura[] renglonesParaFacturar =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglones/pedidos/1"
                + "?tipoDeComprobante="
                + TipoDeComprobante.FACTURA_A,
            RenglonFactura[].class);
    BigDecimal subTotal = renglonesParaFacturar[0].getImporte();
    assertEquals(
        "La importe no es el esperado",
        new BigDecimal("4250.000000000000000000000000000000"),
        renglonesParaFacturar[0].getImporte());
    BigDecimal recargoPorcentaje = BigDecimal.TEN;
    BigDecimal recargo_neto =
        subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    assertEquals(
        "El recargo neto no es el esperado" + recargo_neto.doubleValue(),
        new BigDecimal("425.000000000000000"),
        recargo_neto);
    BigDecimal iva_105_netoFactura = BigDecimal.ZERO;
    BigDecimal iva_21_netoFactura = BigDecimal.ZERO;
    if (renglonesParaFacturar[0].getIvaPorcentaje().compareTo(IVA_105) == 0) {
      iva_105_netoFactura =
          iva_105_netoFactura.add(
              renglonesParaFacturar[0]
                  .getCantidad()
                  .multiply(renglonesParaFacturar[0].getIvaNeto()));
    } else if (renglonesParaFacturar[0].getIvaPorcentaje().compareTo(IVA_21) == 0) {
      iva_21_netoFactura =
          iva_21_netoFactura.add(
              renglonesParaFacturar[0]
                  .getCantidad()
                  .multiply(renglonesParaFacturar[0].getIvaNeto()));
    }
    assertEquals("El iva 10.5 neto no es el esperado", BigDecimal.ZERO, iva_105_netoFactura);
    assertEquals("El iva 21 neto no es el esperado", new BigDecimal("892.500000000000000"), iva_21_netoFactura.setScale(15, RoundingMode.HALF_UP));
    BigDecimal subTotalBruto = subTotal.add(recargo_neto);
    assertEquals("El sub total bruto no es el esperado", new BigDecimal("4675.000000000000000000000000000000"), subTotalBruto);
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    assertEquals("El total no es el esperado", new BigDecimal("5567.500000000000000"), total.setScale(15, RoundingMode.HALF_UP));
    FacturaVentaDTO facturaVentaA = FacturaVentaDTO.builder().build();
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
    restTemplate.postForObject(
        apiPrefix
            + "/facturas/venta?idPedido=1"
            + "&idsFormaDePago=1"
            + "&montos="
            + total
            + "&idCliente=1&idEmpresa=1&idUsuario=2&idTransportista=1",
        facturaVentaA,
        FacturaVenta[].class);
  }

  private void shouldCrearFacturaBDePedido() {
    RenglonFactura[] renglonesParaFacturar =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglones/pedidos/1"
                + "?tipoDeComprobante="
                + TipoDeComprobante.FACTURA_B,
            RenglonFactura[].class);
    FacturaVentaDTO facturaVentaB = FacturaVentaDTO.builder().build();
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
    restTemplate.postForObject(
        apiPrefix
            + "/facturas/venta?idPedido=1"
            + "&idCliente=1&idEmpresa=1&idUsuario=2&idTransportista=1",
        facturaVentaB,
        FacturaVenta[].class);
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

  private void shouldCrearNotaCreditoCliente() {
    List<FacturaVenta> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix
                    + "/facturas/venta/busqueda/criteria?idEmpresa=1"
                    + "&tipoFactura="
                    + TipoDeComprobante.FACTURA_B
                    + "&nroSerie=0&nroFactura=1",
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
                    + "&cantidad=5&idRenglonFactura=1",
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
  }

  @Test
  public void shouldCrearFacturaVentaA() {
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

  @Test
  public void shouldTestReporteFactura() {
    this.shouldCrearFacturaVentaA();
    restTemplate.getForObject(apiPrefix + "/facturas/1/reporte", byte[].class);
  }

  @Test
  public void shouldCrearFacturaVentaB() {
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
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    FacturaVentaDTO facturaVentaB =
        FacturaVentaDTO.builder()
            .razonSocialCliente(cliente.getRazonSocial())
            .nombreUsuario("test test (test)")
            .build();
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
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
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
            facturaVentaB,
            FacturaVentaDTO[].class);
    facturaVentaB.setId_Factura(facturas[0].getId_Factura());
    facturaVentaB.setFecha(facturas[0].getFecha());
    assertEquals(facturaVentaB, facturas[0]);
  }

  @Test
  public void shouldCrearFacturaVentaC() {
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
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    FacturaVentaDTO facturaVentaC =
        FacturaVentaDTO.builder()
            .razonSocialCliente(cliente.getRazonSocial())
            .nombreUsuario("test test (test)")
            .build();
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
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
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
            facturaVentaC,
            FacturaVentaDTO[].class);
    facturaVentaC.setId_Factura(facturas[0].getId_Factura());
    facturaVentaC.setFecha(facturas[0].getFecha());
    assertEquals(facturaVentaC, facturas[0]);
  }

  @Test
  public void shouldCrearFacturaVentaX() {
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
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    FacturaVentaDTO facturaVentaX =
        FacturaVentaDTO.builder()
            .razonSocialCliente(cliente.getRazonSocial())
            .nombreUsuario("test test (test)")
            .build();
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
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
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
            facturaVentaX,
            FacturaVentaDTO[].class);
    facturaVentaX.setId_Factura(facturas[0].getId_Factura());
    facturaVentaX.setFecha(facturas[0].getFecha());
    assertEquals(facturaVentaX, facturas[0]);
  }

  @Test
  public void shouldCrearFacturaVentaY() {
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
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    FacturaVentaDTO facturaVentaY =
        FacturaVentaDTO.builder()
            .razonSocialCliente(cliente.getRazonSocial())
            .nombreUsuario("test test (test)")
            .build();
    facturaVentaY.setTipoComprobante(TipoDeComprobante.FACTURA_Y);
    facturaVentaY.setRenglones(renglones);
    facturaVentaY.setSubTotal(subTotal);
    facturaVentaY.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaY.setRecargoNeto(recargo_neto);
    facturaVentaY.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaY.setDescuentoNeto(descuento_neto);
    facturaVentaY.setSubTotalBruto(subTotalBruto);
    facturaVentaY.setIva105Neto(iva_105_netoFactura);
    facturaVentaY.setIva21Neto(iva_21_netoFactura);
    facturaVentaY.setTotal(total);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
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
            facturaVentaY,
            FacturaVentaDTO[].class);
    facturaVentaY.setId_Factura(facturas[0].getId_Factura());
    facturaVentaY.setFecha(facturas[0].getFecha());
    assertEquals(facturaVentaY, facturas[0]);
  }

  @Test
  public void shouldCrearFacturaVentaPresupuesto() {
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
                + TipoDeComprobante.PRESUPUESTO
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
                + TipoDeComprobante.PRESUPUESTO
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
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", Cliente.class);
    FacturaVentaDTO facturaVentaPresupuesto =
        FacturaVentaDTO.builder()
            .razonSocialCliente(cliente.getRazonSocial())
            .nombreUsuario("test test (test)")
            .build();
    facturaVentaPresupuesto.setTipoComprobante(TipoDeComprobante.PRESUPUESTO);
    facturaVentaPresupuesto.setRenglones(renglones);
    facturaVentaPresupuesto.setSubTotal(subTotal);
    facturaVentaPresupuesto.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaPresupuesto.setRecargoNeto(recargo_neto);
    facturaVentaPresupuesto.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaVentaPresupuesto.setDescuentoNeto(descuento_neto);
    facturaVentaPresupuesto.setSubTotalBruto(subTotalBruto);
    facturaVentaPresupuesto.setIva105Neto(iva_105_netoFactura);
    facturaVentaPresupuesto.setIva21Neto(iva_21_netoFactura);
    facturaVentaPresupuesto.setTotal(total);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
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
            facturaVentaPresupuesto,
            FacturaVentaDTO[].class);
    facturaVentaPresupuesto.setId_Factura(facturas[0].getId_Factura());
    facturaVentaPresupuesto.setFecha(facturas[0].getFecha());
    assertEquals(facturaVentaPresupuesto, facturas[0]);
  }

  @Test
  public void shouldTestSaldoCuentaCorrienteCliente() {
    this.shouldCrearFacturaVentaB();
    assertEquals(
        "El saldo de la cuenta corriente no es el esperado",
        new BigDecimal("-5992.500000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.shouldCrearReciboCliente(5992.5);
    assertEquals(
        "El saldo de la cuenta corriente no es el esperado",
        new BigDecimal("0E-15"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.shouldCrearNotaDebitoCliente();
    assertEquals(
        "El saldo de la cuenta corriente no es el esperado",
        new BigDecimal("-6113.500000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.shouldCrearReciboCliente(6113.5);
    assertEquals(
        "El saldo de la cuenta corriente no es el esperado",
        new BigDecimal("0E-15"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.shouldCrearNotaCreditoCliente();
    assertEquals(
        "El saldo de la cuenta corriente no es el esperado",
        new BigDecimal("4114.000000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
  }

  @Test
  public void shouldTestSaldoParcialCuentaCorrienteCliente() {
    this.shouldCrearFacturaVentaB();
    this.shouldCrearReciboCliente(5992.5);
    this.shouldCrearNotaDebitoCliente();
    this.shouldCrearReciboCliente(6113.5);
    this.shouldCrearNotaCreditoCliente();
    List<RenglonCuentaCorriente> renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/1/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertEquals(
        "El saldo parcial del renglon no es el esperado",
        new Double(4114),
        renglonesCuentaCorriente.get(0).getSaldo());
    assertEquals(
        "El saldo parcial del renglon no es el esperado",
        new Double(0),
        renglonesCuentaCorriente.get(1).getSaldo());
    assertEquals(
        "El saldo parcial del renglon no es el esperado",
        new Double(-6113.5),
        renglonesCuentaCorriente.get(2).getSaldo());
    assertEquals(
        "El saldo parcial del renglon no es el esperado",
        new Double(0),
        renglonesCuentaCorriente.get(3).getSaldo());
    assertEquals(
        "El saldo parcial del renglon no es el esperado",
        new Double(-5992.5),
        renglonesCuentaCorriente.get(4).getSaldo());
  }

  @Test
  public void shouldCrearPedido() {
    this.shouldCrearPorductos();
    List<NuevoRenglonPedidoDTO> renglonesPedidoDTO = new ArrayList();
    renglonesPedidoDTO.add(
        NuevoRenglonPedidoDTO.builder()
            .idProductoItem(1l)
            .cantidad(new BigDecimal("5.000000000000000"))
            .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
            .build());
    renglonesPedidoDTO.add(
        NuevoRenglonPedidoDTO.builder()
            .idProductoItem(2l)
            .cantidad(new BigDecimal("2.000000000000000"))
            .descuentoPorcentaje(BigDecimal.ZERO)
            .build());
    List<RenglonPedidoDTO> renglonesPedido =
        Arrays.asList(
            restTemplate.postForObject(
                apiPrefix + "/pedidos/renglones", renglonesPedidoDTO, RenglonPedidoDTO[].class));
    BigDecimal subTotal = BigDecimal.ZERO;
    for (RenglonPedidoDTO renglon : renglonesPedido) {
      subTotal = subTotal.add(renglon.getSubTotal()).setScale(5, RoundingMode.HALF_UP);
    }
    BigDecimal recargoNeto =
        subTotal.multiply(new BigDecimal("5")).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal descuentoNeto =
        subTotal.multiply(new BigDecimal("15")).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal total = subTotal.add(recargoNeto).subtract(descuentoNeto);
    NuevoPedidoDTO nuevoPedidoDTO =
        NuevoPedidoDTO.builder()
            .descuentoNeto(descuentoNeto)
            .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
            .recargoNeto(recargoNeto)
            .recargoPorcentaje(new BigDecimal("5"))
            .fechaVencimiento(new Date())
            .observaciones("Nuevo Pedido Test")
            .renglones(renglonesPedido)
            .subTotal(subTotal)
            .total(total)
            .build();
    PedidoDTO pedidoRecuperado =
        restTemplate.postForObject(
            apiPrefix + "/pedidos?" + "idEmpresa=1" + "&idCliente=1" + "&idUsuario=2",
            nuevoPedidoDTO,
            PedidoDTO.class);
    assertEquals(
        "El total estimado no es el esperado",
        nuevoPedidoDTO.getTotal(),
        pedidoRecuperado.getTotalEstimado());
    assertEquals(pedidoRecuperado.getObservaciones(), nuevoPedidoDTO.getObservaciones());
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
  }

  @Test
  public void shouldFacturarPedido() {
    this.shouldCrearPedido();
    this.shouldCrearFacturaADePedido();
    PedidoDTO pedidoRecuperado =
        restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    this.shouldCrearFacturaBDePedido();
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.CERRADO, pedidoRecuperado.getEstado());
  }

  @Test
  public void testModificarPedido() {
    this.shouldCrearPorductos();
    List<NuevoRenglonPedidoDTO> renglonesPedidoDTO = new ArrayList<>();
    renglonesPedidoDTO.add(
        NuevoRenglonPedidoDTO.builder()
            .idProductoItem(1l)
            .cantidad(new BigDecimal("5"))
            .descuentoPorcentaje(new BigDecimal("15"))
            .build());
    renglonesPedidoDTO.add(
        NuevoRenglonPedidoDTO.builder()
            .idProductoItem(2l)
            .cantidad(new BigDecimal("2"))
            .descuentoPorcentaje(BigDecimal.ZERO)
            .build());
    List<RenglonPedidoDTO> renglonesPedido =
        Arrays.asList(
            restTemplate.postForObject(
                apiPrefix + "/pedidos/renglones", renglonesPedidoDTO, RenglonPedidoDTO[].class));
    BigDecimal subTotal = BigDecimal.ZERO;
    for (RenglonPedidoDTO renglon : renglonesPedido) {
      subTotal = subTotal.add(renglon.getSubTotal());
    }
    BigDecimal recargoNeto =
        subTotal.multiply(new BigDecimal("5")).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal descuentoNeto =
        subTotal.multiply(new BigDecimal("15")).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal total = subTotal.add(recargoNeto).subtract(descuentoNeto);
    NuevoPedidoDTO nuevoPedidoDTO =
        NuevoPedidoDTO.builder()
            .descuentoNeto(descuentoNeto)
            .descuentoPorcentaje(new BigDecimal("15"))
            .recargoNeto(recargoNeto)
            .recargoPorcentaje(new BigDecimal("5"))
            .fechaVencimiento(new Date())
            .observaciones("Nuevo Pedido Test")
            .renglones(renglonesPedido)
            .subTotal(subTotal)
            .total(total)
            .build();
    PedidoDTO pedidoRecuperado =
        restTemplate.postForObject(
            apiPrefix + "/pedidos?idEmpresa=1&idCliente=1&idUsuario=1",
            nuevoPedidoDTO,
            PedidoDTO.class);
    assertEquals(
        "El total estimado no es el esperado",
        nuevoPedidoDTO.getTotal(),
        pedidoRecuperado.getTotalEstimado());
    assertEquals(nuevoPedidoDTO.getObservaciones(), pedidoRecuperado.getObservaciones());
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
    this.shouldCrearPorductos();
    renglonesPedidoDTO = new ArrayList<>();
    renglonesPedidoDTO.add(
        NuevoRenglonPedidoDTO.builder()
            .idProductoItem(3)
            .cantidad(new BigDecimal("7"))
            .descuentoPorcentaje(new BigDecimal("18"))
            .build());
    renglonesPedido =
        Arrays.asList(
            restTemplate.postForObject(
                apiPrefix + "/pedidos/renglones", renglonesPedidoDTO, RenglonPedidoDTO[].class));
    subTotal = BigDecimal.ZERO;
    for (RenglonPedidoDTO renglon : renglonesPedido) {
      subTotal = subTotal.add(renglon.getSubTotal());
    }
    recargoNeto = subTotal.multiply(new BigDecimal("5")).divide(CIEN, 15, RoundingMode.HALF_UP);
    descuentoNeto = subTotal.multiply(new BigDecimal("15")).divide(CIEN, 15, RoundingMode.HALF_UP);
    total = subTotal.add(recargoNeto).subtract(descuentoNeto);
    pedidoRecuperado.setSubTotal(subTotal);
    pedidoRecuperado.setRecargoNeto(recargoNeto);
    pedidoRecuperado.setDescuentoNeto(descuentoNeto);
    pedidoRecuperado.setTotalActual(total);
    pedidoRecuperado.setTotalEstimado(total);
    pedidoRecuperado.setRenglones(renglonesPedido);
    pedidoRecuperado.setObservaciones("Cambiando las observaciones del pedido");
    restTemplate.put(apiPrefix + "/pedidos?idEmpresa=1&idCliente=1&idUsuario=1", pedidoRecuperado);
    pedidoRecuperado = restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    assertEquals("El total estimado no es el esperado", total, pedidoRecuperado.getTotalEstimado());
    assertEquals("Cambiando las observaciones del pedido", pedidoRecuperado.getObservaciones());
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
  }

  @Test
  public void shouldTestTransicionDeEstadosDeUnPedido() {
    this.shouldCrearPedido();
    this.shouldCrearFacturaADePedido();
    PedidoDTO pedidoRecuperado =
        restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    this.shouldCrearFacturaBDePedido();
    List<FacturaVenta> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix
                    + "/facturas/venta/busqueda/criteria?"
                    + "idEmpresa=1"
                    + "&nroPedido="
                    + pedidoRecuperado.getNroPedido(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVenta>>() {})
            .getBody()
            .getContent();
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.CERRADO, pedidoRecuperado.getEstado());
    restTemplate.delete(
        apiPrefix + "/facturas?idFactura=" + facturasRecuperadas.get(0).getId_Factura());
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    restTemplate.delete(
        apiPrefix + "/facturas?idFactura=" + facturasRecuperadas.get(1).getId_Factura());
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getId_Pedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
  }
}
