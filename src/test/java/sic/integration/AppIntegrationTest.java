package sic.integration;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import sic.modelo.*;
import sic.modelo.calculos.NuevosResultadosPedido;
import sic.modelo.calculos.Resultados;
import sic.modelo.criteria.*;
import sic.modelo.dto.*;
import sic.service.ICajaService;
import sic.service.IClockService;
import sic.service.IPedidoService;
import sic.service.IProductoService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("/data-test.sql")
class AppIntegrationTest {

  @Autowired private IProductoService productoService;
  @Autowired private IPedidoService pedidoService;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private IClockService clockService;
  @Autowired private ICajaService cajaService;
  @Autowired private MessageSource messageSource;

  private String token;
  private final String apiPrefix = "/api/v1";

  private static final BigDecimal IVA_21 = new BigDecimal("21");
  private static final BigDecimal IVA_105 = new BigDecimal("10.5");
  private static final BigDecimal CIEN = new BigDecimal("100");

  @Value("${RECAPTCHA_TEST_KEY}")
  private String recaptchaTestKey;

  private void crearDosProductos(String descripcionUno, String descripcionDos) {
    NuevoProductoDTO productoUno =
      NuevoProductoDTO.builder()
        .codigo(RandomStringUtils.random(10, false, true))
        .descripcion(descripcionUno)
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.TEN);}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("21.0"))
        .ivaNeto(new BigDecimal("210"))
        .precioLista(new BigDecimal("1210"))
        .nota("ProductoTest1")
        .publico(true)
        .build();
    NuevoProductoDTO productoDos =
      NuevoProductoDTO.builder()
        .codigo(RandomStringUtils.random(10, false, true))
        .descripcion(descripcionDos)
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, new BigDecimal("6"));}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("10.5"))
        .ivaNeto(new BigDecimal("105"))
        .precioLista(new BigDecimal("1105"))
        .nota("ProductoTest2")
        .build();
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    RubroDTO rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", RubroDTO.class);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
    restTemplate.postForObject(
      apiPrefix
        + "/productos?idMedida="
        + medida.getIdMedida()
        + "&idRubro="
        + rubro.getIdRubro()
        + "&idProveedor="
        + proveedor.getIdProveedor()
        + "&idSucursal="
        + sucursal.getIdSucursal(),
      productoUno,
      ProductoDTO.class);
    restTemplate.postForObject(
      apiPrefix
        + "/productos?idMedida="
        + medida.getIdMedida()
        + "&idRubro="
        + rubro.getIdRubro()
        + "&idProveedor="
        + proveedor.getIdProveedor()
        + "&idSucursal="
        + sucursal.getIdSucursal(),
      productoDos,
      ProductoDTO.class);
  }

  private void crearFacturaTipoADePedido() {
    RenglonFactura[] renglonesParaFacturar =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglones/pedidos/1"
          + "?tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A,
        RenglonFactura[].class);
    BigDecimal subTotal = renglonesParaFacturar[0].getImporte();
    assertEquals(
      new BigDecimal("4000.000000000000000000000000000000"),
      renglonesParaFacturar[0].getImporte());
    BigDecimal recargoPorcentaje = BigDecimal.TEN;
    BigDecimal recargo_neto =
      subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    assertEquals(new BigDecimal("400.000000000000000"), recargo_neto);
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
    iva_21_netoFactura = iva_21_netoFactura.add(recargo_neto.multiply(IVA_21.divide(new BigDecimal("100"))));
    assertEquals(BigDecimal.ZERO, iva_105_netoFactura);
    assertEquals(
      new BigDecimal("924.000000000000000"),
      iva_21_netoFactura.setScale(15, RoundingMode.HALF_UP));
    BigDecimal subTotalBruto = subTotal.add(recargo_neto);
    assertEquals(new BigDecimal("4400.000000000000000000000000000000"), subTotalBruto);
    BigDecimal total = subTotalBruto.add(iva_105_netoFactura).add(iva_21_netoFactura);
    assertEquals(new BigDecimal("5324.000000000000000"), total.setScale(15, RoundingMode.HALF_UP));
    FacturaVentaDTO facturaVentaA = FacturaVentaDTO.builder()
      .idCliente(1L)
      .build();
    facturaVentaA.setIdSucursal(1L);
    facturaVentaA.setIdCliente(1L);
    facturaVentaA.setIdTransportista(1L);
    facturaVentaA.setObservaciones("Factura A del Pedido");
    facturaVentaA.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    List<RenglonFactura> renglones = new ArrayList<>();
    renglones.add(renglonesParaFacturar[0]);
    facturaVentaA.setRenglones(renglones);
    facturaVentaA.setSubTotal(subTotal);
    facturaVentaA.setRecargoPorcentaje(recargoPorcentaje);
    facturaVentaA.setRecargoNeto(recargo_neto);
    facturaVentaA.setDescuentoPorcentaje(BigDecimal.ZERO);
    facturaVentaA.setDescuentoNeto(BigDecimal.ZERO);
    facturaVentaA.setSubTotalBruto(subTotalBruto);
    facturaVentaA.setIva105Neto(iva_105_netoFactura);
    facturaVentaA.setIva21Neto(iva_21_netoFactura);
    facturaVentaA.setTotal(total);
    facturaVentaA.setFecha(LocalDateTime.now());
    this.abrirCaja();
    Long[] idsFormasDePago = {1L};
    BigDecimal[] montos = {total};
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaA)
        .idPedido(1L)
        .idsFormaDePago(idsFormasDePago)
        .montos(montos)
        .build();
    restTemplate.postForObject(apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVenta[].class);
  }

  private void crearFacturaTipoBDePedido() {
    RenglonFactura[] renglonesParaFacturar =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglones/pedidos/1"
          + "?tipoDeComprobante="
          + TipoDeComprobante.FACTURA_B,
        RenglonFactura[].class);
    FacturaVentaDTO facturaVentaB = FacturaVentaDTO.builder()
      .idCliente(1L)
      .build();
    facturaVentaB.setIdSucursal(1L);
    facturaVentaB.setIdUsuario(2L);
    facturaVentaB.setIdTransportista(1L);
    facturaVentaB.setObservaciones("Factura B del Pedido");
    facturaVentaB.setTipoComprobante(TipoDeComprobante.FACTURA_B);
    List<RenglonFactura> renglonesDeFactura = new ArrayList<>();
    renglonesDeFactura.add(renglonesParaFacturar[0]);
    facturaVentaB.setRenglones(renglonesDeFactura);
    facturaVentaB.setSubTotal(new BigDecimal("1768"));
    facturaVentaB.setRecargoPorcentaje(BigDecimal.TEN);
    facturaVentaB.setRecargoNeto(new BigDecimal("176.800000000000000"));
    facturaVentaB.setDescuentoPorcentaje(BigDecimal.ZERO);
    facturaVentaB.setDescuentoNeto(BigDecimal.ZERO);
    facturaVentaB.setSubTotalBruto(new BigDecimal("1760.00000000000000"));
    facturaVentaB.setIva105Neto(new BigDecimal("184.800000000000000"));
    facturaVentaB.setIva21Neto(BigDecimal.ZERO);
    facturaVentaB.setTotal(new BigDecimal("1944.800000000000000"));
    facturaVentaB.setFecha(LocalDateTime.now());
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaB)
        .idPedido(1L)
        .build();
    restTemplate.postForObject(
        apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVenta[].class);
  }

  private void crearReciboParaCliente(double monto, long idSucursal, long idCliente) {
    ReciboDTO recibo =
        ReciboDTO.builder()
            .concepto("Recibo Test")
            .monto(monto)
            .idSucursal(idSucursal)
            .idCliente(idCliente)
            .idFormaDePago(1L)
            .build();
    restTemplate.postForObject(apiPrefix + "/recibos/clientes", recibo, ReciboDTO.class);
  }

  private void crearNotaDebitoParaCliente() {
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO = NuevaNotaDebitoDeReciboDTO.builder()
      .idRecibo(1L)
      .motivo("Test alta nota debito - Cheque rechazado")
      .gastoAdministrativo(new BigDecimal("121"))
      .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_A)
      .build();
    NotaDebitoDTO notaDebitoCliente =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    restTemplate.postForObject(
      apiPrefix + "/notas/debito",
      notaDebitoCliente,
      Nota.class);
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  private void crearNotaCreditoParaCliente() {
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder()
        .tipoComprobante(TipoDeComprobante.FACTURA_B)
        .idSucursal(1L)
        .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody()
            .getContent();
    Long[] idsFactura = new Long[1];
    RenglonFactura[] renglones =
      restTemplate.getForObject(apiPrefix + "/facturas/" + facturasRecuperadas.get(0).getIdFactura() + "/renglones", RenglonFactura[].class);
    idsFactura[0] = renglones[0].getIdRenglonFactura();
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(facturasRecuperadas.get(0).getIdFactura())
        .idsRenglonesFactura(idsFactura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Devolución")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    restTemplate.postForObject(
      apiPrefix
        + "/notas/credito",
      notaCreditoParaPersistir,
      Nota.class);
    restTemplate.getForObject(apiPrefix + "/notas/2/reporte", byte[].class);
  }

  private void crearReciboParaProveedor(double monto, long idSucursal, long idProveedor) {
    ReciboDTO recibo =
        ReciboDTO.builder()
            .monto(monto)
            .concepto("Recibo para proveedor")
            .idSucursal(idSucursal)
            .idProveedor(idProveedor)
            .idFormaDePago(1L)
            .fecha(LocalDateTime.now())
            .build();
    restTemplate.postForObject(apiPrefix + "/recibos/proveedores", recibo, Recibo.class);
  }

  private void crearNotaDebitoParaProveedor() {
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO = NuevaNotaDebitoDeReciboDTO.builder()
      .idRecibo(3L)
      .motivo("Test alta nota debito - Cheque rechazado")
      .gastoAdministrativo(new BigDecimal("121"))
      .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_B)
      .build();
    NotaDebitoDTO notaDebitoProveedor =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    restTemplate.postForObject(
      apiPrefix + "/notas/debito",
      notaDebitoProveedor,
      Nota.class);
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  private void crearNotaCreditoParaProveedor() {
    Long[] idsRenglonesFactura = new Long[1];
    idsRenglonesFactura[0] = 3L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
        NuevaNotaCreditoDeFacturaDTO.builder()
            .idFactura(2L)
            .idsRenglonesFactura(idsRenglonesFactura)
            .cantidades(cantidades)
            .modificaStock(true)
            .motivo("Devolución")
            .build();
    NotaCreditoDTO notaCreditoParaPersistir =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    restTemplate.postForObject(
        apiPrefix + "/notas/credito", notaCreditoParaPersistir, NotaCreditoDTO.class);
  }

  private void abrirCaja() {
    restTemplate.postForObject(apiPrefix + "/cajas/apertura/sucursales/1?saldoApertura=0", null, CajaDTO.class);
  }

  @BeforeEach
  void setup() {
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
            String mensaje = IOUtils.toString(response.getBody(), Charset.defaultCharset());
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
  }

  @Test
  void shouldRegistrarNuevaCuentaComoResponsableInscripto() {
    RegistracionClienteAndUsuarioDTO registro =
      RegistracionClienteAndUsuarioDTO.builder()
        .apellido("Stark")
        .nombre("Sansa")
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .email("sansa@got.com")
        .telefono("415789966")
        .password("caraDeMala")
        .recaptcha(recaptchaTestKey)
        .nombreFiscal("theRedWolf")
        .build();
    restTemplate.postForObject(apiPrefix + "/registracion", registro, Void.class);
    UsuarioDTO usuario = restTemplate.getForObject(apiPrefix + "/usuarios/3", UsuarioDTO.class);
    assertEquals(usuario.getNombre(), "Sansa");
    assertEquals(usuario.getApellido(), "Stark");
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/3", ClienteDTO.class);
    assertEquals(cliente.getNombreFiscal(), "theRedWolf");
  }

  @Test
  void shouldCrearSucursalResponsableInscripto() {
    SucursalDTO sucursalNueva =
        SucursalDTO.builder()
            .telefono("3795221144")
            .email("sucursal@nueva.com")
            .fechaInicioActividad(LocalDateTime.now())
            .ingresosBrutos(21112244L)
            .idFiscal(7488521665766L)
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .lema("Hoy no se fía, mañana si.")
            .nombre("La gran idea")
            .ubicacion(
                UbicacionDTO.builder().idLocalidad(1L).calle("Rio Chico").numero(6211).build())
            .build();
    SucursalDTO sucursalGuardada =
      restTemplate.postForObject(apiPrefix + "/sucursales", sucursalNueva, SucursalDTO.class);
    assertEquals(sucursalNueva, sucursalGuardada);
  }

  @Test
  void shouldCrearSucursalMonotributista() {
    SucursalDTO sucursalNueva =
      SucursalDTO.builder()
        .telefono("3795221144")
        .email("sucursal@nuevaMonotribustista.com")
        .fechaInicioActividad(LocalDateTime.now())
        .ingresosBrutos(23335577L)
        .idFiscal(7599541775766L)
        .categoriaIVA(CategoriaIVA.MONOTRIBUTO)
        .lema("Hoy no se fía, mañana tampoco.")
        .nombre("Se me prendió el foquito.")
        .ubicacion(
          UbicacionDTO.builder().idLocalidad(1L).calle("Rio Naranja").numero(345).build())
        .build();
    SucursalDTO sucursalGuardada =
      restTemplate.postForObject(apiPrefix + "/sucursales", sucursalNueva, SucursalDTO.class);
    assertEquals(sucursalNueva, sucursalGuardada);
  }

  @Test
  void shouldCrearSucursalConUbicacion() {
    SucursalDTO sucursalNueva =
        SucursalDTO.builder()
            .telefono("3795221144")
            .email("sucursal@nueva.com")
            .fechaInicioActividad(LocalDateTime.now())
            .ingresosBrutos(21112244L)
            .idFiscal(7488521665766L)
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .lema("Hoy no se fía, mañana si.")
            .nombre("La gran idea")
            .ubicacion(
                UbicacionDTO.builder()
                    .calle("Valle Sagrado de los Enanos")
                    .numero(8877)
                    .idLocalidad(1L)
                    .build())
            .build();
    SucursalDTO sucursalGuardada =
        restTemplate.postForObject(apiPrefix + "/sucursales", sucursalNueva, SucursalDTO.class);
    assertEquals(sucursalNueva, sucursalGuardada);
  }

  @Test
  void shouldEditarUbicacionDeSucursal() {
    SucursalDTO sucursalNueva =
        SucursalDTO.builder()
            .telefono("3795221144")
            .email("sucursal@nueva.com")
            .fechaInicioActividad(LocalDateTime.now())
            .ingresosBrutos(21112244L)
            .idFiscal(7488521665766L)
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .lema("Hoy no se fía, mañana si.")
            .nombre("La gran idea")
            .ubicacion(
                UbicacionDTO.builder()
                    .calle("Valle Sagrado de los Enanos")
                    .numero(8877)
                    .idLocalidad(1L)
                    .build())
            .build();
    SucursalDTO sucursalGuardada =
        restTemplate.postForObject(apiPrefix + "/sucursales", sucursalNueva, SucursalDTO.class);
    UbicacionDTO ubicacion = sucursalGuardada.getUbicacion();
    ubicacion.setCalle("Los Rios Negros");
    ubicacion.setNumero(1456);
    sucursalGuardada.setUbicacion(ubicacion);
    restTemplate.put(apiPrefix + "/sucursales", sucursalGuardada);
    sucursalGuardada =
        restTemplate.getForObject(
            apiPrefix + "/sucursales/" + sucursalGuardada.getIdSucursal(), SucursalDTO.class);
    assertEquals(ubicacion, sucursalGuardada.getUbicacion());
  }

  @Test
  void shouldCrearClienteResponsableInscripto() {
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("elenanocañete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Cañete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    assertEquals(cliente, clienteRecuperado);
  }

  @Test
  void shouldCrearClienteResponsableMonotributista() {
    UsuarioDTO credencial =
      UsuarioDTO.builder()
        .username("elaltoRicardo")
        .password("RicardoTapia")
        .nombre("Ricardo")
        .apellido("Tapia")
        .email("tapia@outlook.com.cl")
        .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
        .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
      ClienteDTO.builder()
        .bonificacion(BigDecimal.TEN)
        .nombreFiscal("Ricardo Tapia")
        .nombreFantasia("Menos mal que estan ellos.")
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .idFiscal(1244557L)
        .email("tapia@outlook.com.cl")
        .telefono("3745112324")
        .contacto("Ricardo, trabaja por encargo.")
        .idCredencial(credencial.getIdUsuario())
        .build();
    ClienteDTO clienteRecuperado =
      restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    assertEquals(cliente, clienteRecuperado);
  }

  @Test
  void shouldCrearClienteConUbicaciones() {
    UbicacionDTO ubicacionDeFacturacion = UbicacionDTO.builder()
      .calle("Sarmiento")
      .numero(789)
      .idLocalidad(1L)
      .build();
    UbicacionDTO ubicacionDeEnvio = UbicacionDTO.builder()
      .calle("Belgrano")
      .numero(456)
      .idLocalidad(1L)
      .build();
    UsuarioDTO credencial =
      UsuarioDTO.builder()
        .username("elenanocañete")
        .password("siempredebarrio")
        .nombre("Juan")
        .apellido("Cañete")
        .email("caniete@yahoo.com.br")
        .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
        .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .ubicacionFacturacion(ubicacionDeFacturacion)
            .ubicacionEnvio(ubicacionDeEnvio)
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    assertEquals(cliente, clienteRecuperado);
    assertEquals(ubicacionDeFacturacion, clienteRecuperado.getUbicacionFacturacion());
  }

  @Test
  void shouldCrearClienteConUbicacionDeFacturacion() {
    UsuarioDTO credencial =
      UsuarioDTO.builder()
        .username("elenanocañete")
        .password("siempredebarrio")
        .nombre("Juan")
        .apellido("Cañete")
        .email("caniete@yahoo.com.br")
        .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
        .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    UbicacionDTO ubicacionDeFacturacion = UbicacionDTO.builder()
      .calle("Sarmiento")
      .numero(789)
      .idLocalidad(1L)
      .build();
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .ubicacionFacturacion(ubicacionDeFacturacion)
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    assertEquals(cliente, clienteRecuperado);
    assertEquals(ubicacionDeFacturacion, clienteRecuperado.getUbicacionFacturacion());
    assertNull(clienteRecuperado.getUbicacionEnvio());
  }

  @Test
  void shouldCrearClienteConUbicacionDeEnvio() {
    UsuarioDTO credencial =
      UsuarioDTO.builder()
        .username("elenanocañete")
        .password("siempredebarrio")
        .nombre("Juan")
        .apellido("Cañete")
        .email("caniete@yahoo.com.br")
        .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
        .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    UbicacionDTO ubicacionDeEnvio = UbicacionDTO.builder()
      .calle("Belgrano")
      .numero(456)
      .idLocalidad(1L)
      .build();
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .ubicacionEnvio(ubicacionDeEnvio)
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    assertEquals(cliente, clienteRecuperado);
    assertEquals(ubicacionDeEnvio, clienteRecuperado.getUbicacionEnvio());
    assertNull(clienteRecuperado.getUbicacionFacturacion());
  }

  @Test
  void shouldNotCrearClienteConUbicacionDeFacturacionSinLocalidad() {
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("elenanocañete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Cañete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    UbicacionDTO ubicacionDeFacturacion =
        UbicacionDTO.builder().calle("Sarmiento").numero(789).build();
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .ubicacionFacturacion(ubicacionDeFacturacion)
            .build();
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_ubicacion_facturacion_sin_localidad", null, Locale.getDefault())));
  }

  @Test
  void shouldNotCrearClienteConUbicacionDeEnvioSinLocalidad() {
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("elenanocañete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Cañete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    UbicacionDTO ubicacionDeEnvio = UbicacionDTO.builder().calle("Rosas").numero(6665).build();
    ClienteDTO cliente =
      ClienteDTO.builder()
        .bonificacion(BigDecimal.TEN)
        .nombreFiscal("Juan Fernando Cañete")
        .nombreFantasia("Menos mal que estamos nosotros.")
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .idFiscal(1244557L)
        .email("caniete@yahoo.com.br")
        .telefono("3785663322")
        .contacto("Ramon el hermano de Juan")
        .idCredencial(credencial.getIdUsuario())
        .ubicacionEnvio(ubicacionDeEnvio)
        .build();
    RestClientResponseException thrown =
      assertThrows(
        RestClientResponseException.class,
        () -> restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
      thrown
        .getMessage()
        .contains(
          messageSource.getMessage(
            "mensaje_ubicacion_envio_sin_localidad", null, Locale.getDefault())));
  }

  @Test
  void shouldCrearClienteYLuegoDarDeAltaUbicacionDeFacturacionModificandoCliente() {
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("elenanocañete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Cañete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    clienteRecuperado.setUbicacionFacturacion(
        UbicacionDTO.builder().calle("14 de Julio").numero(785).idLocalidad(1L).build());
    restTemplate.put(apiPrefix + "/clientes", clienteRecuperado);
    clienteRecuperado = restTemplate.getForObject(apiPrefix + "/clientes/3", ClienteDTO.class);
    assertEquals("14 de Julio", clienteRecuperado.getUbicacionFacturacion().getCalle());
    assertEquals(785, clienteRecuperado.getUbicacionFacturacion().getNumero().intValue());
  }

  @Test
  void shouldCrearClienteYLuegoDarDeAltaUbicacionDeEnvioModificandoCliente() {
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("elenanocañete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Cañete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    clienteRecuperado.setUbicacionEnvio(
        UbicacionDTO.builder().calle("8 de Agosto").numero(964).idLocalidad(1L).build());
    restTemplate.put(apiPrefix + "/clientes", clienteRecuperado);
    clienteRecuperado = restTemplate.getForObject(apiPrefix + "/clientes/3", ClienteDTO.class);
    assertEquals("8 de Agosto", clienteRecuperado.getUbicacionEnvio().getCalle());
    assertEquals(964, clienteRecuperado.getUbicacionEnvio().getNumero().intValue());
  }

  @Test
  void shouldCrearClienteYLuegoDarDeAltaUbicacionesModificandoCliente() {
    UsuarioDTO credencial =
      UsuarioDTO.builder()
        .username("elenanocañete")
        .password("siempredebarrio")
        .nombre("Juan")
        .apellido("Cañete")
        .email("caniete@yahoo.com.br")
        .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
        .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
      ClienteDTO.builder()
        .bonificacion(BigDecimal.TEN)
        .nombreFiscal("Juan Fernando Cañete")
        .nombreFantasia("Menos mal que estamos nosotros.")
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .idFiscal(1244557L)
        .email("caniete@yahoo.com.br")
        .telefono("3785663322")
        .contacto("Ramon el hermano de Juan")
        .idCredencial(credencial.getIdUsuario())
        .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    clienteRecuperado.setUbicacionFacturacion(
        UbicacionDTO.builder().calle("Santa Cruz").numero(1247).idLocalidad(1L).build());
    clienteRecuperado.setUbicacionEnvio(
        UbicacionDTO.builder().calle("8 de Agosto").numero(964).idLocalidad(1L).build());
    restTemplate.put(apiPrefix + "/clientes", clienteRecuperado);
    clienteRecuperado = restTemplate.getForObject(apiPrefix + "/clientes/3", ClienteDTO.class);
    assertEquals("Santa Cruz", clienteRecuperado.getUbicacionFacturacion().getCalle());
    assertEquals(1247, clienteRecuperado.getUbicacionFacturacion().getNumero().intValue());
    assertEquals("8 de Agosto", clienteRecuperado.getUbicacionEnvio().getCalle());
    assertEquals(964, clienteRecuperado.getUbicacionEnvio().getNumero().intValue());
  }

  @Test
  void shouldModificarUbicacionesDeClientes() {
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("elenanocañete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Cañete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .ubicacionFacturacion(
                UbicacionDTO.builder().calle("Sarmiento").numero(789).idLocalidad(1L).build())
            .ubicacionEnvio(
                UbicacionDTO.builder().calle("Belgrano").numero(456).idLocalidad(1L).build())
            .idCredencial(credencial.getIdUsuario())
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    UbicacionDTO ubicacionFacturacion = clienteRecuperado.getUbicacionFacturacion();
    ubicacionFacturacion.setCalle("Calle Nueva");
    ubicacionFacturacion.setNumero(8779);
    UbicacionDTO ubicacionEnvio = clienteRecuperado.getUbicacionEnvio();
    ubicacionEnvio.setCalle("Segunda Calle");
    ubicacionEnvio.setNumero(4550);
    clienteRecuperado.setUbicacionFacturacion(ubicacionFacturacion);
    clienteRecuperado.setUbicacionEnvio(ubicacionEnvio);
    restTemplate.put(apiPrefix + "/clientes", clienteRecuperado);
    clienteRecuperado = restTemplate.getForObject(apiPrefix + "/clientes/3", ClienteDTO.class);
    assertEquals(ubicacionFacturacion, clienteRecuperado.getUbicacionFacturacion());
    assertEquals(ubicacionEnvio, clienteRecuperado.getUbicacionEnvio());
  }

  @Test
  void shouldNotEliminarUbicacionesDeCliente() {
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("elenanocañete")
            .password("siempredebarrio")
            .nombre("Juan")
            .apellido("Cañete")
            .email("caniete@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.COMPRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    ClienteDTO cliente =
        ClienteDTO.builder()
            .bonificacion(BigDecimal.TEN)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(1244557L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .ubicacionFacturacion(
                UbicacionDTO.builder().calle("Sarmiento").numero(789).idLocalidad(1L).build())
            .ubicacionEnvio(
                UbicacionDTO.builder().calle("Belgrano").numero(456).idLocalidad(1L).build())
            .idCredencial(credencial.getIdUsuario())
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    UbicacionDTO ubicacionFacturacion = clienteRecuperado.getUbicacionFacturacion();
    UbicacionDTO ubicacionEnvio = clienteRecuperado.getUbicacionEnvio();
    clienteRecuperado.setUbicacionFacturacion(null);
    clienteRecuperado.setUbicacionEnvio(null);
    restTemplate.put(apiPrefix + "/clientes", clienteRecuperado);
    clienteRecuperado = restTemplate.getForObject(apiPrefix + "/clientes/3", ClienteDTO.class);
    assertEquals(ubicacionFacturacion, clienteRecuperado.getUbicacionFacturacion());
    assertEquals(ubicacionEnvio, clienteRecuperado.getUbicacionEnvio());
  }

  @Test
  void shouldCrearProveedorConUbicacion() {
    UbicacionDTO ubicacion = UbicacionDTO.builder()
      .calle("Belgrano")
      .numero(456)
      .idLocalidad(1L)
      .build();
    ProveedorDTO proveedor =
      ProveedorDTO.builder()
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .contacto("Ricardo")
        .email("ricardodelbarrio@gmail.com")
        .telPrimario("4512778851")
        .telSecundario("784551122")
        .web("")
        .razonSocial("Migral Compuesto")
        .ubicacion(ubicacion)
        .build();
    ProveedorDTO proveedorRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/proveedores", proveedor, ProveedorDTO.class);
    assertEquals(proveedor, proveedorRecuperado);
  }

  @Test
  void shouldCrearProveedorConUbicacionYEditarla() {
    UbicacionDTO ubicacion = UbicacionDTO.builder()
      .calle("Belgrano")
      .numero(456)
      .idLocalidad(1L)
      .build();
    ProveedorDTO proveedor =
      ProveedorDTO.builder()
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .contacto("Ricardo")
        .email("ricardodelbarrio@gmail.com")
        .telPrimario("4512778851")
        .telSecundario("784551122")
        .web("")
        .razonSocial("Migral Compuesto")
        .ubicacion(ubicacion)
        .build();
    ProveedorDTO proveedorRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/proveedores", proveedor, ProveedorDTO.class);
    ubicacion = proveedorRecuperado.getUbicacion();
    ubicacion.setCalle("Rosas");
    ubicacion.setNumero(9666);
    proveedorRecuperado.setUbicacion(ubicacion);
    restTemplate.put(apiPrefix + "/proveedores", proveedorRecuperado);
    proveedorRecuperado = restTemplate.getForObject(apiPrefix+ "/proveedores/" + proveedorRecuperado.getIdProveedor(), ProveedorDTO.class);
    assertEquals(ubicacion, proveedorRecuperado.getUbicacion());
  }

  @Test
  void shouldCrearTransportista() {
    TransportistaDTO transportista =
        TransportistaDTO.builder()
            .telefono("78946551122")
            .web("Ronollega.com")
            .nombre("Transporte Segu Ronollega")
            .build();
    TransportistaDTO transportistaRecuperado =
        restTemplate.postForObject(
            apiPrefix + "/transportistas", transportista, TransportistaDTO.class);
    assertEquals(transportista, transportistaRecuperado);
  }

  @Test
  void shouldCrearTransportistaConUbicacion() {
    TransportistaDTO transportista =
        TransportistaDTO.builder()
            .telefono("78946551122")
            .web("Ronollega.com")
            .nombre("Transporte Segu Ronollega")
            .ubicacion(
                UbicacionDTO.builder()
                    .calle("Los Rios Puros Nacidos del Mar de la Calma")
                    .numero(784445)
                    .idLocalidad(1L)
                    .build())
            .build();
    TransportistaDTO transportistaRecuperado =
        restTemplate.postForObject(
            apiPrefix + "/transportistas", transportista, TransportistaDTO.class);
    assertEquals(transportista, transportistaRecuperado);
  }

  @Test
  void shouldEditarUbicacionDeTransportista() {
    TransportistaDTO transportista =
        TransportistaDTO.builder()
            .telefono("78946551122")
            .web("Ronollega.com")
            .nombre("Transporte Segu Ronollega")
            .ubicacion(
                UbicacionDTO.builder()
                    .calle("Los Rios Puros Nacidos del Mar de la Calma")
                    .numero(784445)
                    .idLocalidad(1L)
                    .build())
            .build();
    TransportistaDTO transportistaRecuperado =
        restTemplate.postForObject(
            apiPrefix + "/transportistas", transportista, TransportistaDTO.class);
    UbicacionDTO ubicacion = transportistaRecuperado.getUbicacion();
    ubicacion.setCalle("Los Prados Rosas de los Elfos Sagrados");
    ubicacion.setNumero(789996);
    transportistaRecuperado.setUbicacion(ubicacion);
    restTemplate.put(apiPrefix + "/transportistas", transportistaRecuperado);
    transportistaRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/transportistas/" + transportistaRecuperado.getIdTransportista(),
            TransportistaDTO.class);
    assertEquals(ubicacion, transportistaRecuperado.getUbicacion());
  }

  @Test
  void shouldEliminarUbicacionesDelClienteAlEliminarlo() {
    this.shouldCrearClienteConUbicaciones();
    restTemplate.delete(apiPrefix + "/clientes/3");
    assertNull(restTemplate.getForObject(apiPrefix + "/ubicaciones/7", UbicacionDTO.class));
    assertNull(restTemplate.getForObject(apiPrefix + "/ubicaciones/6", UbicacionDTO.class));
  }

  @Test
  void shouldEliminarUbicacionesDelSucursalAlEliminarla() {
    this.shouldCrearSucursalResponsableInscripto();
    restTemplate.delete(apiPrefix + "/sucursales/2");
    assertNull(restTemplate.getForObject(apiPrefix + "/ubicaciones/6", UbicacionDTO.class));
  }

  @Test
  void shouldEliminarUbicacionesDelProveedorAlEliminarlo() {
    this.shouldCrearProveedorConUbicacion();
    restTemplate.delete(apiPrefix + "/proveedores/2");
    assertNull(restTemplate.getForObject(apiPrefix + "/ubicaciones/6", UbicacionDTO.class));
  }

  @Test
  void shouldEliminarUbicacionesDelTransportistaAlEliminarla() {
    this.shouldCrearTransportistaConUbicacion();
    restTemplate.delete(apiPrefix + "/transportistas/2");
    assertNull(restTemplate.getForObject(apiPrefix + "/ubicaciones/6", UbicacionDTO.class));
  }

  @Test
  void shouldCrearMedida() {
    MedidaDTO medida = MedidaDTO.builder().nombre("Longitud de Plank").build();
    MedidaDTO medidaRecuperada =
      restTemplate.postForObject(apiPrefix + "/medidas?idSucursal=1", medida, MedidaDTO.class);
    assertEquals(medida, medidaRecuperada);
  }

  @Test
  void shouldCrearRubro() {
    RubroDTO rubro = RubroDTO.builder().nombre("Reparación de Ovnis").build();
    RubroDTO rubroRecuperado =
      restTemplate.postForObject(apiPrefix + "/rubros?idSucursal=1", rubro, RubroDTO.class);
    assertEquals(rubro, rubroRecuperado);
  }

  @Test
  void shouldGetRubrosDeSucursal() {
    this.shouldCrearRubro();
    List<RubroDTO> rubrosRecuperados =
        restTemplate
            .exchange(
                apiPrefix + "/rubros",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RubroDTO>>() {})
            .getBody();
    assertEquals("Ferreteria", rubrosRecuperados.get(0).getNombre());
    assertEquals("Reparación de Ovnis", rubrosRecuperados.get(1).getNombre());
  }

  @Test
  void shouldGetExceptionYaExisteRubroConElNombreIngresadoEnAlta() {
    this.shouldCrearRubro();
    RubroDTO rubro = RubroDTO.builder().nombre("Reparación de Ovnis").build();
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate.postForObject(
                    apiPrefix + "/rubros?idSucursal=1", rubro, RubroDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_rubro_nombre_duplicado", null, Locale.getDefault())));
  }

  @Test
  void shouldGetExceptionYaExisteRubroConElNombreIngresadoEnModificacion() {
    this.shouldCrearRubro();
    RubroDTO rubro = restTemplate.getForObject(apiPrefix + "/rubros/2", RubroDTO.class);
    rubro.setNombre("Ferreteria");
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.put(apiPrefix + "/rubros", rubro));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_rubro_nombre_duplicado", null, Locale.getDefault())));
  }

  @Test
  void shouldCrearProveedorResponsableInscripto() {
    ProveedorDTO proveedor =
      ProveedorDTO.builder()
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .contacto("Ricardo")
        .email("ricardodelbarrio@gmail.com")
        .telPrimario("4512778851")
        .telSecundario("784551122")
        .web("")
        .razonSocial("Migral Compuesto")
        .build();
    ProveedorDTO proveedorRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/proveedores", proveedor, ProveedorDTO.class);
    assertEquals(proveedor, proveedorRecuperado);
  }

  @Test
  void shouldCrearFacturaVentaASucursal1() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=6"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=3"
          + "&idCliente=1",
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
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    FacturaVentaDTO facturaVentaA =
      FacturaVentaDTO.builder().nombreFiscalCliente(cliente.getNombreFiscal())
        .idCliente(cliente.getIdCliente())
        .build();
    facturaVentaA.setIdSucursal(1L);
    facturaVentaA.setIdCliente(1L);
    facturaVentaA.setIdTransportista(transportista.getIdTransportista());
    facturaVentaA.setObservaciones("Factura Venta A test");
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
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaA).build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaA, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaVentaASucursal2() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    this.shouldCrearSucursalResponsableInscripto();
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoUno.getCantidadEnSucursales().stream().filter(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.getIdSucursal().equals(2l))
      .forEach(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.setCantidad(new BigDecimal("10")));
    restTemplate.put(apiPrefix + "/productos", productoUno);
    productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    productoDos.getCantidadEnSucursales().stream().filter(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.getIdSucursal().equals(2l))
      .forEach(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.setCantidad(new BigDecimal("20")));
    restTemplate.put(apiPrefix + "/productos", productoDos);
    productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=6"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=3"
          + "&idCliente=1",
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
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    FacturaVentaDTO facturaVentaA =
      FacturaVentaDTO.builder().nombreFiscalCliente(cliente.getNombreFiscal())
        .idCliente(cliente.getIdCliente())
        .build();
    facturaVentaA.setIdSucursal(2L);
    facturaVentaA.setIdCliente(1L);
    facturaVentaA.setIdTransportista(transportista.getIdTransportista());
    facturaVentaA.setObservaciones("Factura Venta A test");
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
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/2", SucursalDTO.class);
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO = NuevaFacturaVentaDTO.builder()
      .facturaVenta(facturaVentaA)
      .build();
    FacturaVentaDTO[] facturas =
      restTemplate.postForObject(
        apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaA, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }


  @Test
  void shouldEmitirReporteFactura() {
    this.shouldCrearFacturaVentaASucursal1();
    restTemplate.getForObject(apiPrefix + "/facturas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearFacturaVentaB() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_B
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=5"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_B
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=2"
          + "&idCliente=1",
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
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaB =
      FacturaVentaDTO.builder()
        .build();
    facturaVentaB.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaB.setIdCliente(cliente.getIdCliente());
    facturaVentaB.setIdTransportista(transportista.getIdTransportista());
    facturaVentaB.setObservaciones("Factura Venta B test");
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
    facturaVentaB.setFechaVencimiento(LocalDate.now().plusYears(1L));
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/1", UsuarioDTO.class);
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaB).build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaB, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldNotModificarFacturaSiElClienteSeModifica() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=6"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=3"
          + "&idCliente=1",
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
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", ClienteDTO.class);
    FacturaVentaDTO facturaVentaA =
      FacturaVentaDTO.builder().nombreFiscalCliente(cliente.getNombreFiscal()).build();
    facturaVentaA.setObservaciones("Factura Venta A test");
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
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    facturaVentaA.setIdCliente(cliente.getIdCliente());
    facturaVentaA.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaA.setIdTransportista(transportista.getIdTransportista());
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaA).build();
    FacturaVentaDTO facturaVenta =
        restTemplate
            .postForObject(
                apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class)[0];
    String ubicacionSinModificacion = facturaVenta.getUbicacionCliente();
    cliente.setNombreFiscal("Superior Spider Man");
    cliente.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    cliente.getUbicacionFacturacion().setCalle("Rio Torcuato");
    restTemplate.put(apiPrefix + "/clientes", cliente);
    facturaVenta = restTemplate.getForObject(apiPrefix + "/facturas/1", FacturaVentaDTO.class);
    assertEquals(cliente.getNroCliente(), facturaVenta.getNroDeCliente());
    assertEquals("Peter Parker", facturaVenta.getNombreFiscalCliente());
    assertEquals(CategoriaIVA.RESPONSABLE_INSCRIPTO, facturaVenta.getCategoriaIVACliente());
    assertEquals("Usuario Test test (test)", facturaVenta.getNombreUsuario());
    assertEquals(ubicacionSinModificacion, facturaVenta.getUbicacionCliente());
  }

  @Test
  void shouldCrearFacturaVentaC() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_C
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=5"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_C
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=2"
          + "&idCliente=1",
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
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaC =
        FacturaVentaDTO.builder().idCliente(cliente.getIdCliente()).build();
    facturaVentaC.setIdTransportista(transportista.getIdTransportista());
    facturaVentaC.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaC.setIdCliente(cliente.getIdCliente());
    facturaVentaC.setIdTransportista(transportista.getIdTransportista());
    facturaVentaC.setObservaciones("Factura Venta C test");
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
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaC).build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaC, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaVentaX() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_X
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=6"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_X
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=3"
          + "&idCliente=1",
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
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaX =
      FacturaVentaDTO.builder().idCliente(cliente.getIdCliente()).build();
    facturaVentaX.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaX.setIdCliente(cliente.getIdCliente());
    facturaVentaX.setIdTransportista(transportista.getIdTransportista());
    facturaVentaX.setObservaciones("Factura Venta X test");
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
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaX).build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaX, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaVentaY() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_Y
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=6"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_Y
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=3"
          + "&idCliente=1",
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
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaY =
      FacturaVentaDTO.builder().idCliente(cliente.getIdCliente()).build();
    facturaVentaY.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaY.setIdCliente(cliente.getIdCliente());
    facturaVentaY.setIdTransportista(transportista.getIdTransportista());
    facturaVentaY.setObservaciones("Factura Venta Y test");
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
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaY).build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaY, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaVentaPresupuesto() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.PRESUPUESTO
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=5"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.PRESUPUESTO
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=2"
          + "&idCliente=1",
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
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaPresupuesto =
      FacturaVentaDTO.builder().idCliente(cliente.getIdCliente()).build();
    facturaVentaPresupuesto.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaPresupuesto.setIdCliente(cliente.getIdCliente());
    facturaVentaPresupuesto.setIdTransportista(transportista.getIdTransportista());
    facturaVentaPresupuesto.setObservaciones("Factura Venta Presupuesto test");
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
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaPresupuesto).build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaPresupuesto, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaVentaSinTransportista() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.PRESUPUESTO
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=5"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.PRESUPUESTO
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=2"
          + "&idCliente=1",
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
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaPresupuesto =
      FacturaVentaDTO.builder().idCliente(cliente.getIdCliente()).build();
    facturaVentaPresupuesto.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaPresupuesto.setIdCliente(cliente.getIdCliente());
    facturaVentaPresupuesto.setObservaciones("Factura Venta Presupuesto test");
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
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaPresupuesto).build();
    FacturaVentaDTO[] facturas =
      restTemplate.postForObject(
        apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaPresupuesto, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals("", facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearYEliminarFacturaVenta() {
    this.shouldCrearFacturaVentaASucursal1();
    restTemplate.delete(apiPrefix + "/facturas/1");
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.getForObject(apiPrefix + "/facturas/1", FacturaDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage("mensaje_factura_eliminada", null, Locale.getDefault())));
  }

  @Test
  void shouldCrearFacturaVentaXParaClienteSinUbicacionDeFacturacion() {
    this.shouldCrearClienteResponsableInscripto();
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_X
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=6"
          + "&idCliente=1",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-venta?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_X
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=3"
          + "&idCliente=1",
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
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/2", Cliente.class);
    Transportista transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaX =
      FacturaVentaDTO.builder().idCliente(cliente.getIdCliente()).build();
    facturaVentaX.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaX.setIdCliente(cliente.getIdCliente());
    facturaVentaX.setIdTransportista(transportista.getIdTransportista());
    facturaVentaX.setObservaciones("Factura Venta X test");
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
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
      NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaX)
        .build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(facturaVentaX, facturas[0]);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
      credencial.getNombre()
        + " "
        + credencial.getApellido()
        + " ("
        + credencial.getUsername()
        + ")",
      facturas[0].getNombreUsuario());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldNotCrearFacturaVentaAParaClienteSinUbicacionDeFacturacion() {
    this.shouldCrearClienteResponsableInscripto();
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ProductoDTO productoUno =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
        restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon-venta?"
                + "idProducto="
                + productoUno.getIdProducto()
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_A
                + "&movimiento="
                + Movimiento.VENTA
                + "&cantidad=6"
                + "&idCliente=1",
            RenglonFactura.class);
    RenglonFactura renglonDos =
        restTemplate.getForObject(
            apiPrefix
                + "/facturas/renglon-venta?"
                + "idProducto="
                + productoDos.getIdProducto()
                + "&tipoDeComprobante="
                + TipoDeComprobante.FACTURA_A
                + "&movimiento="
                + Movimiento.VENTA
                + "&cantidad=3"
                + "&idCliente=1",
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
    Cliente cliente = restTemplate.getForObject(apiPrefix + "/clientes/2", Cliente.class);
    Transportista transportista =
        restTemplate.getForObject(apiPrefix + "/transportistas/1", Transportista.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    FacturaVentaDTO facturaVentaA =
        FacturaVentaDTO.builder().idCliente(cliente.getIdCliente()).build();
    facturaVentaA.setIdSucursal(sucursal.getIdSucursal());
    facturaVentaA.setIdCliente(cliente.getIdCliente());
    facturaVentaA.setIdTransportista(transportista.getIdTransportista());
    facturaVentaA.setObservaciones("Factura Venta A test");
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
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder().facturaVenta(facturaVentaA).build();
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate.postForObject(
                    apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault())));
  }

  @Test
  void shouldVerificarCantidadDeArticulosEnFacturaA() {
    this.shouldCrearFacturaVentaASucursal1();
    FacturaDTO facturaDTO = restTemplate.getForObject(apiPrefix + "/facturas/1", FacturaDTO.class);
    assertEquals(new BigDecimal("9.000000000000000"), facturaDTO.getCantidadArticulos());
  }

  @Test
  void shouldCrearFacturaCompraASucursal1() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=1"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=4"
          + "&bonificacion=20",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=2"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=3"
          + "&bonificacion=20",
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
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    FacturaCompraDTO facturaCompraA = FacturaCompraDTO.builder()
      .idProveedor(1L)
      .build();
    facturaCompraA.setIdSucursal(1L);
    facturaCompraA.setIdTransportista(1L);
    facturaCompraA.setObservaciones("Factura Compra A test");
    facturaCompraA.setRazonSocialProveedor(proveedor.getRazonSocial());
    facturaCompraA.setFecha(LocalDateTime.now());
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
    facturaCompraA.setIdProveedor(1L);
    facturaCompraA.setIdTransportista(1L);
    FacturaCompraDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/compra", facturaCompraA, FacturaCompraDTO[].class);
    assertEquals(facturaCompraA, facturas[0]);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    TransportistaDTO transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", TransportistaDTO.class);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaCompraASucursal2() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    this.shouldCrearSucursalResponsableInscripto();
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=1"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=10"
          + "&bonificacion=20",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=2"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=20"
          + "&bonificacion=20",
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
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    FacturaCompraDTO facturaCompraA = FacturaCompraDTO.builder()
      .idProveedor(1L)
      .build();
    facturaCompraA.setIdSucursal(2L);
    facturaCompraA.setIdTransportista(1L);
    facturaCompraA.setObservaciones("Factura Compra A test");
    facturaCompraA.setRazonSocialProveedor(proveedor.getRazonSocial());
    facturaCompraA.setFecha(LocalDateTime.now());
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
    facturaCompraA.setIdProveedor(1L);
    facturaCompraA.setIdTransportista(1L);
    FacturaCompraDTO[] facturas =
      restTemplate.postForObject(
        apiPrefix + "/facturas/compra", facturaCompraA, FacturaCompraDTO[].class);
    assertEquals(facturaCompraA, facturas[0]);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/2", SucursalDTO.class);
    TransportistaDTO transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", TransportistaDTO.class);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaBCompraSucursal1() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=1"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_B
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=5"
          + "&bonificacion=20",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=2"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_B
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=2"
          + "&bonificacion=20",
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
    FacturaCompraDTO facturaCompraB = FacturaCompraDTO.builder()
      .idProveedor(1L)
      .build();
    facturaCompraB.setIdSucursal(1L);
    facturaCompraB.setIdTransportista(1L);
    facturaCompraB.setObservaciones("Factura Compra B test");
    facturaCompraB.setFecha(LocalDateTime.now());
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
    facturaCompraB.setIdProveedor(1L);
    facturaCompraB.setIdTransportista(1L);
    FacturaCompraDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/compra", facturaCompraB, FacturaCompraDTO[].class);
    facturaCompraB.setRazonSocialProveedor("Chamaco S.R.L.");
    assertEquals(facturaCompraB, facturas[0]);
    ProveedorDTO proveedor =
        restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    TransportistaDTO transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", TransportistaDTO.class);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaBCompraSucursal2() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    this.shouldCrearSucursalResponsableInscripto();
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=1"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_B
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=5"
          + "&bonificacion=20",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=2"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_B
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=2"
          + "&bonificacion=20",
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
    FacturaCompraDTO facturaCompraB = FacturaCompraDTO.builder()
      .idProveedor(1L)
      .build();
    facturaCompraB.setIdSucursal(2L);
    facturaCompraB.setIdTransportista(1L);
    facturaCompraB.setObservaciones("Factura Compra B test");
    facturaCompraB.setFecha(LocalDateTime.now());
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
    facturaCompraB.setIdProveedor(1L);
    facturaCompraB.setIdTransportista(1L);
    FacturaCompraDTO[] facturas =
      restTemplate.postForObject(
        apiPrefix + "/facturas/compra", facturaCompraB, FacturaCompraDTO[].class);
    facturaCompraB.setRazonSocialProveedor("Chamaco S.R.L.");
    assertEquals(facturaCompraB, facturas[0]);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/2", SucursalDTO.class);
    TransportistaDTO transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", TransportistaDTO.class);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaCompraC() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=1"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_C
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=3"
          + "&bonificacion=0",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=2"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_C
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=1"
          + "&bonificacion=0",
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
    FacturaCompraDTO facturaCompraC = FacturaCompraDTO.builder()
      .idProveedor(1L)
      .build();
    facturaCompraC.setIdTransportista(1L);
    facturaCompraC.setIdSucursal(1L);
    facturaCompraC.setObservaciones("Factura Compra C test");
    facturaCompraC.setFecha(LocalDateTime.now());
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
    facturaCompraC.setIdProveedor(1L);
    facturaCompraC.setIdTransportista(1L);
    FacturaCompraDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/compra", facturaCompraC, FacturaCompraDTO[].class);
    facturaCompraC.setRazonSocialProveedor("Chamaco S.R.L.");
    assertEquals(facturaCompraC, facturas[0]);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    TransportistaDTO transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", TransportistaDTO.class);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaCompraX() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=1"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_X
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=5"
          + "&bonificacion=20",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=2"
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_X
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=2"
          + "&bonificacion=20",
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
    FacturaCompraDTO facturaCompraX = FacturaCompraDTO.builder()
      .idProveedor(1L)
      .build();
    facturaCompraX.setIdSucursal(1L);
    facturaCompraX.setIdTransportista(1L);
    facturaCompraX.setObservaciones("Factura Compra X test");
    facturaCompraX.setFecha(LocalDateTime.now());
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
    facturaCompraX.setIdProveedor(1L);
    facturaCompraX.setIdTransportista(1L);
    FacturaCompraDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/compra", facturaCompraX, FacturaCompraDTO[].class);
    facturaCompraX.setRazonSocialProveedor("Chamaco S.R.L.");
    assertEquals(facturaCompraX, facturas[0]);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    TransportistaDTO transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", TransportistaDTO.class);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCrearFacturaCompraPresupuesto() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=1"
          + "&tipoDeComprobante="
          + TipoDeComprobante.PRESUPUESTO
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=5"
          + "&bonificacion=20",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon-compra?"
          + "idProducto=2"
          + "&tipoDeComprobante="
          + TipoDeComprobante.PRESUPUESTO
          + "&movimiento="
          + Movimiento.COMPRA
          + "&cantidad=2"
          + "&bonificacion=20",
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
    FacturaCompraDTO facturaCompraPresupuesto = FacturaCompraDTO.builder()
      .idProveedor(1L)
      .build();
    facturaCompraPresupuesto.setIdSucursal(1L);
    facturaCompraPresupuesto.setIdTransportista(1L);
    facturaCompraPresupuesto.setObservaciones("Factura Compra Presupuesto test");
    facturaCompraPresupuesto.setFecha(LocalDateTime.now());
    facturaCompraPresupuesto.setTipoComprobante(TipoDeComprobante.PRESUPUESTO);
    facturaCompraPresupuesto.setRenglones(renglones);
    facturaCompraPresupuesto.setSubTotal(subTotal);
    facturaCompraPresupuesto.setRecargoPorcentaje(recargoPorcentaje);
    facturaCompraPresupuesto.setRecargoNeto(recargo_neto);
    facturaCompraPresupuesto.setDescuentoPorcentaje(descuentoPorcentaje);
    facturaCompraPresupuesto.setDescuentoNeto(descuento_neto);
    facturaCompraPresupuesto.setSubTotalBruto(subTotalBruto);
    facturaCompraPresupuesto.setIva105Neto(iva_105_netoFactura);
    facturaCompraPresupuesto.setIva21Neto(iva_21_netoFactura);
    facturaCompraPresupuesto.setTotal(total);
    facturaCompraPresupuesto.setIdProveedor(1L);
    facturaCompraPresupuesto.setIdTransportista(1L);
    FacturaCompraDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/compra", facturaCompraPresupuesto, FacturaCompraDTO[].class);
    facturaCompraPresupuesto.setRazonSocialProveedor("Chamaco S.R.L.");
    assertEquals(facturaCompraPresupuesto, facturas[0]);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    TransportistaDTO transportista =
      restTemplate.getForObject(apiPrefix + "/transportistas/1", TransportistaDTO.class);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(transportista.getNombre(), facturas[0].getNombreTransportista());
  }

  @Test
  void shouldCalcularPreciosDeProductosConRegargo() {
    NuevoProductoDTO productoUno =
      NuevoProductoDTO.builder()
        .codigo("1")
        .descripcion("uno")
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.TEN);}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("21.0"))
        .ivaNeto(new BigDecimal("210"))
        .precioLista(new BigDecimal("1210"))
        .nota("ProductoTest1")
        .publico(true)
        .build();
    NuevoProductoDTO productoDos =
      NuevoProductoDTO.builder()
        .codigo("2")
        .descripcion("dos")
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, new BigDecimal("6"));}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("10.5"))
        .ivaNeto(new BigDecimal("105"))
        .precioLista(new BigDecimal("1105"))
        .nota("ProductoTest2")
        .build();
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    RubroDTO rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", RubroDTO.class);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
    restTemplate.postForObject(
      apiPrefix
        + "/productos?idMedida="
        + medida.getIdMedida()
        + "&idRubro="
        + rubro.getIdRubro()
        + "&idProveedor="
        + proveedor.getIdProveedor()
        + "&idSucursal="
        + sucursal.getIdSucursal(),
      productoUno,
      ProductoDTO.class);
    restTemplate.postForObject(
      apiPrefix
        + "/productos?idMedida="
        + medida.getIdMedida()
        + "&idRubro="
        + rubro.getIdRubro()
        + "&idProveedor="
        + proveedor.getIdProveedor()
        + "&idSucursal="
        + sucursal.getIdSucursal(),
      productoDos,
      ProductoDTO.class);
    long[] idsProductos = {1L, 2L};
    ProductosParaActualizarDTO productosParaActualizarDTO = ProductosParaActualizarDTO.builder()
      .idProducto(idsProductos).descuentoRecargoPorcentaje(BigDecimal.TEN).build();
    restTemplate.put(apiPrefix + "/productos/multiples", productosParaActualizarDTO);
    Producto productoUnoRecuperado =
        restTemplate.getForObject(apiPrefix + "/productos/1", Producto.class);
    Producto productoDosRecuperado =
        restTemplate.getForObject(apiPrefix + "/productos/2", Producto.class);
    assertEquals(new BigDecimal("110.000000000000000"), productoUnoRecuperado.getPrecioCosto());
    assertEquals(new BigDecimal("990.000000000000000"), productoUnoRecuperado.getGananciaNeto());
    assertEquals(
        new BigDecimal("1100.000000000000000"), productoUnoRecuperado.getPrecioVentaPublico());
    assertEquals(new BigDecimal("231.000000000000000"), productoUnoRecuperado.getIvaNeto());
    assertEquals(new BigDecimal("1331.000000000000000"), productoUnoRecuperado.getPrecioLista());
    assertEquals(new BigDecimal("110.000000000000000"), productoDosRecuperado.getPrecioCosto());
    assertEquals(new BigDecimal("990.000000000000000"), productoDosRecuperado.getGananciaNeto());
    assertEquals(
        new BigDecimal("1100.000000000000000"), productoDosRecuperado.getPrecioVentaPublico());
    assertEquals(new BigDecimal("115.500000000000000"), productoDosRecuperado.getIvaNeto());
    assertEquals(new BigDecimal("1215.500000000000000"), productoDosRecuperado.getPrecioLista());
  }

  @Test
  void shouldCambiarPreciosProductos() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    long[] idsProductos = {1L, 2L};
    ProductosParaActualizarDTO productosParaActualizarDTO = ProductosParaActualizarDTO.builder()
      .idProducto(idsProductos).precioCosto(new BigDecimal("150"))
      .gananciaPorcentaje(new BigDecimal("10"))
      .gananciaNeto(new BigDecimal("15"))
      .precioVentaPublico(new BigDecimal("165"))
      .ivaPorcentaje(BigDecimal.ZERO)
      .ivaNeto(BigDecimal.ZERO)
      .precioLista(new BigDecimal("165")).build();
    restTemplate.put(apiPrefix + "/productos/multiples", productosParaActualizarDTO);
    ProductoDTO productoUno = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(new BigDecimal("150.000000000000000"), productoUno.getPrecioCosto());
    assertEquals(new BigDecimal("150.000000000000000"), productoDos.getPrecioCosto());
    assertEquals(new BigDecimal("10.000000000000000"), productoUno.getGananciaPorcentaje());
    assertEquals(new BigDecimal("10.000000000000000"), productoDos.getGananciaPorcentaje());
    assertEquals(new BigDecimal("15.000000000000000"), productoUno.getGananciaNeto());
    assertEquals(new BigDecimal("15.000000000000000"), productoDos.getGananciaNeto());
    assertEquals(new BigDecimal("165.000000000000000"), productoUno.getPrecioVentaPublico());
    assertEquals(new BigDecimal("165.000000000000000"), productoDos.getPrecioVentaPublico());
    assertEquals(new BigDecimal("0E-15"), productoUno.getIvaPorcentaje());
    assertEquals(new BigDecimal("0E-15"), productoDos.getIvaPorcentaje());
    assertEquals(new BigDecimal("0E-15"), productoUno.getIvaNeto());
    assertEquals(new BigDecimal("0E-15"), productoDos.getIvaNeto());
    assertEquals(new BigDecimal("165.000000000000000"), productoDos.getPrecioLista());
    assertEquals(new BigDecimal("165.000000000000000"), productoDos.getPrecioLista());

  }

  @Test
  void shouldCambiarMedidasProductos() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    long[] idsProductos = {1L, 2L};
    MedidaDTO medida = MedidaDTO.builder()
      .nombre("Distancia de Plank")
      .build();
    medida = restTemplate.postForObject(apiPrefix + "/medidas?idSucursal=1", medida, MedidaDTO.class);
    ProductosParaActualizarDTO productosParaActualizarDTO = ProductosParaActualizarDTO.builder()
      .idProducto(idsProductos)
      .idMedida(medida.getIdMedida()).build();
    restTemplate.put(apiPrefix + "/productos/multiples", productosParaActualizarDTO);
    ProductoDTO productoUno = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals("Distancia de Plank", productoUno.getNombreMedida());
    assertEquals("Distancia de Plank", productoDos.getNombreMedida());
  }

  @Test
  void shouldCambiarRubrosProductos() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    long[] idsProductos = {1L, 2L};
    RubroDTO rubro = RubroDTO.builder()
      .nombre("Telefonía Galactica")
      .build();
    rubro = restTemplate.postForObject(apiPrefix + "/rubros?idSucursal=1", rubro, RubroDTO.class);
    ProductosParaActualizarDTO productosParaActualizarDTO = ProductosParaActualizarDTO.builder()
      .idProducto(idsProductos)
      .idRubro(rubro.getIdRubro()).build();
    restTemplate.put(apiPrefix + "/productos/multiples", productosParaActualizarDTO);
    ProductoDTO productoUno = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals("Telefonía Galactica", productoUno.getNombreRubro());
    assertEquals("Telefonía Galactica", productoDos.getNombreRubro());
  }

  @Test
  void shouldCambiarProveedorProductos() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    long[] idsProductos = {1L, 2L};
    ProveedorDTO proveedor =
      ProveedorDTO.builder()
        .razonSocial("Chamigo S.R.L.")
        .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
        .idFiscal(99999999999L)
        .telPrimario("379 59566333")
        .telSecundario("379 41122547")
        .contacto("Raul Jomer")
        .email("chamigosrl@gmail.com")
        .web("www.chamigosrl.com.ar")
        .eliminado(false)
        .saldoCuentaCorriente(BigDecimal.ZERO)
        .build();
    proveedor = restTemplate.postForObject(apiPrefix + "/proveedores", proveedor, ProveedorDTO.class);
    ProductosParaActualizarDTO productosParaActualizarDTO = ProductosParaActualizarDTO.builder()
      .idProducto(idsProductos)
      .idProveedor(proveedor.getIdProveedor()).build();
    restTemplate.put(apiPrefix + "/productos/multiples", productosParaActualizarDTO);
    ProductoDTO productoUno = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals("Chamigo S.R.L.", productoUno.getRazonSocialProveedor());
    assertEquals("Chamigo S.R.L.", productoDos.getRazonSocialProveedor());
  }

  @Test
  void shouldCrearProductoConIva21() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    Rubro rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", Rubro.class);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
    NuevoProductoDTO productoUno =
      NuevoProductoDTO.builder()
        .codigo("123test")
        .descripcion(RandomStringUtils.random(10, true, false))
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.TEN);}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("21.0"))
        .ivaNeto(new BigDecimal("210"))
        .precioLista(new BigDecimal("1210"))
        .nota("Producto Test")
        .build();
    ProductoDTO productoRecuperado =
      restTemplate.postForObject(
        apiPrefix
          + "/productos?idMedida="
          + medida.getIdMedida()
          + "&idRubro="
          + rubro.getIdRubro()
          + "&idProveedor="
          + proveedor.getIdProveedor()
          + "&idSucursal="
          + sucursal.getIdSucursal(),
        productoUno,
        ProductoDTO.class);
    assertEquals(productoUno.getCantidadEnSucursal().get(1L), productoRecuperado.getCantidadTotalEnSucursales());
    assertEquals(productoUno.getIvaPorcentaje(), productoRecuperado.getIvaPorcentaje());
    assertEquals(productoUno.getIvaNeto(), productoRecuperado.getIvaNeto());
    assertEquals(productoUno.getCantMinima(), productoRecuperado.getCantMinima());
    assertEquals(productoUno.getBulto(), productoRecuperado.getBulto());
    assertEquals(productoUno.getCodigo(), productoRecuperado.getCodigo());
    assertEquals(productoUno.getDescripcion(), productoRecuperado.getDescripcion());
    assertEquals(productoUno.getGananciaNeto(), productoRecuperado.getGananciaNeto());
    assertEquals(productoUno.getGananciaPorcentaje(), productoRecuperado.getGananciaPorcentaje());
    assertEquals(productoUno.getPrecioLista(), productoRecuperado.getPrecioLista());
    assertEquals(productoUno.getPrecioVentaPublico(), productoRecuperado.getPrecioVentaPublico());
    assertEquals(productoUno.getPrecioCosto(), productoRecuperado.getPrecioCosto());
    assertEquals(new BigDecimal("21.0"), productoRecuperado.getIvaPorcentaje());
    assertEquals(new BigDecimal("210"), productoRecuperado.getIvaNeto());
  }

  @Test
  void shouldCrearProductoConIva105() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    Rubro rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", Rubro.class);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
    NuevoProductoDTO productoUno =
      NuevoProductoDTO.builder()
        .codigo("123test")
        .descripcion(RandomStringUtils.random(10, true, false))
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.TEN);}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("10.5"))
        .ivaNeto(new BigDecimal("105"))
        .precioLista(new BigDecimal("1105"))
        .nota("Producto Test")
        .build();
    ProductoDTO productoRecuperado =
      restTemplate.postForObject(
        apiPrefix
          + "/productos?idMedida="
          + medida.getIdMedida()
          + "&idRubro="
          + rubro.getIdRubro()
          + "&idProveedor="
          + proveedor.getIdProveedor()
          + "&idSucursal="
          + sucursal.getIdSucursal(),
        productoUno,
        ProductoDTO.class);
    assertEquals(new BigDecimal("10.5"), productoRecuperado.getIvaPorcentaje());
    assertEquals(new BigDecimal("105"), productoRecuperado.getIvaNeto());
  }

  @Test
  void shouldNotCrearProductoDestacado() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    Rubro rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", Rubro.class);
    ProveedorDTO proveedor =
        restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
    NuevoProductoDTO productoUno =
        NuevoProductoDTO.builder()
            .codigo(RandomStringUtils.random(10, false, true))
            .descripcion(RandomStringUtils.random(10, true, false))
            .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.TEN);}})
            .bulto(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .nota("Producto Test")
            .oferta(true)
            .build();
    ProductoDTO productoRecuperado =
        restTemplate.postForObject(
            apiPrefix
                + "/productos?idMedida="
                + medida.getIdMedida()
                + "&idRubro="
                + rubro.getIdRubro()
                + "&idProveedor="
                + proveedor.getIdProveedor()
                + "&idSucursal="
                + sucursal.getIdSucursal(),
            productoUno,
            ProductoDTO.class);
    assertFalse(productoRecuperado.isOferta());
  }

  @Test
  void shouldModificarProducto() {
    this.shouldCrearProductoConIva21();
    ProductoDTO productoAModificar =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoAModificar.setDescripcion("PRODUCTO MODIFICADO.");
    Set<CantidadEnSucursalDTO> cantidadEnSucursal = productoAModificar.getCantidadEnSucursales();
    cantidadEnSucursal.forEach(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.setCantidad(new BigDecimal("52")));
    productoAModificar.setCantidadEnSucursales(cantidadEnSucursal);
    productoAModificar.setCodigo("666");
    restTemplate.put(apiPrefix + "/productos?idMedida=2", productoAModificar);
    ProductoDTO productoModificado =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    assertEquals(productoAModificar, productoModificado);
  }

  @Test
  void shouldNotModificarProductoComoDestacadoSiEsPrivado() {
    this.shouldCrearProductoConIva21();
    ProductoDTO productoAModificar =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoAModificar.setDescripcion("PRODUCTO MODIFICADO.");
    productoAModificar.setOferta(true);
    productoAModificar.setPorcentajeBonificacionOferta(BigDecimal.TEN);
    productoAModificar.setUrlImagen(null);
    productoAModificar.setCodigo("666");
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.put(apiPrefix + "/productos?idMedida=2", productoAModificar));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_oferta_privado_o_sin_imagen", null, Locale.getDefault())));
  }

  @Test
  void shouldNotModificarProductoComoDestacadoSiNoTieneImagen() {
    this.shouldCrearProductoConIva21();
    ProductoDTO productoAModificar =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoAModificar.setDescripcion("PRODUCTO MODIFICADO.");
    Set<CantidadEnSucursalDTO> cantidadEnSucursal = productoAModificar.getCantidadEnSucursales();
    cantidadEnSucursal.forEach(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.setCantidad(new BigDecimal("52")));
    productoAModificar.setCantidadEnSucursales(cantidadEnSucursal);
    productoAModificar.setPublico(false);
    productoAModificar.setOferta(true);
    productoAModificar.setCodigo("666");
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.put(apiPrefix + "/productos?idMedida=2", productoAModificar));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_oferta_privado_o_sin_imagen", null, Locale.getDefault())));
  }

  @Test
  void shouldEliminarProducto() {
    this.shouldCrearProductoConIva21();
    ProductoDTO productoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/productos/busqueda?idSucursal=1&codigo=123test", ProductoDTO.class);
    restTemplate.delete(apiPrefix + "/productos?idProducto=" + productoRecuperado.getIdProducto());
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate.getForObject(
                    apiPrefix + "/productos/" + productoRecuperado.getIdProducto(),
                    ProductoDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_producto_no_existente", null, Locale.getDefault())));
  }

  @Test
  void shouldVerificarProductoConStockDisponibleSucursal1() {
    NuevoProductoDTO productoTestSinStock =
        NuevoProductoDTO.builder()
            .codigo(RandomStringUtils.random(10, false, true))
            .descripcion(RandomStringUtils.random(10, true, false))
            .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.ONE);}})
            .bulto(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .nota("ProductoTestSinStock")
            .publico(true)
            .build();
    ProductoDTO productoSinStock =
        restTemplate.postForObject(
            apiPrefix + "/productos?idMedida=1&idRubro=1&idProveedor=1",
            productoTestSinStock,
            ProductoDTO.class);
    BigDecimal[] cantidades = {BigDecimal.ONE};
    long[] idsProductos = {1L};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder()
            .cantidad(cantidades)
            .idProducto(idsProductos)
            .idSucursal(1L)
            .build();
    Map faltante =
        restTemplate.postForObject(
            apiPrefix + "/productos/disponibilidad-stock",
            productosParaVerificarStockDTO,
            Map.class);
    assertTrue(faltante.isEmpty(), "Debería no devolver faltantes");
  }

  @Test
  void shouldVerificarProductoSinStockDisponibleSucursal1() {
    NuevoProductoDTO productoTestSinStock =
      NuevoProductoDTO.builder()
        .codigo(RandomStringUtils.random(10, false, true))
        .descripcion(RandomStringUtils.random(10, true, false))
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.ZERO);}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("21.0"))
        .ivaNeto(new BigDecimal("210"))
        .precioLista(new BigDecimal("1210"))
        .nota("ProductoTestSinStock")
        .publico(true)
        .build();
    ProductoDTO productoSinStock =
      restTemplate.postForObject(
        apiPrefix + "/productos?idMedida=1&idRubro=1&idProveedor=1",
        productoTestSinStock,
        ProductoDTO.class);
    BigDecimal[] cantidades = {BigDecimal.ONE};
    long[] idsProductos = {productoSinStock.getIdProducto()};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
      ProductosParaVerificarStockDTO.builder()
        .cantidad(cantidades)
        .idProducto(idsProductos)
        .idSucursal(1L)
        .build();
    Map faltante =
      restTemplate.postForObject(
        apiPrefix + "/productos/disponibilidad-stock",
        productosParaVerificarStockDTO,
        Map.class);
    assertFalse(faltante.isEmpty(), "Debería devolver faltantes");
  }

  @Test
  void shouldVerificarProductoConStockDisponibleSucursal2() {
    this.shouldCrearSucursalResponsableInscripto();
    NuevoProductoDTO productoTestSinStock =
      NuevoProductoDTO.builder()
        .codigo(RandomStringUtils.random(10, false, true))
        .descripcion(RandomStringUtils.random(10, true, false))
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(2L, BigDecimal.ONE);}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("21.0"))
        .ivaNeto(new BigDecimal("210"))
        .precioLista(new BigDecimal("1210"))
        .nota("ProductoTestSinStock")
        .publico(true)
        .build();
    ProductoDTO productoSinStock =
      restTemplate.postForObject(
        apiPrefix + "/productos?idMedida=1&idRubro=1&idProveedor=1",
        productoTestSinStock,
        ProductoDTO.class);
    BigDecimal[] cantidades = {BigDecimal.ONE};
    long[] idsProductos = {productoSinStock.getIdProducto()};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder()
            .cantidad(cantidades)
            .idProducto(idsProductos)
            .idSucursal(2L)
            .build();
    Map faltante =
        restTemplate.postForObject(
            apiPrefix + "/productos/disponibilidad-stock",
            productosParaVerificarStockDTO,
            Map.class);
    assertTrue(faltante.isEmpty(), "Debería no devolver faltantes");
  }

  @Test
  void shouldVerificarProductoSinStockDisponibleSucursal2() {
    this.shouldCrearSucursalResponsableInscripto();
    NuevoProductoDTO productoTestSinStock =
      NuevoProductoDTO.builder()
        .codigo(RandomStringUtils.random(10, false, true))
        .descripcion(RandomStringUtils.random(10, true, false))
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.ONE);}})
        .bulto(BigDecimal.ONE)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("21.0"))
        .ivaNeto(new BigDecimal("210"))
        .precioLista(new BigDecimal("1210"))
        .nota("ProductoTestSinStock")
        .publico(true)
        .build();
    ProductoDTO productoSinStock =
      restTemplate.postForObject(
        apiPrefix + "/productos?idMedida=1&idRubro=1&idProveedor=1",
        productoTestSinStock,
        ProductoDTO.class);
    BigDecimal[] cantidades = {BigDecimal.ONE};
    long[] idsProductos = {productoSinStock.getIdProducto()};
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
      ProductosParaVerificarStockDTO.builder()
        .cantidad(cantidades)
        .idProducto(idsProductos)
        .idSucursal(2L)
        .build();
    Map faltante =
      restTemplate.postForObject(
        apiPrefix + "/productos/disponibilidad-stock",
        productosParaVerificarStockDTO,
        Map.class);
    assertFalse(faltante.isEmpty(), "Debería devolver faltantes");
  }

  @Test
  void shouldVerificarStockVenta() {
    this.shouldCrearFacturaVentaASucursal1();
    ProductoDTO producto1 =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
        restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(new BigDecimal("4.000000000000000"), producto1.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("3.000000000000000"), producto2.getCantidadTotalEnSucursales());
    restTemplate.delete(apiPrefix + "/facturas/1");
    producto1 =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto2 =
        restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(new BigDecimal("10.000000000000000"), producto1.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("6.000000000000000"), producto2.getCantidadTotalEnSucursales());
  }

  @Test
  void shouldVerificarStockVentaSucursal1() {
    this.shouldCrearFacturaVentaASucursal1();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    List<CantidadEnSucursalDTO> cantidadesEnSucursalProducto1 = new ArrayList<>();
    producto1
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto1.add(cantidadEnSucursalDTO));
    List<CantidadEnSucursalDTO> cantidadesEnSucursalProducto2 = new ArrayList<>();
    producto2
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto2.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("4.000000000000000"), cantidadesEnSucursalProducto1.get(0).getCantidad());
    assertEquals(new BigDecimal("3.000000000000000"), cantidadesEnSucursalProducto2.get(0).getCantidad());
    restTemplate.delete(apiPrefix + "/facturas/1");
    producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    cantidadesEnSucursalProducto1.clear();
    cantidadesEnSucursalProducto2.clear();
    producto1
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto1.add(cantidadEnSucursalDTO));
    producto2
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto2.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("10.000000000000000"), cantidadesEnSucursalProducto1.get(0).getCantidad());
    assertEquals(new BigDecimal("6.000000000000000"), cantidadesEnSucursalProducto2.get(0).getCantidad());
  }

  @Test
  void shouldVerificarStockVentaSucursal2() {
    this.shouldCrearFacturaVentaASucursal2();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    List<CantidadEnSucursalDTO> cantidadesEnSucursalProducto1 = new ArrayList<>();
    producto1
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto1.add(cantidadEnSucursalDTO));
    List<CantidadEnSucursalDTO> cantidadesEnSucursalProducto2 = new ArrayList<>();
    producto2
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto2.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("4.000000000000000"), cantidadesEnSucursalProducto1.get(1).getCantidad());
    assertEquals(new BigDecimal("17.000000000000000"), cantidadesEnSucursalProducto2.get(1).getCantidad());
    restTemplate.delete(apiPrefix + "/facturas/1");
    producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    cantidadesEnSucursalProducto1.clear();
    cantidadesEnSucursalProducto2.clear();
    producto1
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto1.add(cantidadEnSucursalDTO));
    producto2
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursalProducto2.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("10.000000000000000"), cantidadesEnSucursalProducto1.get(1).getCantidad());
    assertEquals(new BigDecimal("20.000000000000000"), cantidadesEnSucursalProducto2.get(1).getCantidad());
  }

  @Test
  void shouldActualizarStockConCantidadEnSucursal2() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    this.shouldCrearSucursalResponsableInscripto();
    ProductoDTO producto1 =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    assertEquals(new BigDecimal("10.000000000000000"), producto1.getCantidadTotalEnSucursales());
    List<CantidadEnSucursalDTO> cantidadesEnSucursal = new ArrayList<>();
    producto1
        .getCantidadEnSucursales()
        .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("10.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
    assertEquals(new BigDecimal("0E-15"), cantidadesEnSucursal.get(1).getCantidad());
    producto1.getCantidadEnSucursales().stream()
        .filter(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.getIdSucursal().equals(2L))
        .forEach(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.setCantidad(new BigDecimal("15")));
    restTemplate.put(apiPrefix + "/productos", producto1);
    producto1 = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    assertEquals(new BigDecimal("25.000000000000000"), producto1.getCantidadTotalEnSucursales());
    cantidadesEnSucursal.clear();
    producto1
        .getCantidadEnSucursales()
        .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("10.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
    assertEquals(new BigDecimal("15.000000000000000"), cantidadesEnSucursal.get(1).getCantidad());
  }

  @Test
  void shouldVerificarStockCompraSucursal1() {
    this.shouldCrearFacturaCompraASucursal1();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    List<CantidadEnSucursalDTO> cantidadesEnSucursal = new ArrayList<>();
    producto1
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("14.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
    cantidadesEnSucursal.clear();
    producto2
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("9.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
  }

  @Test
  void shouldVerificarStockCompraSucursal2() {
    this.shouldCrearFacturaCompraASucursal2();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    List<CantidadEnSucursalDTO> cantidadesEnSucursal = new ArrayList<>();
    producto1
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("10.000000000000000"), cantidadesEnSucursal.get(1).getCantidad());
    cantidadesEnSucursal.clear();
    producto2
      .getCantidadEnSucursales()
      .forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("20.000000000000000"), cantidadesEnSucursal.get(1).getCantidad());
  }

  @Disabled
  @Test
  void shouldBajaFacturaCompraCuandoLaCantidadEsNegativa() {
    this.shouldCrearFacturaCompraASucursal1();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(new BigDecimal("14.000000000000000"), producto1.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("9.000000000000000"), producto2.getCantidadTotalEnSucursales());
    ProductoDTO productoUno =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO productoDos =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    RenglonFactura renglonUno =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon?"
          + "idProducto="
          + productoUno.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=14"
          + "&descuentoPorcentaje=10",
        RenglonFactura.class);
    RenglonFactura renglonDos =
      restTemplate.getForObject(
        apiPrefix
          + "/facturas/renglon?"
          + "idProducto="
          + productoDos.getIdProducto()
          + "&tipoDeComprobante="
          + TipoDeComprobante.FACTURA_A
          + "&movimiento="
          + Movimiento.VENTA
          + "&cantidad=9"
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
      FacturaVentaDTO.builder().nombreFiscalCliente(cliente.getNombreFiscal()).build();
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
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    restTemplate.postForObject(
      apiPrefix
        + "/facturas/venta?"
        + "idCliente="
        + cliente.getIdCliente()
        + "&idSucursal="
        + sucursal.getIdSucursal()
        + "&idUsuario="
        + credencial.getIdUsuario()
        + "&idTransportista="
        + transportista.getIdTransportista(),
      facturaVentaA,
      FacturaVentaDTO[].class);
    restTemplate.delete(apiPrefix + "/facturas/1");
    producto1 = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto2 = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(BigDecimal.TEN, producto1.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("6.000000000000000"), producto2.getCantidadTotalEnSucursales());
  }

  @Test
  void shouldCrearNotaCreditoVentaDeFacturaASucursal1() {
    this.shouldCrearFacturaVentaASucursal1();
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder()
        .idSucursal(1L)
        .tipoComprobante(TipoDeComprobante.FACTURA_A)
        .numSerie(1L)
        .numFactura(1L)
        .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix
                    + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody()
            .getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    idsRenglonesFacutura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(facturasRecuperadas.get(0).getIdFactura())
        .idsRenglonesFactura(idsRenglonesFacutura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Color equivocado.")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    notaCreditoParaPersistir.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaCreditoParaPersistir, notaGuardada);
    assertEquals(new BigDecimal("4000.000000000000000000000000000000"), notaGuardada.getSubTotal());
    assertEquals(new BigDecimal("400.000000000000000"), notaGuardada.getRecargoNeto());
    assertEquals(new BigDecimal("1000.000000000000000"), notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("3400.000000000000000000000000000000"), notaGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("714.000000000000000000000000000000"), notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("4114.000000000000000000000000000000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_A, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearNotaCreditoVentaDeFacturaASucursal2() {
    this.shouldCrearFacturaVentaASucursal2();
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder()
        .idSucursal(2L)
        .tipoComprobante(TipoDeComprobante.FACTURA_A)
        .numSerie(0L)
        .numFactura(1L)
        .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
      restTemplate
        .exchange(
          apiPrefix
            + "/facturas/venta/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
        .getBody()
        .getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    idsRenglonesFacutura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(facturasRecuperadas.get(0).getIdFactura())
        .idsRenglonesFactura(idsRenglonesFacutura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Color equivocado.")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    notaCreditoParaPersistir.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaCreditoParaPersistir, notaGuardada);
    assertEquals(new BigDecimal("4000.000000000000000000000000000000"), notaGuardada.getSubTotal());
    assertEquals(new BigDecimal("400.000000000000000"), notaGuardada.getRecargoNeto());
    assertEquals(new BigDecimal("1000.000000000000000"), notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("3400.000000000000000000000000000000"), notaGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("714.000000000000000000000000000000"), notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("4114.000000000000000000000000000000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_A, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearNotaCreditoVentaDeFacturaB() {
    this.shouldCrearFacturaVentaB();
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder()
        .tipoComprobante(TipoDeComprobante.FACTURA_B)
        .numSerie(1L)
        .numFactura(1L)
        .idSucursal(1L)
        .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody()
            .getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    idsRenglonesFacutura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", ClienteDTO.class);
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/clientes", cliente);
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
        NuevaNotaCreditoDeFacturaDTO.builder()
            .idFactura(facturasRecuperadas.get(0).getIdFactura())
            .idsRenglonesFactura(idsRenglonesFacutura)
            .cantidades(cantidades)
            .modificaStock(true)
            .motivo("Color equivocado.")
            .build();
    NotaCreditoDTO notaCreditoParaPersistir =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
        restTemplate.postForObject(
            apiPrefix
                + "/notas/credito",
            notaCreditoParaPersistir,
            NotaCreditoDTO.class);
    notaCreditoParaPersistir.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaCreditoParaPersistir, notaGuardada);
    assertEquals(new BigDecimal("4840.000000000000000000000000000000"), notaGuardada.getSubTotal());
    assertEquals(new BigDecimal("484.000000000000000"), notaGuardada.getRecargoNeto());
    assertEquals(new BigDecimal("1210.000000000000000"), notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("3400.000000000000000000000000000000"), notaGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("714.000000000000000000000000000000"), notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("4114.000000000000000000000000000000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_B, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearNotaCreditoVentaDeFacturaX() {
    this.shouldCrearFacturaVentaX();
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder()
            .tipoComprobante(TipoDeComprobante.FACTURA_X)
            .numSerie(1L)
            .numFactura(1L)
            .idSucursal(1L)
            .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody()
            .getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    idsRenglonesFacutura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(facturasRecuperadas.get(0).getIdFactura())
        .idsRenglonesFactura(idsRenglonesFacutura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Color equivocado.")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    notaCreditoParaPersistir.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaCreditoParaPersistir, notaGuardada);
    assertEquals(new BigDecimal("4000.000000000000000000000000000000"), notaGuardada.getSubTotal());
    assertEquals(new BigDecimal("400.000000000000000"), notaGuardada.getRecargoNeto());
    assertEquals(new BigDecimal("1000.000000000000000"), notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("3400.000000000000000000000000000000"), notaGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("3400.000000000000000000000000000000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_X, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearNotaCreditoVentaDeFacturaC() {
    this.shouldCrearFacturaVentaC();
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder()
        .tipoComprobante(TipoDeComprobante.FACTURA_C)
        .numSerie(1L)
        .numFactura(1L)
        .idSucursal(1L)
        .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody()
            .getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    idsRenglonesFacutura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(facturasRecuperadas.get(0).getIdFactura())
        .idsRenglonesFactura(idsRenglonesFacutura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Color equivocado.")
        .build();
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    notaCreditoParaPersistir.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaCreditoParaPersistir, notaGuardada);
    assertEquals(new BigDecimal("4840.000000000000000000000000000000"), notaGuardada.getSubTotal());
    assertEquals(new BigDecimal("484.000000000000000"), notaGuardada.getRecargoNeto());
    assertEquals(new BigDecimal("1210.000000000000000"), notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("4114.000000000000000000000000000000"), notaGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("0E-30"), notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("4114.000000000000000000000000000000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_C, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearNotaCreditoVentaASinFactura() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO =
        NuevaNotaCreditoSinFacturaDTO.builder()
            .detalle("Diferencia de precio")
            .monto(new BigDecimal("1000"))
            .tipo(TipoDeComprobante.NOTA_CREDITO_A)
            .idCliente(1L)
            .idSucursal(sucursal.getIdSucursal())
            .motivo("Descuento mal aplicado")
            .build();
    NotaCreditoDTO notaPorGuardar =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito/calculos-sin-factura",
            nuevaNotaCreditoSinFacturaDTO,
            NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito", notaPorGuardar, NotaCreditoDTO.class);
    notaPorGuardar.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaPorGuardar, notaGuardada);
    assertEquals(new BigDecimal("826.446280991735537"), notaGuardada.getSubTotal());
    assertEquals(BigDecimal.ZERO, notaGuardada.getRecargoNeto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("826.446280991735537"), notaGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("173.553719008264463"), notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("1000.000000000000000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_A, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearNotaCreditoVentaBSinFactura() {
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", ClienteDTO.class);
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/clientes", cliente);
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO =
      NuevaNotaCreditoSinFacturaDTO.builder()
        .detalle("Diferencia de precio")
        .monto(new BigDecimal("1000"))
        .tipo(TipoDeComprobante.NOTA_CREDITO_B)
        .idCliente(1L)
        .idSucursal(sucursal.getIdSucursal())
        .motivo("Descuento mal aplicado")
        .build();
    NotaCreditoDTO notaPorGuardar =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos-sin-factura",
        nuevaNotaCreditoSinFacturaDTO,
        NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito", notaPorGuardar, NotaCreditoDTO.class);
    notaPorGuardar.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaPorGuardar, notaGuardada);
    assertEquals(new BigDecimal("1000"), notaGuardada.getSubTotal());
    assertEquals(BigDecimal.ZERO, notaGuardada.getRecargoNeto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("826.446280991735537"), notaGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("173.553719008264463"), notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("1000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_B, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldCrearNotaCreditoVentaCSinFactura() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO =
      NuevaNotaCreditoSinFacturaDTO.builder()
        .detalle("Diferencia de precio")
        .monto(new BigDecimal("1000"))
        .tipo(TipoDeComprobante.NOTA_CREDITO_C)
        .idCliente(1L)
        .idSucursal(sucursal.getIdSucursal())
        .motivo("Descuento mal aplicado")
        .build();
    NotaCreditoDTO notaPorGuardar =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos-sin-factura",
        nuevaNotaCreditoSinFacturaDTO,
        NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito", notaPorGuardar, NotaCreditoDTO.class);
    notaPorGuardar.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaPorGuardar, notaGuardada);
    assertEquals(new BigDecimal("1000"), notaGuardada.getSubTotal());
    assertEquals(BigDecimal.ZERO, notaGuardada.getRecargoNeto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("1000"), notaGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("1000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_C, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }


  @Test
  void shouldCrearNotaCreditoVentaXSinFactura() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO =
      NuevaNotaCreditoSinFacturaDTO.builder()
        .detalle("Diferencia de precio")
        .monto(new BigDecimal("1000"))
        .tipo(TipoDeComprobante.NOTA_CREDITO_X)
        .idCliente(1L)
        .idSucursal(sucursal.getIdSucursal())
        .motivo("Descuento mal aplicado")
        .build();
    NotaCreditoDTO notaPorGuardar =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos-sin-factura",
        nuevaNotaCreditoSinFacturaDTO,
        NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito", notaPorGuardar, NotaCreditoDTO.class);
    notaPorGuardar.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaPorGuardar, notaGuardada);
    assertEquals(new BigDecimal("1000"), notaGuardada.getSubTotal());
    assertEquals(BigDecimal.ZERO, notaGuardada.getRecargoNeto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getDescuentoNeto());
    assertEquals(new BigDecimal("1000"), notaGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("1000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_X, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
  }

  @Test
  void shouldNotCrearNotaCreditoVentaSinRenglonesDeFacturaA() {
    this.shouldCrearFacturaVentaASucursal1();
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder()
        .tipoComprobante(TipoDeComprobante.FACTURA_A)
        .numSerie(1L)
        .numFactura(1L)
        .idSucursal(1L)
        .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
      restTemplate
        .exchange(
          apiPrefix
            + "/facturas/venta/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
        .getBody()
        .getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    idsRenglonesFacutura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("5");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
        NuevaNotaCreditoDeFacturaDTO.builder()
            .idFactura(facturasRecuperadas.get(0).getIdFactura())
            .idsRenglonesFactura(idsRenglonesFacutura)
            .cantidades(cantidades)
            .modificaStock(true)
            .motivo("Color equivocado.")
            .build();
    NotaCreditoDTO notaCreditoParaPersistir =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    notaCreditoParaPersistir.setRenglonesNotaCredito(null);
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate.postForObject(
                    apiPrefix + "/notas/credito", notaCreditoParaPersistir, NotaCreditoDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_nota_de_renglones_vacio", null, Locale.getDefault())));
  }

  @Test
  void shouldNotCalcularNotaCreditoVentaSinRenglonesDeFacturaA() {
    this.shouldCrearFacturaVentaASucursal1();
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder()
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .numSerie(1L)
            .numFactura(1L)
            .idSucursal(1L)
            .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody()
            .getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    BigDecimal[] cantidades = new BigDecimal[1];
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
        NuevaNotaCreditoDeFacturaDTO.builder()
            .idFactura(facturasRecuperadas.get(0).getIdFactura())
            .idsRenglonesFactura(idsRenglonesFacutura)
            .cantidades(cantidades)
            .modificaStock(true)
            .motivo("Color equivocado.")
            .build();
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate.postForObject(
                    apiPrefix + "/notas/credito/calculos",
                    nuevaNotaCreditoDTO,
                    NotaCreditoDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_nota_de_renglones_vacio", null, Locale.getDefault())));
  }

  @Test
  void shouldVerificarStockNotaCreditoVenta() {
    this.shouldCrearNotaCreditoVentaDeFacturaB();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(new BigDecimal("10.000000000000000"), producto1.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("4.000000000000000"), producto2.getCantidadTotalEnSucursales());
    restTemplate.delete(apiPrefix + "/notas/1");
    producto1 = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto2 = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(new BigDecimal("5.000000000000000"), producto1.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("4.000000000000000"), producto2.getCantidadTotalEnSucursales());
  }

  @Test
  void shouldVerificarStockNotaCreditoVentaSucursal1() {
    this.shouldCrearNotaCreditoVentaDeFacturaASucursal1();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    List<CantidadEnSucursalDTO> cantidadesEnSucursal = new ArrayList<>();
    producto1.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("9.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
    cantidadesEnSucursal.clear();
    producto2.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("3.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
    restTemplate.delete(apiPrefix + "/notas/1");
    producto1 = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto2 = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    cantidadesEnSucursal.clear();
    producto1.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("4.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
    cantidadesEnSucursal.clear();
    producto2.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("3.000000000000000"), cantidadesEnSucursal.get(0).getCantidad());
  }

  @Test
  void shouldVerificarStockNotaCreditoVentaSucursal2() {
    this.shouldCrearNotaCreditoVentaDeFacturaASucursal2();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    List<CantidadEnSucursalDTO> cantidadesEnSucursal = new ArrayList<>();
    producto1.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("9.000000000000000"), cantidadesEnSucursal.get(1).getCantidad());
    cantidadesEnSucursal.clear();
    producto2.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("17.000000000000000"), cantidadesEnSucursal.get(1).getCantidad());
    restTemplate.delete(apiPrefix + "/notas/1");
    producto1 = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto2 = restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    cantidadesEnSucursal.clear();
    producto1.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("4.000000000000000"), cantidadesEnSucursal.get(1).getCantidad());
    cantidadesEnSucursal.clear();
    producto2.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadesEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("17.000000000000000"), cantidadesEnSucursal.get(1).getCantidad());
  }

  @Test
  void shouldCrearNotaCreditoCompraConFacturaA() {
    this.shouldCrearFacturaCompraASucursal1();
    Long[] idsFactura = new Long[1];
    idsFactura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("3");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(1L)
        .idsRenglonesFactura(idsFactura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Devolución")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaCreditoRecuperada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    assertEquals(notaCreditoParaPersistir, notaCreditoRecuperada);
    assertEquals(new BigDecimal("240.000000000000000000000000000000"), notaCreditoRecuperada.getSubTotal());
    assertEquals(new BigDecimal("24.000000000000000"), notaCreditoRecuperada.getRecargoNeto());
    assertEquals(new BigDecimal("60.000000000000000"), notaCreditoRecuperada.getDescuentoNeto());
    assertEquals(new BigDecimal("204.000000000000000000000000000000"), notaCreditoRecuperada.getSubTotalBruto());
    assertEquals(new BigDecimal("42.840000000000000000000000000000"), notaCreditoRecuperada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva105Neto());
    assertEquals(new BigDecimal("246.840000000000000000000000000000"), notaCreditoRecuperada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_A, notaCreditoRecuperada.getTipoComprobante());
  }

  @Test
  void shouldCrearNotaCreditoBCompraConFacturaSucursal1() {
    this.shouldCrearFacturaBCompraSucursal1();
    Long[] idsFactura = new Long[1];
    idsFactura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("3");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(1L)
        .idsRenglonesFactura(idsFactura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Devolución")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaCreditoRecuperada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    assertEquals(notaCreditoParaPersistir, notaCreditoRecuperada);
    assertEquals(new BigDecimal("290.400000000000000000000000000000"), notaCreditoRecuperada.getSubTotal());
    assertEquals(new BigDecimal("29.040000000000000"), notaCreditoRecuperada.getRecargoNeto());
    assertEquals(new BigDecimal("72.600000000000000"), notaCreditoRecuperada.getDescuentoNeto());
    assertEquals(new BigDecimal("204.000000000000000000000000000000"), notaCreditoRecuperada.getSubTotalBruto());
    assertEquals(new BigDecimal("42.840000000000000000000000000000"), notaCreditoRecuperada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva105Neto());
    assertEquals(new BigDecimal("246.840000000000000000000000000000"), notaCreditoRecuperada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_B, notaCreditoRecuperada.getTipoComprobante());
  }

  @Test
  void shouldCrearNotaCreditoBCompraConFacturaSucursal2() {
    this.shouldCrearFacturaBCompraSucursal2();
    Long[] idsFactura = new Long[1];
    idsFactura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("3");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(1L)
        .idsRenglonesFactura(idsFactura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Devolución")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaCreditoRecuperada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    assertEquals(notaCreditoParaPersistir, notaCreditoRecuperada);
    assertEquals(new BigDecimal("290.400000000000000000000000000000"), notaCreditoRecuperada.getSubTotal());
    assertEquals(new BigDecimal("29.040000000000000"), notaCreditoRecuperada.getRecargoNeto());
    assertEquals(new BigDecimal("72.600000000000000"), notaCreditoRecuperada.getDescuentoNeto());
    assertEquals(new BigDecimal("204.000000000000000000000000000000"), notaCreditoRecuperada.getSubTotalBruto());
    assertEquals(new BigDecimal("42.840000000000000000000000000000"), notaCreditoRecuperada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva105Neto());
    assertEquals(new BigDecimal("246.840000000000000000000000000000"), notaCreditoRecuperada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_B, notaCreditoRecuperada.getTipoComprobante());
  }

  @Test
  void shouldCrearNotaCreditoCompraConFacturaX() {
    this.shouldCrearFacturaCompraX();
    Long[] idsFactura = new Long[1];
    idsFactura[0] = 1L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("3");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
      NuevaNotaCreditoDeFacturaDTO.builder()
        .idFactura(1L)
        .idsRenglonesFactura(idsFactura)
        .cantidades(cantidades)
        .modificaStock(true)
        .motivo("Devolución")
        .build();
    NotaCreditoDTO notaCreditoParaPersistir =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaCreditoRecuperada =
      restTemplate.postForObject(
        apiPrefix
          + "/notas/credito",
        notaCreditoParaPersistir,
        NotaCreditoDTO.class);
    assertEquals(notaCreditoParaPersistir, notaCreditoRecuperada);
    assertEquals(new BigDecimal("240.000000000000000000000000000000"), notaCreditoRecuperada.getSubTotal());
    assertEquals(new BigDecimal("24.000000000000000"), notaCreditoRecuperada.getRecargoNeto());
    assertEquals(new BigDecimal("60.000000000000000"), notaCreditoRecuperada.getDescuentoNeto());
    assertEquals(new BigDecimal("204.000000000000000000000000000000"), notaCreditoRecuperada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva105Neto());
    assertEquals(new BigDecimal("204.000000000000000000000000000000"), notaCreditoRecuperada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_X, notaCreditoRecuperada.getTipoComprobante());
  }

  @Test
  void shouldCrearNotaCreditoCompraASinFactura() {
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO =
        NuevaNotaCreditoSinFacturaDTO.builder()
            .detalle("RenglonNotaCredito")
            .monto(new BigDecimal("1000"))
            .tipo(TipoDeComprobante.NOTA_CREDITO_A)
            .idProveedor(1L)
            .idSucursal(1L)
            .motivo("Descuento mal aplicado")
            .build();
    NotaCreditoDTO notaPorGuardar =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito/calculos-sin-factura",
            nuevaNotaCreditoSinFacturaDTO,
            NotaCreditoDTO.class);
    NotaCreditoDTO notaCreditoRecuperada =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito", notaPorGuardar, NotaCreditoDTO.class);
    notaPorGuardar.setNroNota(notaCreditoRecuperada.getNroNota());
    assertEquals(notaPorGuardar, notaCreditoRecuperada);
    assertEquals(new BigDecimal("826.446280991735537"), notaCreditoRecuperada.getSubTotal());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getRecargoNeto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getDescuentoNeto());
    assertEquals(new BigDecimal("826.446280991735537"), notaCreditoRecuperada.getSubTotalBruto());
    assertEquals(new BigDecimal("173.553719008264463"), notaCreditoRecuperada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva105Neto());
    assertEquals(new BigDecimal("1000.000000000000000"), notaCreditoRecuperada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_A, notaCreditoRecuperada.getTipoComprobante());
  }

  @Test
  void shouldCrearNotaCreditoCompraBSinFactura() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO =
      NuevaNotaCreditoSinFacturaDTO.builder()
        .detalle("RenglonNotaCredito")
        .monto(new BigDecimal("1000"))
        .tipo(TipoDeComprobante.NOTA_CREDITO_B)
        .idProveedor(1L)
        .idSucursal(1L)
        .motivo("Descuento mal aplicado")
        .build();
    NotaCreditoDTO notaPorGuardar =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos-sin-factura",
        nuevaNotaCreditoSinFacturaDTO,
        NotaCreditoDTO.class);
    NotaCreditoDTO notaCreditoRecuperada =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito", notaPorGuardar, NotaCreditoDTO.class);
    notaPorGuardar.setNroNota(notaCreditoRecuperada.getNroNota());
    assertEquals(notaPorGuardar, notaCreditoRecuperada);
    assertEquals(new BigDecimal("1000"), notaCreditoRecuperada.getSubTotal());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getRecargoNeto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getDescuentoNeto());
    assertEquals(new BigDecimal("826.446280991735537"), notaCreditoRecuperada.getSubTotalBruto());
    assertEquals(new BigDecimal("173.553719008264463"), notaCreditoRecuperada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva105Neto());
    assertEquals(new BigDecimal("1000"), notaCreditoRecuperada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_B, notaCreditoRecuperada.getTipoComprobante());
  }

  @Test
  void shouldCrearNotaCreditoCompraXSinFactura() {
    NuevaNotaCreditoSinFacturaDTO nuevaNotaCreditoSinFacturaDTO =
      NuevaNotaCreditoSinFacturaDTO.builder()
        .detalle("RenglonNotaCredito")
        .monto(new BigDecimal("1000"))
        .tipo(TipoDeComprobante.NOTA_CREDITO_X)
        .idCliente(1L)
        .idSucursal(1L)
        .motivo("Descuento mal aplicado")
        .build();
    NotaCreditoDTO notaPorGuardar =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito/calculos-sin-factura",
        nuevaNotaCreditoSinFacturaDTO,
        NotaCreditoDTO.class);
    NotaCreditoDTO notaCreditoRecuperada =
      restTemplate.postForObject(
        apiPrefix + "/notas/credito", notaPorGuardar, NotaCreditoDTO.class);
    notaPorGuardar.setNroNota(notaCreditoRecuperada.getNroNota());
    assertEquals(notaPorGuardar, notaCreditoRecuperada);
    assertEquals(new BigDecimal("1000"), notaCreditoRecuperada.getSubTotal());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getRecargoNeto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getDescuentoNeto());
    assertEquals(new BigDecimal("1000"), notaCreditoRecuperada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva21Neto());
    assertEquals(BigDecimal.ZERO, notaCreditoRecuperada.getIva105Neto());
    assertEquals(new BigDecimal("1000"), notaCreditoRecuperada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_X, notaCreditoRecuperada.getTipoComprobante());
  }

  @Test
  void shouldCrearNotaDebitoAParaClienteConRecibo() {
    this.abrirCaja();
    this.crearReciboParaCliente(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("Tiene una deuda muy vieja que no paga.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_A)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoAParaProveedorConRecibo() {
    this.abrirCaja();
    this.crearReciboParaProveedor(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("No pagamos, la vida es así.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_A)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoBParaClienteConRecibo() {
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", ClienteDTO.class);
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/clientes", cliente);
    this.abrirCaja();
    this.crearReciboParaCliente(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("Tiene una deuda muy vieja que no paga.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_B)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoBParaProveedorConRecibo() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix +  "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    this.abrirCaja();
    this.crearReciboParaProveedor(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("Se nos pasó un vencimiento.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_B)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoCParaClienteDeRecibo() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    this.abrirCaja();
    this.crearReciboParaCliente(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("Tiene una deuda muy vieja que no paga.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_C)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoCParaProveedorDeRecibo() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    ProveedorDTO proveedor = restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    proveedor.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/proveedores", proveedor);
    this.abrirCaja();
    this.crearReciboParaProveedor(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("Tiene una deuda muy vieja que no paga.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_C)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoXParaClienteDeRecibo() {
    this.abrirCaja();
    this.crearReciboParaCliente(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("Tiene una deuda muy vieja que no paga.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_X)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoXParaProveedorDeRecibo() {
    this.abrirCaja();
    this.crearReciboParaProveedor(100, 1L, 1L);
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
      NuevaNotaDebitoDeReciboDTO.builder()
        .idRecibo(1L)
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .motivo("Tiene una deuda muy vieja que no paga.")
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_X)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1600.000000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoAParaClienteSinRecibo() {
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idCliente(1L)
        .motivo("Tiene una deuda muy vieja que no paga.")
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_A)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1500.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoAParaProveedorSinRecibo() {
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idProveedor(1L)
        .motivo("Tenemos una deuda que no pagamos.")
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_A)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1500.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoBParaClienteSinRecibo() {
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/1", ClienteDTO.class);
    cliente.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/clientes", cliente);
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idCliente(1L)
        .motivo("Tiene una deuda muy vieja que no paga.")
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_B)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1500.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoBParaProveedorSinRecibo() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idProveedor(1L)
        .motivo("Tiene una deuda muy vieja que no paga.")
        .gastoAdministrativo(new BigDecimal("1500.00"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_B)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1500.000000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoCParaClienteSinRecibo() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idCliente(1L)
        .motivo("Tiene una deuda muy vieja que no paga.")
        .gastoAdministrativo(new BigDecimal("1000"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_C)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoCParaProveedorSinRecibo() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    ProveedorDTO proveedor = restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    proveedor.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/proveedores", proveedor);
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idCliente(1L)
        .motivo("Tiene una deuda muy vieja que no paga.")
        .gastoAdministrativo(new BigDecimal("1000"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_C)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoXParaClienteSinRecibo() {
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idCliente(1L)
        .motivo("Tiene una deuda muy vieja que no paga.")
        .gastoAdministrativo(new BigDecimal("1000"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_X)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldCrearNotaDebitoXParaProveedorSinRecibo() {
    NuevaNotaDebitoSinReciboDTO nuevaNotaDebitoSinReciboDeCliente =
      NuevaNotaDebitoSinReciboDTO.builder()
        .idCliente(1L)
        .motivo("Tiene una deuda muy vieja que no paga.")
        .gastoAdministrativo(new BigDecimal("1000"))
        .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_X)
        .idSucursal(1L)
        .build();
    NotaDebitoDTO notaDebitoCalculada =
      restTemplate.postForObject(apiPrefix + "/notas/debito/calculos-sin-recibo", nuevaNotaDebitoSinReciboDeCliente, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =restTemplate.postForObject(apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(BigDecimal.ZERO, notaDebitoGuardada.getIva21Neto());
    assertEquals(new BigDecimal("1000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
  }

  @Test
  void shouldVerificarStockNotaCreditoCompra() {
    this.shouldCrearNotaCreditoBCompraConFacturaSucursal1();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    assertEquals(new BigDecimal("12.000000000000000"), producto1.getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("8.000000000000000"), producto2.getCantidadTotalEnSucursales());
  }

  @Test
  void shouldVerificarStockNotaCreditoCompraSucursal1() {
    this.shouldCrearNotaCreditoBCompraConFacturaSucursal1();
    List<CantidadEnSucursalDTO> cantidadEnSucursal = new ArrayList<>();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto1.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("12.000000000000000"), cantidadEnSucursal.get(0).getCantidad());
    cantidadEnSucursal.clear();
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    producto2.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("8.000000000000000"), cantidadEnSucursal.get(0).getCantidad());
  }

  @Test
  void shouldVerificarStockNotaCreditoCompraSucursal2() {
    this.shouldCrearNotaCreditoBCompraConFacturaSucursal2();
    List<CantidadEnSucursalDTO> cantidadEnSucursal = new ArrayList<>();
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    producto1.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("2.000000000000000"), cantidadEnSucursal.get(1).getCantidad());
    cantidadEnSucursal.clear();
    ProductoDTO producto2 =
      restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    producto2.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> cantidadEnSucursal.add(cantidadEnSucursalDTO));
    assertEquals(new BigDecimal("2.000000000000000"), cantidadEnSucursal.get(1).getCantidad());
  }

  @Test
  void shouldComprobarSaldoCuentaCorrienteCliente() {
    this.abrirCaja();
    this.shouldCrearFacturaVentaB();
    assertEquals(
        new BigDecimal("-5616.800000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.crearReciboParaCliente(5992.5, 1L, 1L);
    assertEquals(
        new BigDecimal("375.700000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    restTemplate.delete(apiPrefix + "/facturas/1");
    assertEquals(
      new BigDecimal("5992.500000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.crearNotaDebitoParaCliente();
    assertEquals(
        new BigDecimal("-121.000000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.crearReciboParaCliente(6113.5, 1L, 1L);
    assertEquals(
        new BigDecimal("5992.500000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.shouldCrearFacturaVentaB();
    this.crearNotaCreditoParaCliente();
    assertEquals(
        new BigDecimal("4489.700000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
    this.shouldCrearFacturaVentaASucursal1();
    assertEquals(
        new BigDecimal("-2701.300000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class));
  }

  @Test
  void shouldComprobarSaldoParcialCuentaCorrienteCliente() {
    this.abrirCaja();
    this.shouldCrearFacturaVentaB();
    this.crearReciboParaCliente(5992.5, 1L, 1L);
    this.crearNotaDebitoParaCliente();
    this.crearReciboParaCliente(6113.5, 1L, 1L);
    this.crearNotaCreditoParaCliente();
    List<RenglonCuentaCorriente> renglonesCuentaCorriente =
      restTemplate
        .exchange(
          apiPrefix + "/cuentas-corriente/1/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {
          })
        .getBody()
        .getContent();
    assertEquals(new Double(4489.7), renglonesCuentaCorriente.get(0).getSaldo());
    assertEquals(new Double(375.7), renglonesCuentaCorriente.get(1).getSaldo());
    assertEquals(new Double(-5737.8), renglonesCuentaCorriente.get(2).getSaldo());
    assertEquals(new Double(375.7), renglonesCuentaCorriente.get(3).getSaldo());
    assertEquals(new Double(-5616.8), renglonesCuentaCorriente.get(4).getSaldo());
  }
  
  @Test
  void shouldCrearReporteCuentaCorrienteCliente() {
    this.shouldComprobarSaldoParcialCuentaCorrienteCliente();
    BusquedaCuentaCorrienteClienteCriteria criteria = BusquedaCuentaCorrienteClienteCriteria.builder()
      .idCliente(1L)
      .build();
    restTemplate.postForObject(apiPrefix + "/cuentas-corriente/clientes/reporte/criteria?formato=xlsx", criteria, byte[].class);
    restTemplate.postForObject(apiPrefix + "/cuentas-corriente/clientes/reporte/criteria?formato=pdf", criteria, byte[].class);
  }

  @Test
  void shouldCrearPedido() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
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
    NuevoPedidoDTO nuevoPedidoDTO =
      NuevoPedidoDTO.builder()
        .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
        .recargoPorcentaje(new BigDecimal("5"))
        .observaciones("Nuevo Pedido Test")
        .renglones(renglonesPedidoDTO)
        .idSucursal(1L)
        .idUsuario(2L)
        .idCliente(1L)
        .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_FACTURACION)
        .build();
    PedidoDTO pedidoRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/pedidos",
        nuevoPedidoDTO,
        PedidoDTO.class);
    assertEquals(new BigDecimal("5947.200000000000000"), pedidoRecuperado.getTotalEstimado());
    assertEquals(pedidoRecuperado.getObservaciones(), nuevoPedidoDTO.getObservaciones());
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
  }

  @Test
  void shouldCalcularPiePedido() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
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
    List<RenglonPedidoDTO> renglonesPedido =
      Arrays.asList(
        restTemplate.postForObject(
          apiPrefix + "/pedidos/renglones/clientes/1", renglonesPedidoDTO, RenglonPedidoDTO[].class));
    BigDecimal descuentoPorcentaje = new BigDecimal("20");
    BigDecimal recargoPorcentaje = BigDecimal.ZERO;
    NuevosResultadosPedido nuevosResultadosPedido = new NuevosResultadosPedido();
    nuevosResultadosPedido.setRenglones(renglonesPedido);
    nuevosResultadosPedido.setDescuentoPorcentaje(descuentoPorcentaje);
    nuevosResultadosPedido.setRecargoPorcentaje(recargoPorcentaje);
    Resultados resultados = restTemplate.postForObject(apiPrefix + "/pedidos/calculo-pedido", nuevosResultadosPedido, Resultados.class);
    assertEquals(new BigDecimal("6608.000000000000000000000000000000"), resultados.getSubTotal());
    assertEquals(new BigDecimal("1321.60000000000000000000000000000000"), resultados.getDescuentoNeto());
    assertEquals(new BigDecimal("0E-32"), resultados.getRecargoNeto());
    assertEquals(new BigDecimal("5286.40000000000000000000000000000000"), resultados.getSubTotalBruto());
    assertEquals(null, resultados.getIva105Neto());
    assertEquals(null, resultados.getIva21Neto());
    assertEquals(new BigDecimal("5286.40000000000000000000000000000000"), resultados.getTotal());
  }

  @Test
  void shouldNotActualizarPedidoPorNumeroNoExistente() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
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
    NuevoPedidoDTO nuevoPedidoDTO =
        NuevoPedidoDTO.builder()
            .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
            .recargoPorcentaje(new BigDecimal("5"))
            .observaciones("Nuevo Pedido Test")
            .renglones(renglonesPedidoDTO)
            .idSucursal(1L)
            .idUsuario(2L)
            .idCliente(1L)
            .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_FACTURACION)
            .build();
    PedidoDTO pedidoRecuperado =
        restTemplate.postForObject(apiPrefix + "/pedidos", nuevoPedidoDTO, PedidoDTO.class);
    pedidoRecuperado.setNroPedido(1L);
    pedidoRecuperado.setRenglones(
        restTemplate
            .exchange(
                apiPrefix + "/pedidos/" + pedidoRecuperado.getIdPedido() + "/renglones",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RenglonPedidoDTO>>() {})
            .getBody());
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate.put(
                    apiPrefix
                        + "/pedidos?idSucursal=1&idUsuario=2&idCliente=1&tipoDeEnvio=USAR_UBICACION_FACTURACION",
                    pedidoRecuperado));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_pedido_no_existente", null, Locale.getDefault())));
  }

  @Test
  void shouldCrearPedidoConUbicacionFacturacion() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
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
    NuevoPedidoDTO nuevoPedidoDTO =
      NuevoPedidoDTO.builder()
        .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
        .recargoPorcentaje(new BigDecimal("5"))
        .observaciones("Nuevo Pedido Test")
        .renglones(renglonesPedidoDTO)
        .idSucursal(1L)
        .idUsuario(2L)
        .idCliente(1L)
        .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_FACTURACION)
        .build();
    PedidoDTO pedidoRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/pedidos",
        nuevoPedidoDTO,
        PedidoDTO.class);
    assertEquals("Rio Parana 14500 Corrientes Corrientes", pedidoRecuperado.getDetalleEnvio());
  }

  @Test
  void shouldCrearPedidoConUbicacionEnvio() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
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
    NuevoPedidoDTO nuevoPedidoDTO =
      NuevoPedidoDTO.builder()
        .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
        .recargoPorcentaje(new BigDecimal("5"))
        .observaciones("Nuevo Pedido Test")
        .renglones(renglonesPedidoDTO)
        .idSucursal(1L)
        .idUsuario(2L)
        .idCliente(1L)
        .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_ENVIO)
        .build();
    PedidoDTO pedidoRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/pedidos",
        nuevoPedidoDTO,
        PedidoDTO.class);
    assertEquals("Rio Uruguay 15000 Corrientes Corrientes", pedidoRecuperado.getDetalleEnvio());
  }

  @Test
  void shouldCrearPedidoConUbicacionSucursal() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    this.shouldCrearSucursalResponsableInscripto();
    ConfiguracionSucursalDTO configuracionSucursalDTO =
      restTemplate.getForObject(apiPrefix + "/configuraciones-sucursal/2", ConfiguracionSucursalDTO.class);
    configuracionSucursalDTO.setPuntoDeRetiro(true);
    restTemplate.put(apiPrefix + "/configuraciones-sucursal", configuracionSucursalDTO);
    List<NuevoRenglonPedidoDTO> nuevosRenglonesPedido = new ArrayList<>();
    nuevosRenglonesPedido.add(
      NuevoRenglonPedidoDTO.builder()
        .idProductoItem(1L)
        .cantidad(new BigDecimal("5.000000000000000"))
        .build());
    nuevosRenglonesPedido.add(
      NuevoRenglonPedidoDTO.builder()
        .idProductoItem(2L)
        .cantidad(new BigDecimal("2.000000000000000"))
        .build());
    NuevoPedidoDTO nuevoPedidoDTO =
      NuevoPedidoDTO.builder()
        .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
        .recargoPorcentaje(new BigDecimal("5"))
        .observaciones("Nuevo Pedido Test")
        .renglones(nuevosRenglonesPedido)
        .idUsuario(2L)
        .idCliente(1L)
        .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
        .idSucursal(1L)
        .build();
    PedidoDTO pedidoRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/pedidos",
        nuevoPedidoDTO,
        PedidoDTO.class);
    assertEquals("Rio Piacentin 345 Corrientes Corrientes", pedidoRecuperado.getDetalleEnvio());
  }

  @Test
  void shouldNotCrearPedidoSinUbicacionSucursal() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    this.shouldCrearSucursalResponsableInscripto();
    ConfiguracionSucursalDTO configuracionSucursalDTO =
      restTemplate.getForObject(apiPrefix + "/configuraciones-sucursal/2", ConfiguracionSucursalDTO.class);
    configuracionSucursalDTO.setPuntoDeRetiro(true);
    restTemplate.put(apiPrefix + "/configuraciones-sucursal", configuracionSucursalDTO);
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
    NuevoPedidoDTO nuevoPedidoDTO =
      NuevoPedidoDTO.builder()
        .descuentoPorcentaje(new BigDecimal("15.000000000000000"))
        .recargoPorcentaje(new BigDecimal("5"))
        .observaciones("Nuevo Pedido Test")
        .renglones(renglonesPedidoDTO)
        .idUsuario(2L)
        .idCliente(1L)
        .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
        .build();
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate.postForObject(
                    apiPrefix + "/pedidos", nuevoPedidoDTO, PedidoDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_pedido_retiro_sucursal_no_seleccionada", null, Locale.getDefault())));
  }

  @Test
  void shouldFacturarPedido() {
    this.shouldCrearPedido();
    this.crearFacturaTipoADePedido();
    PedidoDTO pedidoRecuperado =
      restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    pedidoRecuperado =
      restTemplate.getForObject(
        apiPrefix + "/pedidos/" + pedidoRecuperado.getIdPedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    this.crearFacturaTipoBDePedido();
    pedidoRecuperado =
      restTemplate.getForObject(
        apiPrefix + "/pedidos/" + pedidoRecuperado.getIdPedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.CERRADO, pedidoRecuperado.getEstado());
  }

  @Test
  void shouldModificarPedido() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    List<NuevoRenglonPedidoDTO> nuevosRenglonesDePedido = new ArrayList<>();
    nuevosRenglonesDePedido.add(
      NuevoRenglonPedidoDTO.builder()
        .idProductoItem(1L)
        .cantidad(new BigDecimal("5"))
        .build());
    nuevosRenglonesDePedido.add(
      NuevoRenglonPedidoDTO.builder()
        .idProductoItem(2L)
        .cantidad(new BigDecimal("2"))
        .build());
    NuevoPedidoDTO nuevoPedidoDTO =
      NuevoPedidoDTO.builder()
        .descuentoPorcentaje(new BigDecimal("15"))
        .recargoPorcentaje(new BigDecimal("5"))
        .observaciones("Nuevo Pedido Test")
        .renglones(nuevosRenglonesDePedido)
        .idSucursal(1L)
        .idUsuario(1L)
        .idCliente(1L)
        .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_FACTURACION)
        .build();
    PedidoDTO pedidoRecuperado =
      restTemplate.postForObject(
        apiPrefix + "/pedidos",
        nuevoPedidoDTO,
        PedidoDTO.class);
    assertEquals(new BigDecimal("5947.200000000000000"), pedidoRecuperado.getTotalEstimado());
    assertEquals(nuevoPedidoDTO.getObservaciones(), pedidoRecuperado.getObservaciones());
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    nuevosRenglonesDePedido = new ArrayList<>();
    nuevosRenglonesDePedido.add(
      NuevoRenglonPedidoDTO.builder()
        .idProductoItem(3)
        .cantidad(new BigDecimal("7"))
        .build());
    List<RenglonPedidoDTO> renglonesPedido =
      Arrays.asList(
        restTemplate.postForObject(
          apiPrefix + "/pedidos/renglones/clientes/1", nuevosRenglonesDePedido, RenglonPedidoDTO[].class));
    pedidoRecuperado.setRenglones(renglonesPedido);
    pedidoRecuperado.setObservaciones("Cambiando las observaciones del pedido");
    restTemplate.put(apiPrefix + "/pedidos?idSucursal=1&idCliente=1&idUsuario=1&tipoDeEnvio=USAR_UBICACION_FACTURACION", pedidoRecuperado);
    PedidoDTO pedidoModificado =
      restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    assertEquals(pedidoRecuperado, pedidoModificado);
    assertEquals(new BigDecimal("6098.400000000000000"), pedidoModificado.getTotalEstimado());
    assertEquals("Cambiando las observaciones del pedido", pedidoModificado.getObservaciones());
    assertEquals(EstadoPedido.ABIERTO, pedidoModificado.getEstado());
  }

  @Test
  void shouldVerificarTransicionDeEstadosDeUnPedido() {
    this.shouldCrearPedido();
    this.crearFacturaTipoADePedido();
    PedidoDTO pedidoRecuperado =
        restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getIdPedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    this.crearFacturaTipoBDePedido();
    pedidoRecuperado =
        restTemplate.getForObject(
            apiPrefix + "/pedidos/" + pedidoRecuperado.getIdPedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.CERRADO, pedidoRecuperado.getEstado());
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder()
        .nroPedido(pedidoRecuperado.getNroPedido())
        .idSucursal(1L)
        .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    List<FacturaVentaDTO> facturasRecuperadas =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody()
            .getContent();
    restTemplate.delete(
      apiPrefix + "/facturas/" + facturasRecuperadas.get(0).getIdFactura());
    pedidoRecuperado =
      restTemplate.getForObject(
        apiPrefix + "/pedidos/" + pedidoRecuperado.getIdPedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ACTIVO, pedidoRecuperado.getEstado());
    restTemplate.delete(
      apiPrefix + "/facturas/" + facturasRecuperadas.get(1).getIdFactura());
    pedidoRecuperado =
      restTemplate.getForObject(
        apiPrefix + "/pedidos/" + pedidoRecuperado.getIdPedido(), PedidoDTO.class);
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
  }

  @Test
  void shloudVerificarCantidadDeArticulosEnPedido() {
    this.shouldCrearPedido();
    PedidoDTO pedidoDTO = restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    assertEquals(new BigDecimal("7.000000000000000"), pedidoDTO.getCantidadArticulos());
  }

  @Test
  void shouldVerificarTotalActualPedido() {
    this.shouldCrearPedido();
    PedidoDTO pedidoDTO = restTemplate.getForObject(apiPrefix + "/pedidos/1", PedidoDTO.class);
    assertEquals(new BigDecimal("7.000000000000000"), pedidoDTO.getCantidadArticulos());
    assertEquals(new BigDecimal("5947.200000000000000"), pedidoDTO.getTotalEstimado());
    ProductoDTO productoRecuperado = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoRecuperado.setPrecioCosto(new BigDecimal("110"));
    productoRecuperado.setGananciaPorcentaje(new BigDecimal("900"));
    productoRecuperado.setGananciaNeto(new BigDecimal("990"));
    productoRecuperado.setPrecioVentaPublico(new BigDecimal("1100"));
    productoRecuperado.setIvaNeto(new BigDecimal("231"));
    productoRecuperado.setPrecioLista(new BigDecimal("1331"));
    restTemplate.put(apiPrefix + "/productos", productoRecuperado);
    BusquedaPedidoCriteria criteriaPedido = new BusquedaPedidoCriteria();
    criteriaPedido.setIdSucursal(1L);
    HttpEntity<BusquedaPedidoCriteria> requestEntity = new HttpEntity<>(criteriaPedido);
    List<PedidoDTO> pedidos =
      restTemplate
        .exchange(
          apiPrefix + "/pedidos/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<PedidoDTO>>() {})
        .getBody()
        .getContent();
    assertTrue(!pedidos.isEmpty());
    assertEquals(new BigDecimal("6382.80000000000000000000000000000000000000000000000"), pedidos.get(0).getTotalActual());
  }

  @Test
  void shouldComprobarSaldoCuentaCorrienteProveedor() {
    this.abrirCaja();
    this.shouldCrearFacturaBCompraSucursal1();
    assertEquals(
      new BigDecimal("-561.680000000000000"),
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class));
    this.crearReciboParaProveedor(599.25, 1L, 1L);
    assertEquals(
      37.57,
      restTemplate
        .getForObject(apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
        .doubleValue());
    restTemplate.delete(apiPrefix + "/recibos/1");
    assertEquals(
      new BigDecimal("-561.680000000000000"),
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class));
    this.crearReciboParaProveedor(499.25, 1L, 1L);
    assertEquals(
      new BigDecimal("-62.430000000000000"),
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class));
    this.crearReciboParaProveedor(200, 1L, 1L);
    assertEquals(
      new BigDecimal("137.570000000000000"),
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class));
  }

  @Test
  void shouldComprobarSaldoParcialCuentaCorrienteProveedor() {
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    sucursal.setCategoriaIVA(CategoriaIVA.MONOTRIBUTO);
    restTemplate.put(apiPrefix + "/sucursales", sucursal);
    this.abrirCaja();
    this.shouldCrearFacturaBCompraSucursal1();
    this.crearReciboParaProveedor(599.25, 1L, 1L);
    restTemplate.delete(apiPrefix + "/recibos/1");
    this.crearReciboParaProveedor(499.25, 1L, 1L);
    this.crearReciboParaProveedor(200, 1L, 1L);
    this.shouldCrearFacturaBCompraSucursal1();
    this.crearNotaCreditoParaProveedor();
    List<RenglonCuentaCorriente> renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/2/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertEquals(-12.71, renglonesCuentaCorriente.get(0).getSaldo().doubleValue());
    assertEquals(-424.11, renglonesCuentaCorriente.get(1).getSaldo().doubleValue());
    assertEquals(137.57, renglonesCuentaCorriente.get(2).getSaldo().doubleValue());
    assertEquals(-62.43, renglonesCuentaCorriente.get(3).getSaldo().doubleValue());
    assertEquals(-561.68, renglonesCuentaCorriente.get(4).getSaldo().doubleValue());
    this.crearNotaDebitoParaProveedor();
    renglonesCuentaCorriente =
        restTemplate
            .exchange(
                apiPrefix + "/cuentas-corriente/2/renglones" + "?pagina=" + 0 + "&tamanio=" + 50,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginaRespuestaRest<RenglonCuentaCorriente>>() {})
            .getBody()
            .getContent();
    assertEquals(-333.71, renglonesCuentaCorriente.get(0).getSaldo().doubleValue());
    assertEquals(-12.71, renglonesCuentaCorriente.get(1).getSaldo().doubleValue());
    assertEquals(-424.11, renglonesCuentaCorriente.get(2).getSaldo().doubleValue());
    assertEquals(137.57, renglonesCuentaCorriente.get(3).getSaldo().doubleValue());
    assertEquals(-62.43, renglonesCuentaCorriente.get(4).getSaldo().doubleValue());
    assertEquals(-561.68, renglonesCuentaCorriente.get(5).getSaldo().doubleValue());
  }

  @Test
  void shouldCrearUsuario() {
    UsuarioDTO nuevoUsuario =
      UsuarioDTO.builder()
        .username("wicca")
        .password("Salem123")
        .nombre("Sabrina")
        .apellido("Spellman")
        .email("Witch@gmail.com")
        .roles(Collections.singletonList(Rol.ENCARGADO))
        .habilitado(true)
        .build();
    restTemplate.postForObject(apiPrefix + "/usuarios", nuevoUsuario, UsuarioDTO.class);
    UsuarioDTO usuarioRecuperado =
      restTemplate.getForObject(apiPrefix + "/usuarios/3", UsuarioDTO.class);
    assertEquals(nuevoUsuario, usuarioRecuperado);
  }

  @Test
  void shouldModificarUsuario() {
    this.shouldCrearUsuario();
    UsuarioDTO usuarioRecuperado =
      restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    usuarioRecuperado.setUsername("darkmagic");
    Rol[] roles = new Rol[]{Rol.ADMINISTRADOR, Rol.ENCARGADO};
    usuarioRecuperado.setRoles(Arrays.asList(roles));
    restTemplate.put(apiPrefix + "/usuarios", usuarioRecuperado);
    UsuarioDTO usuarioModificado =
      restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    assertEquals(usuarioRecuperado, usuarioModificado);
  }

  @Test
  void shouldValidarPermisosUsuarioAlEliminarProveedor() {
    UsuarioDTO nuevoUsuario =
        UsuarioDTO.builder()
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
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.delete(apiPrefix + "/proveedores/1"));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_usuario_rol_no_valido", null, Locale.getDefault())));
  }

  @Test
  void shouldActualizarFechaUltimaModificacionCuentaCorrienteCliente() {
    this.abrirCaja();
    shouldCrearFacturaVentaB();
    CuentaCorriente ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/clientes/1", CuentaCorriente.class);
    FacturaVentaDTO facturaVentaDTO =
      restTemplate.getForObject(apiPrefix + "/facturas/1", FacturaVentaDTO.class);
    assertEquals(facturaVentaDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    this.crearReciboParaCliente(5992.5, 1L, 1L);
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/clientes/1", CuentaCorriente.class);
    ReciboDTO reciboDTO = restTemplate.getForObject(apiPrefix + "/recibos/1", ReciboDTO.class);
    assertEquals(reciboDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    this.crearNotaDebitoParaCliente();
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/clientes/1", CuentaCorriente.class);
    NotaDebitoDTO notaDebitoDTO =
      restTemplate.getForObject(apiPrefix + "/notas/1", NotaDebitoDTO.class);
    assertEquals(notaDebitoDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    this.crearReciboParaCliente(6113.5, 1L, 1L);
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/clientes/1", CuentaCorriente.class);
    reciboDTO = restTemplate.getForObject(apiPrefix + "/recibos/2", ReciboDTO.class);
    assertEquals(reciboDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    this.crearNotaCreditoParaCliente();
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/clientes/1", CuentaCorriente.class);
    NotaCreditoDTO notaCreditoDTO =
      restTemplate.getForObject(apiPrefix + "/notas/2", NotaCreditoDTO.class);
    assertEquals(notaCreditoDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    restTemplate.delete(apiPrefix + "/notas/2");
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/clientes/1", CuentaCorriente.class);
    assertEquals(reciboDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
  }

  @Test
  void shouldActualizarFechaUltimaModificacionCuentaCorrienteProveedor() {
    this.abrirCaja();
    shouldCrearFacturaBCompraSucursal1();
    CuentaCorriente ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1", CuentaCorriente.class);
    FacturaCompraDTO facturaCompraDTO =
      restTemplate.getForObject(apiPrefix + "/facturas/1", FacturaCompraDTO.class);
    assertEquals(facturaCompraDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    this.crearReciboParaProveedor(599.25, 1L, 1L);
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1", CuentaCorriente.class);
    ReciboDTO reciboDTO = restTemplate.getForObject(apiPrefix + "/recibos/1", ReciboDTO.class);
    assertEquals(reciboDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    restTemplate.delete(apiPrefix + "/recibos/1");
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1", CuentaCorriente.class);
    assertEquals(facturaCompraDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    this.crearReciboParaProveedor(499.25, 1L, 1L);
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1", CuentaCorriente.class);
    reciboDTO = restTemplate.getForObject(apiPrefix + "/recibos/2", ReciboDTO.class);
    assertEquals(reciboDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
    this.crearReciboParaProveedor(200, 1L, 1L);
    ccCliente =
      restTemplate.getForObject(
        apiPrefix + "/cuentas-corriente/proveedores/1", CuentaCorriente.class);
    reciboDTO = restTemplate.getForObject(apiPrefix + "/recibos/3", ReciboDTO.class);
    assertEquals(reciboDTO.getFecha(), ccCliente.getFechaUltimoMovimiento());
  }

  @Test
  void shouldGetMultiplesProductosPorIdEnOrden() {
    this.shouldCrearPedido();
    List<Long> idsProductos = new ArrayList<>();
    idsProductos.add(1L);
    idsProductos.add(2L);
    List<Producto> productos = productoService.getMultiplesProductosPorId(idsProductos);
    List<RenglonPedido> renglones = pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(1L);
    assertEquals(productos.get(0).getIdProducto().longValue(), renglones.get(0).getIdProductoItem());
    assertEquals(productos.get(1).getIdProducto().longValue(), renglones.get(1).getIdProductoItem());
  }

  @Test
  void shouldVerificarTotalizadoresVenta() {
    BusquedaFacturaVentaCriteria criteria =
      BusquedaFacturaVentaCriteria.builder().idSucursal(1L).build();
    BigDecimal totalFacturadoVenta =
      restTemplate.postForObject(
        apiPrefix + "/facturas/total-facturado-venta/criteria", criteria, BigDecimal.class);
    BigDecimal totalIvaVenta =
      restTemplate.postForObject(
        apiPrefix + "/facturas/total-iva-venta/criteria", criteria, BigDecimal.class);
    BigDecimal gananciaTotal =
      restTemplate.postForObject(
        apiPrefix + "/facturas/ganancia-total/criteria", criteria, BigDecimal.class);
    assertEquals(BigDecimal.ZERO, totalFacturadoVenta);
    assertEquals(BigDecimal.ZERO, totalIvaVenta);
    assertEquals(BigDecimal.ZERO, gananciaTotal);
    this.shouldCrearFacturaVentaASucursal1();
    totalFacturadoVenta =
      restTemplate.postForObject(
        apiPrefix + "/facturas/total-facturado-venta/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("7191.000000000000000"), totalFacturadoVenta);
    totalIvaVenta =
      restTemplate.postForObject(
        apiPrefix + "/facturas/total-iva-venta/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("1071.000000000000000"), totalIvaVenta);
    gananciaTotal =
      restTemplate.postForObject(
        apiPrefix + "/facturas/ganancia-total/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("8100.000000000000000000000000000000"), gananciaTotal);
    ProductoDTO producto1 =
      restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    Set<CantidadEnSucursalDTO> cantidadEnSucursal = producto1.getCantidadEnSucursales();
    cantidadEnSucursal.stream()
        .filter(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.getIdSucursal() == 1L)
        .forEach(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.setCantidad(BigDecimal.TEN));
    producto1.setCantidadEnSucursales(cantidadEnSucursal);
    restTemplate.put(apiPrefix + "/productos?idSucursal=1", producto1);
    ProductoDTO producto2 =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    cantidadEnSucursal = producto2.getCantidadEnSucursales();
    cantidadEnSucursal.stream()
        .filter(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.getIdSucursal() == 1L)
        .forEach(cantidadEnSucursalDTO -> cantidadEnSucursalDTO.setCantidad(new BigDecimal("6")));
    producto2.setCantidadEnSucursales(cantidadEnSucursal);
    restTemplate.put(apiPrefix + "/productos?idSucursal=1", producto2);
    this.shouldCrearFacturaVentaPresupuesto();
    totalFacturadoVenta =
      restTemplate.postForObject(
        apiPrefix + "/facturas/total-facturado-venta/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("12807.800000000000000"), totalFacturadoVenta);
    totalIvaVenta =
      restTemplate.postForObject(
        apiPrefix + "/facturas/total-iva-venta/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("1071.000000000000000"), totalIvaVenta);
    gananciaTotal =
      restTemplate.postForObject(
        apiPrefix + "/facturas/ganancia-total/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("14400.000000000000000000000000000000"), gananciaTotal);
  }

  @Test
  void shouldVerificarTotalizadoresCompra() {
    BusquedaFacturaCompraCriteria criteria =
        BusquedaFacturaCompraCriteria.builder().idSucursal(1L).build();
    BigDecimal totalFacturadoCompra =
        restTemplate.postForObject(
            apiPrefix + "/facturas/total-facturado-compra/criteria", criteria, BigDecimal.class);
    BigDecimal totalIvaCompra =
        restTemplate.postForObject(
            apiPrefix + "/facturas/total-iva-compra/criteria", criteria, BigDecimal.class);
    assertEquals(BigDecimal.ZERO, totalFacturadoCompra);
    assertEquals(BigDecimal.ZERO, totalIvaCompra);
    this.shouldCrearFacturaCompraASucursal1();
    totalFacturadoCompra =
        restTemplate.postForObject(
            apiPrefix + "/facturas/total-facturado-compra/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("554.540000000000000"), totalFacturadoCompra);
    totalIvaCompra =
        restTemplate.postForObject(
            apiPrefix + "/facturas/total-iva-compra/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("78.540000000000000"), totalIvaCompra);
    this.shouldCrearFacturaCompraPresupuesto();
    totalFacturadoCompra =
        restTemplate.postForObject(
            apiPrefix + "/facturas/total-facturado-compra/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("1116.220000000000000"), totalFacturadoCompra);
    totalIvaCompra =
        restTemplate.postForObject(
            apiPrefix + "/facturas/total-iva-compra/criteria", criteria, BigDecimal.class);
    assertEquals(new BigDecimal("78.540000000000000"), totalIvaCompra);
  }

  @Test
  void shouldVerificarSaldoCaja() {
    CajaDTO caja =
      restTemplate.postForObject(
        apiPrefix + "/cajas/apertura/sucursales/1?saldoApertura=200",
        null,
        CajaDTO.class);
    assertEquals(new BigDecimal("200"), caja.getSaldoApertura());
    this.crearReciboParaCliente(300, 1L, 1L);
    assertEquals(
      new BigDecimal("500.000000000000000"),
      restTemplate.getForObject(apiPrefix + "/cajas/1/saldo-sistema", BigDecimal.class));
    this.crearReciboParaProveedor(500, 1L, 1L);
    assertEquals(
      new BigDecimal("0E-15"),
      restTemplate.getForObject(apiPrefix + "/cajas/1/saldo-sistema", BigDecimal.class));
    GastoDTO gasto =
      GastoDTO.builder()
        .concepto("Gasto test")
        .fecha(LocalDateTime.now())
        .monto(new BigDecimal("200"))
        .build();
    restTemplate.postForObject(
      apiPrefix + "/gastos?idSucursal=1&idFormaDePago=1", gasto, GastoDTO.class);
    assertEquals(
      new BigDecimal("-200.000000000000000"),
      restTemplate.getForObject(apiPrefix + "/cajas/1/saldo-sistema", BigDecimal.class));
  }

  @Test
  void shouldCrearGasto() {
    this.abrirCaja();
    GastoDTO gasto =
      GastoDTO.builder()
        .concepto("Gasto test")
        .fecha(LocalDateTime.now())
        .monto(new BigDecimal("200.000000000000000"))
        .build();
    restTemplate.postForObject(
      apiPrefix + "/gastos?idSucursal=1&idFormaDePago=1", gasto, GastoDTO.class);
    assertEquals(
      new BigDecimal("-200.000000000000000"),
      restTemplate.getForObject(apiPrefix + "/cajas/1/saldo-sistema", BigDecimal.class));
    GastoDTO gastoRecuperado =  restTemplate.getForObject(apiPrefix + "/gastos/1", GastoDTO.class);
    assertEquals(gasto, gastoRecuperado);
  }

  @Test
  void shouldEliminarGasto() {
    this.shouldCrearGasto();
    restTemplate.delete(apiPrefix + "/gastos/1");
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.getForObject(apiPrefix + "/gastos/1", GastoDTO.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage("mensaje_gasto_no_existente", null, Locale.getDefault())));
  }

  @Test
  void shouldRecuperarGastoPorSucursal() {
    this.shouldCrearGasto();
    BusquedaGastoCriteria criteria = BusquedaGastoCriteria.builder().idSucursal(1L).build();
    HttpEntity<BusquedaGastoCriteria> requestEntity = new HttpEntity<>(criteria);
    List<GastoDTO> gastos =
        restTemplate
            .exchange(
                apiPrefix + "/gastos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<GastoDTO>>() {})
            .getBody()
            .getContent();
    GastoDTO gastoRecuperado = restTemplate.getForObject(apiPrefix + "/gastos/1", GastoDTO.class);
    assertEquals(gastoRecuperado, gastos.get(0));
  }

  @Test
  void shouldModificarCostoEnvioDeLocalidad() {
    LocalidadDTO localidad = restTemplate.getForObject(apiPrefix + "/ubicaciones/localidades/1", LocalidadDTO.class);
    localidad.setEnvioGratuito(true);
    localidad.setCostoEnvio(new BigDecimal("450"));
    restTemplate.put(apiPrefix + "/ubicaciones/localidades", localidad);
    assertTrue(localidad.isEnvioGratuito());
    assertEquals(new BigDecimal("450"), localidad.getCostoEnvio());
  }

  @Test
  void shouldVerificarFechaCierreCajaSinScheduling() {
    restTemplate.postForObject(apiPrefix + "/cajas/apertura/sucursales/1?saldoApertura=200", null, CajaDTO.class);
    clockService.cambiarFechaHora(2030, 9, 24, 23, 59, 59);
    restTemplate.put(apiPrefix + "/cajas/1/cierre?monto=300", CajaDTO.class);
    CajaDTO cajaRecuperada = restTemplate.getForObject(apiPrefix + "/cajas/1", CajaDTO.class);
    LocalDateTime fechaCierre = cajaRecuperada.getFechaCierre();
    assertEquals(2030, fechaCierre.getYear());
    assertEquals(9, fechaCierre.getMonthValue());
    assertEquals(24, fechaCierre.getDayOfMonth());
  }

  @Test
  void shouldVerificarFechaCierreCajaConScheduling() {
    clockService.cambiarFechaHora(2019, 12, 31, 10, 15, 35);
    restTemplate.postForObject(apiPrefix + "/cajas/apertura/sucursales/1?saldoApertura=200", null, CajaDTO.class);
    cajaService.cerrarCaja(1L, new BigDecimal("300"), 1L, true);
    CajaDTO cajaRecuperada = restTemplate.getForObject(apiPrefix + "/cajas/1", CajaDTO.class);
    LocalDateTime fechaCierre = cajaRecuperada.getFechaCierre();
    assertEquals(2019, fechaCierre.getYear());
    assertEquals(12, fechaCierre.getMonthValue());
    assertEquals(31, fechaCierre.getDayOfMonth());
    assertEquals(23, fechaCierre.getHour());
    assertEquals(59, fechaCierre.getMinute());
    assertEquals(59, fechaCierre.getSecond());
  }

  @Test
  void shouldAgregarItemsAlCarritoCompra() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    restTemplate.postForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/1?cantidad=5", null, ItemCarritoCompra.class);
    restTemplate.postForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/2?cantidad=9", null, ItemCarritoCompra.class);
    ItemCarritoCompra item1 = restTemplate.getForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/1", ItemCarritoCompra.class);
    assertEquals(1L, item1.getProducto().getIdProducto().longValue());
    assertEquals(5, item1.getCantidad().doubleValue());
    assertTrue(item1.getProducto().isHayStock());
    ItemCarritoCompra item2 = restTemplate.getForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/2", ItemCarritoCompra.class);
    assertEquals(2L, item2.getProducto().getIdProducto().longValue());
    assertEquals(9, item2.getCantidad().doubleValue());
    assertTrue(item2.getProducto().isHayStock());
  }

  @Test
  void shouldDevolverNullCuandoElItemNoExisteEnElCarrito() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    ItemCarritoCompra item1 = restTemplate.getForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/1", ItemCarritoCompra.class);
    assertNull(item1);
  }

  @Test
  void shouldModificarCantidadesDeUnItemDelCarrito() {
    this.crearDosProductos(RandomStringUtils.random(10, false, true), RandomStringUtils.random(10, false, true));
    this.shouldAgregarItemsAlCarritoCompra();
    restTemplate.postForObject(
        apiPrefix + "/carrito-compra/usuarios/1/productos/1?cantidad=2",
        null,
        ItemCarritoCompra.class);
    ItemCarritoCompra item1 =
        restTemplate.getForObject(
            apiPrefix + "/carrito-compra/usuarios/1/productos/1", ItemCarritoCompra.class);
    assertEquals(2, item1.getCantidad().doubleValue());
    restTemplate.postForObject(
        apiPrefix + "/carrito-compra/usuarios/1/productos/2?cantidad=-3",
        null,
        ItemCarritoCompra.class);
    ItemCarritoCompra item2 =
        restTemplate.getForObject(
            apiPrefix + "/carrito-compra/usuarios/1/productos/2", ItemCarritoCompra.class);
    assertEquals(new BigDecimal("0E-15"), item2.getCantidad());
  }

  @Test
  void shouldGenerarPedidoRetiroEnSucursalConItemsDelCarrito() {
    this.shouldAgregarItemsAlCarritoCompra();
    ConfiguracionSucursalDTO configuracionSucursalDTO =
      restTemplate.getForObject(apiPrefix + "/configuraciones-sucursal/1", ConfiguracionSucursalDTO.class);
    configuracionSucursalDTO.setPuntoDeRetiro(true);
    restTemplate.put(apiPrefix + "/configuraciones-sucursal", configuracionSucursalDTO);
    NuevaOrdenDeCompraDTO nuevaOrdenDeCompraDTO = NuevaOrdenDeCompraDTO.builder()
      .idSucursal(1L)
      .idCliente(1L)
      .idUsuario(1L)
      .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
      .observaciones("probando pedido desde carrito, sin pago")
      .build();
    PedidoDTO pedido =
        restTemplate.postForObject(
            apiPrefix + "/carrito-compra", nuevaOrdenDeCompraDTO, PedidoDTO.class);
    assertEquals(14, pedido.getCantidadArticulos().doubleValue());
    assertEquals(new BigDecimal("10236.80000000000000000000000000000000000000000000000"), pedido.getTotalActual());
  }

  @Test
  void shouldGenerarPedidoEnvioDomicilioConItemsDelCarrito() {
    this.shouldAgregarItemsAlCarritoCompra();
    ConfiguracionSucursalDTO configuracionSucursalDTO =
      restTemplate.getForObject(apiPrefix + "/configuraciones-sucursal/1", ConfiguracionSucursalDTO.class);
    configuracionSucursalDTO.setPuntoDeRetiro(true);
    restTemplate.put(apiPrefix + "/configuraciones-sucursal", configuracionSucursalDTO);
    NuevaOrdenDeCompraDTO nuevaOrdenDeCompraDTO = NuevaOrdenDeCompraDTO.builder()
      .idCliente(1L)
      .idUsuario(1L)
      .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_ENVIO)
      .observaciones("probando pedido desde carrito, sin pago")
      .build();
    PedidoDTO pedido =
      restTemplate.postForObject(
        apiPrefix + "/carrito-compra", nuevaOrdenDeCompraDTO, PedidoDTO.class);
    assertEquals(14, pedido.getCantidadArticulos().doubleValue());
    assertEquals(new BigDecimal("10236.80000000000000000000000000000000000000000000000"), pedido.getTotalActual());
  }

  @Test
  void shouldActualizarConfiguracionSucursal() {
    ConfiguracionSucursalDTO configuracionSucursalDTO =
      restTemplate.getForObject(apiPrefix + "/configuraciones-sucursal/1", ConfiguracionSucursalDTO.class);
    configuracionSucursalDTO.setPuntoDeRetiro(true);
    restTemplate.put(apiPrefix + "/configuraciones-sucursal", configuracionSucursalDTO);
    configuracionSucursalDTO = restTemplate.getForObject(apiPrefix + "/configuraciones-sucursal/1", ConfiguracionSucursalDTO.class);
    assertTrue(configuracionSucursalDTO.isPuntoDeRetiro());
  }
  
  @Test
  void crearProductoEnOfertaAndAgregarloAlCarrito() {
    NuevoProductoDTO productoUno =
      NuevoProductoDTO.builder()
        .codigo(RandomStringUtils.random(10, false, true))
        .descripcion(RandomStringUtils.random(10, true, false))
        .cantidadEnSucursal(new HashMap<Long, BigDecimal>() {{put(1L, BigDecimal.TEN);}})
        .bulto(BigDecimal.TEN)
        .precioCosto(CIEN)
        .gananciaPorcentaje(new BigDecimal("900"))
        .gananciaNeto(new BigDecimal("900"))
        .precioVentaPublico(new BigDecimal("1000"))
        .ivaPorcentaje(new BigDecimal("21.0"))
        .ivaNeto(new BigDecimal("210"))
        .precioLista(new BigDecimal("1210"))
        .nota("ProductoTest1")
        .publico(true)
        .build();
    SucursalDTO sucursal = restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    RubroDTO rubro = restTemplate.getForObject(apiPrefix + "/rubros/1", RubroDTO.class);
    ProveedorDTO proveedor =
      restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    Medida medida = restTemplate.getForObject(apiPrefix + "/medidas/1", Medida.class);
    restTemplate.postForObject(
      apiPrefix
        + "/productos?idMedida="
        + medida.getIdMedida()
        + "&idRubro="
        + rubro.getIdRubro()
        + "&idProveedor="
        + proveedor.getIdProveedor()
        + "&idSucursal="
        + sucursal.getIdSucursal(),
      productoUno,
      ProductoDTO.class);
    ProductoDTO productoRecuperado = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoRecuperado.setPublico(true);
    productoRecuperado.setOferta(true);
    productoRecuperado.setUrlImagen("imagen.com");
    productoRecuperado.setPorcentajeBonificacionOferta(BigDecimal.TEN);
    restTemplate.put(apiPrefix + "/productos", productoRecuperado);
    productoRecuperado = restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    assertTrue(productoRecuperado.isOferta());
    assertEquals(new BigDecimal("10.000000000000000"), productoRecuperado.getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("1089.00000000000000000"), productoRecuperado.getPrecioListaBonificado());
    restTemplate.postForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/1?cantidad=5", null, ItemCarritoCompra.class);
    ItemCarritoCompra item1 = restTemplate.getForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/1", ItemCarritoCompra.class);
    assertEquals(new BigDecimal("6050.00"), item1.getImporte());
    assertNull(item1.getImporteBonificado());
    assertEquals(new BigDecimal("10.000000000000000"), item1.getProducto().getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("1089.00"), item1.getProducto().getPrecioListaBonificado());
    restTemplate.postForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/1?cantidad=10", null, ItemCarritoCompra.class);
    item1 = restTemplate.getForObject(apiPrefix + "/carrito-compra/usuarios/1/productos/1", ItemCarritoCompra.class);
    assertEquals(new BigDecimal("12100.00"), item1.getImporte());
    assertEquals(new BigDecimal("10890.00"), item1.getImporteBonificado());
    assertEquals(new BigDecimal("10.000000000000000"), item1.getProducto().getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("1089.00"), item1.getProducto().getPrecioListaBonificado());
  }

  @Test
  void buscarProductosAndVerificarPrecios() {
    this.crearDosProductos(
        "A " + RandomStringUtils.random(10, false, true),
        "B" + RandomStringUtils.random(10, false, true));
    BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder().pagina(0).build();
    HttpEntity<BusquedaProductoCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<ProductoDTO> paginaRespuestaRest =
        restTemplate
            .exchange(
                apiPrefix + "/productos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
            .getBody();
    assertNotNull(paginaRespuestaRest);
    List<ProductoDTO> productosRecuperados = paginaRespuestaRest.getContent();
    assertNull(productosRecuperados.get(0).getPrecioListaBonificado());
    assertNull(productosRecuperados.get(1).getPrecioListaBonificado());
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/2", ClienteDTO.class);
    cliente.setBonificacion(BigDecimal.TEN);
    restTemplate.put(apiPrefix + "/clientes", cliente);
    cliente = restTemplate.getForObject(apiPrefix + "/clientes/2", ClienteDTO.class);
    assertEquals(new BigDecimal("10.000000000000000"), cliente.getBonificacion());
    paginaRespuestaRest =
        restTemplate
            .exchange(
                apiPrefix + "/productos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
            .getBody();
    assertNotNull(paginaRespuestaRest);
    productosRecuperados = paginaRespuestaRest.getContent();
    assertEquals(
        new BigDecimal("1089.000000000000000000000000000000"),
        productosRecuperados.get(0).getPrecioListaBonificado());
    assertEquals(
        new BigDecimal("994.500000000000000000000000000000"),
        productosRecuperados.get(1).getPrecioListaBonificado());
    ProductoDTO productoUno =
        restTemplate.getForObject(apiPrefix + "/productos/1", ProductoDTO.class);
    productoUno.setPublico(true);
    productoUno.setOferta(true);
    productoUno.setUrlImagen("imagen.com");
    productoUno.setPorcentajeBonificacionOferta(BigDecimal.TEN);
    restTemplate.put(apiPrefix + "/productos", productoUno);
    ProductoDTO productoDos =
        restTemplate.getForObject(apiPrefix + "/productos/2", ProductoDTO.class);
    productoDos.setPublico(true);
    productoDos.setOferta(true);
    productoDos.setUrlImagen("imagen2.com");
    productoDos.setPorcentajeBonificacionOferta(new BigDecimal("50"));
    restTemplate.put(apiPrefix + "/productos", productoDos);
    paginaRespuestaRest =
        restTemplate
            .exchange(
                apiPrefix + "/productos/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
            .getBody();
    productosRecuperados = paginaRespuestaRest.getContent();
    assertEquals(
        new BigDecimal("1089.00000000000000000"),
        productosRecuperados.get(0).getPrecioListaBonificado());
    assertEquals(
        new BigDecimal("552.50000000000000000"),
        productosRecuperados.get(1).getPrecioListaBonificado());
  }

  @Test
  void shouldCrearReporteListaPrecios() {
    this.crearDosProductos("1er Producto", "2do Producto");
    BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder().pagina(0).build();
    HttpEntity<BusquedaProductoCriteria> requestEntity = new HttpEntity<>(criteria);
    restTemplate
        .exchange(
            apiPrefix + "/productos/reporte/criteria", HttpMethod.POST, requestEntity, byte[].class)
        .getBody();
    restTemplate
        .exchange(
            apiPrefix + "/productos/reporte/criteria?formato=xlsx",
            HttpMethod.POST,
            requestEntity,
            byte[].class)
        .getBody();
    restTemplate
        .exchange(
            apiPrefix + "/productos/reporte/criteria?formato=pdf",
            HttpMethod.POST,
            requestEntity,
            byte[].class)
        .getBody();
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () ->
                restTemplate
                    .exchange(
                        apiPrefix + "/productos/reporte/criteria?formato=formatoNoValido",
                        HttpMethod.POST,
                        requestEntity,
                        byte[].class)
                    .getBody());
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage("mensaje_formato_no_valido", null, Locale.getDefault())));
  }

  @Test
  void shouldThrowsExceptionWhenUsuarioLoginInvalido() {
    Credencial credencial = new Credencial();
    credencial.setUsername("falsoLogin");
    credencial.setPassword("passFalso");
    RestClientResponseException thrown =
        assertThrows(
            RestClientResponseException.class,
            () -> restTemplate.postForObject(apiPrefix + "/login", credencial, String.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSource.getMessage(
                    "mensaje_usuario_logInInvalido", null, Locale.getDefault())));
  }

  @Test
  void shouldThrowsExceptionWhenUsuarioNoHabilitado() {
    UsuarioDTO usuario = restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    usuario.setPassword("passwordTest");
    restTemplate.put(apiPrefix + "/usuarios", usuario);
    Credencial credencial = new Credencial();
    credencial.setUsername("marce");
    credencial.setPassword("passwordTest");
    RestClientResponseException thrown =
      assertThrows(
        RestClientResponseException.class,
        () -> restTemplate.postForObject(apiPrefix + "/login", credencial, String.class));
    assertNotNull(thrown.getMessage());
    assertTrue(
      thrown
        .getMessage()
        .contains(
          messageSource.getMessage(
            "mensaje_usuario_no_habilitado", null, Locale.getDefault())));
  }

  @Test
  void shouldCrearProductosEnTresSucursales(){
    this.crearDosProductos("A", "B");
    BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder().pagina(0).build();
    HttpEntity<BusquedaProductoCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<ProductoDTO> paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/productos/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    List<ProductoDTO> productosRecuperados = paginaRespuestaRest.getContent();
    assertEquals(new BigDecimal("10.000000000000000"), productosRecuperados.get(0).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("6.000000000000000"), productosRecuperados.get(1).getCantidadTotalEnSucursales());
    assertEquals(1, productosRecuperados.get(0).getCantidadEnSucursales().size());
    assertEquals(1, productosRecuperados.get(1).getCantidadEnSucursales().size());
    CantidadEnSucursalDTO cantidadEnSucursalDTO = CantidadEnSucursalDTO.builder().idSucursal(1L).build();
    assertTrue(productosRecuperados.get(0).getCantidadEnSucursales().contains(cantidadEnSucursalDTO));
    assertTrue(productosRecuperados.get(1).getCantidadEnSucursales().contains(cantidadEnSucursalDTO));
    this.shouldCrearSucursalResponsableInscripto();
    paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/productos/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    productosRecuperados = paginaRespuestaRest.getContent();
    ProductoDTO productoParaModificar = productosRecuperados.get(0);
    Set<CantidadEnSucursalDTO> cantidadEnSucursal = productoParaModificar.getCantidadEnSucursales();
    cantidadEnSucursal.remove(CantidadEnSucursalDTO.builder().idSucursal(1L).build());
    cantidadEnSucursal.stream().filter(cantidad -> cantidad.getIdSucursal() == 2L).forEach(cantidad -> cantidad.setCantidad(BigDecimal.TEN));
    productoParaModificar.setCantidadEnSucursales(cantidadEnSucursal);
    restTemplate.put(apiPrefix + "/productos", productoParaModificar);
    paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/productos/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    productosRecuperados = paginaRespuestaRest.getContent();
    assertEquals(new BigDecimal("20.000000000000000"), productosRecuperados.get(0).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("6.000000000000000"), productosRecuperados.get(1).getCantidadTotalEnSucursales());
    assertEquals(2, productosRecuperados.get(0).getCantidadEnSucursales().size());
    assertEquals(2, productosRecuperados.get(1).getCantidadEnSucursales().size());
    cantidadEnSucursalDTO = CantidadEnSucursalDTO.builder().idSucursal(2L).build();
    assertTrue(productosRecuperados.get(0).getCantidadEnSucursales().contains(cantidadEnSucursalDTO));
    assertTrue(productosRecuperados.get(1).getCantidadEnSucursales().contains(cantidadEnSucursalDTO));
    this.shouldCrearSucursalMonotributista();
    paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/productos/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    productosRecuperados = paginaRespuestaRest.getContent();
    productoParaModificar = productosRecuperados.get(0);
    cantidadEnSucursal = productoParaModificar.getCantidadEnSucursales();
    cantidadEnSucursal.remove(CantidadEnSucursalDTO.builder().idSucursal(1L).build());
    cantidadEnSucursal.remove(CantidadEnSucursalDTO.builder().idSucursal(2L).build());
    cantidadEnSucursal.stream().filter(cantidad -> cantidad.getIdSucursal() == 3L).forEach(cantidad -> cantidad.setCantidad(BigDecimal.ONE));
    productoParaModificar.setCantidadEnSucursales(cantidadEnSucursal);
    restTemplate.put(apiPrefix + "/productos", productoParaModificar);
    paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/productos/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<ProductoDTO>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    productosRecuperados = paginaRespuestaRest.getContent();
    assertEquals(new BigDecimal("21.000000000000000"), productosRecuperados.get(0).getCantidadTotalEnSucursales());
    assertEquals(new BigDecimal("6.000000000000000"), productosRecuperados.get(1).getCantidadTotalEnSucursales());
    assertEquals(3, productosRecuperados.get(0).getCantidadEnSucursales().size());
    assertEquals(3, productosRecuperados.get(1).getCantidadEnSucursales().size());
    cantidadEnSucursalDTO = CantidadEnSucursalDTO.builder().idSucursal(3L).build();
    assertTrue(productosRecuperados.get(0).getCantidadEnSucursales().contains(cantidadEnSucursalDTO));
    assertTrue(productosRecuperados.get(1).getCantidadEnSucursales().contains(cantidadEnSucursalDTO));
  }

  @Test
  void shouldBuscarUsuarios(){
    BusquedaUsuarioCriteria criteria = BusquedaUsuarioCriteria.builder()
      .apellido("test")
      .email("test@test.com")
      .nombre("Usuario Test")
      .ordenarPor("nombre")
      .roles(Collections.singletonList(Rol.ADMINISTRADOR))
      .sentido("ASC")
      .username("test")
      .pagina(0).build();
    HttpEntity<BusquedaUsuarioCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<UsuarioDTO> paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/usuarios/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<UsuarioDTO>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    assertEquals(1, paginaRespuestaRest.getContent().size());
    assertEquals("Usuario Test", paginaRespuestaRest.getContent().get(0).getNombre());
  }

  @Test
  void shouldBuscarCaja() {
    BusquedaCajaCriteria criteria = BusquedaCajaCriteria.builder()
      .fechaDesde(LocalDateTime.now())
      .fechaHasta(LocalDateTime.now())
      .idSucursal(1L)
      .idUsuarioApertura(1L)
      .ordenarPor("fechaApertura")
      .sentido("ASC")
      .pagina(0).build();
    this.abrirCaja();
    HttpEntity<BusquedaCajaCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<CajaDTO> paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/cajas/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<CajaDTO>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    assertEquals(1, paginaRespuestaRest.getContent().size());
  }

  @Test
  void shouldBuscarCuentasCorrientes() {
    this.shouldCrearClienteResponsableInscripto();
    this.shouldCrearFacturaVentaB();
    this.crearReciboParaCliente(2800, 1L, 2L);
    this.crearReciboParaCliente(500, 1L, 3L);
    BusquedaCuentaCorrienteClienteCriteria criteria = BusquedaCuentaCorrienteClienteCriteria.builder()
      .pagina(0)
      .ordenarPor("cliente.nombreFiscal")
      .sentido("DESC")
      .build();
    HttpEntity<BusquedaCuentaCorrienteClienteCriteria> requestEntity = new HttpEntity<>(criteria);
    PaginaRespuestaRest<CuentaCorrienteCliente> paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/cuentas-corriente/clientes/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<CuentaCorrienteCliente>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    assertEquals(3, paginaRespuestaRest.getContent().size());
    ClienteDTO clienteParaBuscar = restTemplate.getForObject(apiPrefix + "/clientes/1", ClienteDTO.class);
    criteria = BusquedaCuentaCorrienteClienteCriteria.builder()
      .idCliente(clienteParaBuscar.getIdCliente())
      .nombreFiscal(clienteParaBuscar.getNombreFiscal())
      .nombreFantasia(clienteParaBuscar.getNombreFantasia())
      .idFiscal(clienteParaBuscar.getIdFiscal())
      .idViajante(clienteParaBuscar.getIdViajante())
      .idProvincia(clienteParaBuscar.getUbicacionFacturacion().getIdProvincia())
      .idLocalidad(clienteParaBuscar.getUbicacionFacturacion().getIdLocalidad())
      .nroDeCliente(clienteParaBuscar.getNroCliente())
    .build();
    requestEntity = new HttpEntity<>(criteria);
    paginaRespuestaRest =
      restTemplate
        .exchange(
          apiPrefix + "/cuentas-corriente/clientes/busqueda/criteria",
          HttpMethod.POST,
          requestEntity,
          new ParameterizedTypeReference<PaginaRespuestaRest<CuentaCorrienteCliente>>() {})
        .getBody();
    assertNotNull(paginaRespuestaRest);
    assertEquals(1, paginaRespuestaRest.getContent().size());
    assertEquals(new BigDecimal("-5616.8"), paginaRespuestaRest.getContent().get(0).getSaldo());
    assertEquals(clienteParaBuscar.getIdCliente(), paginaRespuestaRest.getContent().get(0).getCliente().getIdCliente());
  }
}
