package sic.integration;

import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import sic.model.NuevoTokenCard;
import sic.model.Pedido;
import sic.model.TokenCard;
import sic.modelo.*;
import sic.modelo.criteria.*;
import sic.modelo.dto.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(locations = "/application.properties")
class AppIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;
  private String token;
  private final String apiPrefix = "/api/v1";

  @Value("${SIC_MERCADOPAGO_PUBLIC_KEY}")
  private String mpPublicKey;

  @Value("${SIC_MERCADOPAGO_ACCESS_TOKEN}")
  private String mpAccesToken;

  private static final BigDecimal IVA_21 = new BigDecimal("21");
  private static final BigDecimal IVA_105 = new BigDecimal("10.5");
  private static final BigDecimal CIEN = new BigDecimal("100");

  @Value("${RECAPTCHA_TEST_KEY}")
  private String recaptchaTestKey;

  private void crearReciboParaCliente(
      double monto, long idSucursal, long idCliente, long idFormaDePago) {
    ReciboDTO recibo =
        ReciboDTO.builder()
            .concepto("Recibo Test")
            .monto(monto)
            .idSucursal(idSucursal)
            .idCliente(idCliente)
            .idFormaDePago(idFormaDePago)
            .build();
    restTemplate.postForObject(apiPrefix + "/recibos/clientes", recibo, ReciboDTO.class);
  }

  private void crearReciboParaProveedor(
      double monto, long idSucursal, long idProveedor, long idFormaDePago) {
    ReciboDTO recibo =
        ReciboDTO.builder()
            .monto(monto)
            .concepto("Recibo para proveedor")
            .idSucursal(idSucursal)
            .idProveedor(idProveedor)
            .idFormaDePago(idFormaDePago)
            .fecha(LocalDateTime.now())
            .build();
    restTemplate.postForObject(apiPrefix + "/recibos/proveedores", recibo, Recibo.class);
  }

  private void iniciarSesionComoAdministrador() {
    this.token =
        restTemplate
            .postForEntity(
                apiPrefix + "/login",
                new Credencial("dueño", "dueño123", Aplicacion.SIC_OPS_WEB),
                String.class)
            .getBody();
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
  }

  @Test
  @DisplayName("Se inicia una nueva sucursal y su usuario Administrador")
  @Order(1)
  void inicioDeActividad() {
    this.token =
        restTemplate
            .postForEntity(
                apiPrefix + "/login",
                new Credencial("test", "test", Aplicacion.SIC_OPS_WEB),
                String.class)
            .getBody();
    UsuarioDTO credencial =
        UsuarioDTO.builder()
            .username("dueño")
            .password("dueño123")
            .nombre("Max")
            .apellido("Power")
            .email("liderDeLaEmpresa@yahoo.com.br")
            .roles(new ArrayList<>(Collections.singletonList(Rol.ADMINISTRADOR)))
            .build();
    credencial = restTemplate.postForObject(apiPrefix + "/usuarios", credencial, UsuarioDTO.class);
    credencial.setHabilitado(true);
    restTemplate.put(apiPrefix + "/usuarios", credencial);
    this.iniciarSesionComoAdministrador();
    SucursalDTO sucursalDTO =
        SucursalDTO.builder()
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .email("support@globocorporation.com")
            .fechaInicioActividad(LocalDateTime.now())
            .idFiscal(30712391215L)
            .ingresosBrutos(123456789L)
            .lema("Primera Sucursal")
            .nombre("FirstOfAll")
            .telefono("3794551122")
            .ubicacion(
                UbicacionDTO.builder()
                    .idLocalidad(1L)
                    .idProvincia(1L)
                    .calle("Avenida 5315")
                    .codigoPostal("3400")
                    .build())
            .build();
    SucursalDTO sucursalRecuperada = restTemplate.postForObject(apiPrefix + "/sucursales", sucursalDTO, SucursalDTO.class);
    assertEquals(sucursalDTO, sucursalRecuperada);
    ConfiguracionSucursalDTO configuracionSucursalDTO =
        restTemplate.getForObject(
            apiPrefix + "/configuraciones-sucursal/" + sucursalRecuperada.getIdSucursal(), ConfiguracionSucursalDTO.class);
    configuracionSucursalDTO.setPuntoDeRetiro(true);
    restTemplate.put(apiPrefix + "/configuraciones-sucursal", configuracionSucursalDTO);
    configuracionSucursalDTO =
            restTemplate.getForObject(
                    apiPrefix + "/configuraciones-sucursal/" + sucursalRecuperada.getIdSucursal(), ConfiguracionSucursalDTO.class);
    assertTrue(configuracionSucursalDTO.isPuntoDeRetiro());
  }

  @Test
  @DisplayName(
      "Comprar productos al proveedor RI con factura A y verificar saldo CC, luego saldar la CC con un cheque de 3ro")
  @Order(2)
  void CompraEscenario1() {
    this.iniciarSesionComoAdministrador();
    ProveedorDTO proveedorDTO =
        ProveedorDTO.builder()
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .contacto("Raul Gomez")
            .email("chamacosrl@gmail.com")
            .idFiscal(23127895679L)
            .razonSocial("Chamaco S.R.L.")
            .telPrimario("3794356778")
            .telSecundario("3795300115")
            .web("www.chamacosrl.com.ar")
            .nroProveedor("1")
            .ubicacion(
                UbicacionDTO.builder()
                    .idLocalidad(3L)
                    .idProvincia(3L)
                    .calle("Avenida 6688")
                    .codigoPostal("3500")
                    .build())
            .build();
    restTemplate.postForObject(apiPrefix + "/proveedores", proveedorDTO, ProveedorDTO.class);
    RubroDTO rubroDTO = RubroDTO.builder()
            .nombre("Ferreteria")
            .build();
    RubroDTO rubro = restTemplate.postForObject(apiPrefix + "/rubros", rubroDTO, RubroDTO.class);
    MedidaDTO medidaDTO = MedidaDTO.builder()
            .nombre("Metro")
            .build();
    MedidaDTO medida = restTemplate.postForObject(apiPrefix + "/medidas", medidaDTO, MedidaDTO.class);
    NuevoProductoDTO productoUno =
        NuevoProductoDTO.builder()
            .codigo(RandomStringUtils.random(10, false, true))
            .descripcion("Ventilador de pie")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {
                    put(1L, BigDecimal.TEN);
                  }
                })
            .bulto(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .nota("ProductoTest1")
            .publico(true)
            .build();
    NuevoProductoDTO productoDos =
        NuevoProductoDTO.builder()
            .codigo(RandomStringUtils.random(10, false, true))
            .descripcion("Reflector led 100w")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {
                    put(1L, new BigDecimal("6"));
                  }
                })
            .bulto(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("10.5"))
            .ivaNeto(new BigDecimal("105"))
            .precioLista(new BigDecimal("1105"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .nota("ProductoTest2")
            .build();
    SucursalDTO sucursal =
        restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    ProveedorDTO proveedor =
        restTemplate.getForObject(apiPrefix + "/proveedores/1", ProveedorDTO.class);
    ProductoDTO productoUnoRecuperado =
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
    assertEquals("Ventilador de pie", productoUnoRecuperado.getDescripcion());
    assertEquals(BigDecimal.TEN, productoUnoRecuperado.getCantidadTotalEnSucursales());
    assertEquals("Metro", productoUnoRecuperado.getNombreMedida());
    assertEquals(new BigDecimal("100"), productoUnoRecuperado.getPrecioCosto());
    assertEquals(new BigDecimal("900"), productoUnoRecuperado.getGananciaPorcentaje());
    assertEquals(new BigDecimal("900"), productoUnoRecuperado.getGananciaNeto());
    assertEquals(new BigDecimal("1000"), productoUnoRecuperado.getPrecioVentaPublico());
    assertEquals(new BigDecimal("21.0"), productoUnoRecuperado.getIvaPorcentaje());
    assertEquals(new BigDecimal("210"), productoUnoRecuperado.getIvaNeto());
    assertEquals(new BigDecimal("1210"), productoUnoRecuperado.getPrecioLista());
    assertEquals("Ferreteria", productoUnoRecuperado.getNombreRubro());
    assertEquals(BigDecimal.ZERO, productoUnoRecuperado.getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("20"), productoUnoRecuperado.getPorcentajeBonificacionPrecio());
    assertEquals(
        new BigDecimal("968.000000000000000"), productoUnoRecuperado.getPrecioBonificado());
    ProductoDTO productoDosRecuperado =
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
    assertEquals("Reflector led 100w", productoDosRecuperado.getDescripcion());
    assertEquals(new BigDecimal("6"), productoDosRecuperado.getCantidadTotalEnSucursales());
    assertEquals("Metro", productoDosRecuperado.getNombreMedida());
    assertEquals(new BigDecimal("100"), productoDosRecuperado.getPrecioCosto());
    assertEquals(new BigDecimal("900"), productoDosRecuperado.getGananciaPorcentaje());
    assertEquals(new BigDecimal("900"), productoDosRecuperado.getGananciaNeto());
    assertEquals(new BigDecimal("1000"), productoDosRecuperado.getPrecioVentaPublico());
    assertEquals(new BigDecimal("10.5"), productoDosRecuperado.getIvaPorcentaje());
    assertEquals(new BigDecimal("105"), productoDosRecuperado.getIvaNeto());
    assertEquals(new BigDecimal("1105"), productoDosRecuperado.getPrecioLista());
    assertEquals("Ferreteria", productoDosRecuperado.getNombreRubro());
    assertEquals(BigDecimal.ZERO, productoDosRecuperado.getPorcentajeBonificacionOferta());
    assertEquals(new BigDecimal("20"), productoDosRecuperado.getPorcentajeBonificacionPrecio());
    assertEquals(
        new BigDecimal("884.000000000000000"), productoDosRecuperado.getPrecioBonificado());
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
    FacturaCompraDTO facturaCompraA = FacturaCompraDTO.builder().idProveedor(1L).build();
    facturaCompraA.setIdSucursal(1L);
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
    FacturaCompraDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/compra", facturaCompraA, FacturaCompraDTO[].class);
    assertEquals(facturaCompraA, facturas[0]);
    assertEquals(proveedor.getRazonSocial(), facturas[0].getRazonSocialProveedor());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(
        new BigDecimal("-554.540000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class));
    this.crearReciboParaProveedor(554.54, 1L, 1L, 2L);
    assertEquals(
        0.0,
        restTemplate
            .getForObject(apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
            .doubleValue());
  }

  @Test
  @DisplayName("Actualizar CC según ND por mora, luego verificar saldo CC")
  @Order(3)
  void CompraEscenario2() {
    this.iniciarSesionComoAdministrador();
    NuevaNotaDebitoDeReciboDTO nuevaNotaDebitoDeReciboDTO =
        NuevaNotaDebitoDeReciboDTO.builder()
            .idRecibo(1L)
            .gastoAdministrativo(new BigDecimal("1500.00"))
            .motivo("No pagamos, la vida es así.")
            .tipoDeComprobante(TipoDeComprobante.NOTA_DEBITO_A)
            .build();
    NotaDebitoDTO notaDebitoCalculada =
        restTemplate.postForObject(
            apiPrefix + "/notas/debito/calculos", nuevaNotaDebitoDeReciboDTO, NotaDebitoDTO.class);
    NotaDebitoDTO notaDebitoGuardada =
        restTemplate.postForObject(
            apiPrefix + "/notas/debito", notaDebitoCalculada, NotaDebitoDTO.class);
    assertEquals(new BigDecimal("1239.669421487603306"), notaDebitoGuardada.getSubTotalBruto());
    assertEquals(
        new BigDecimal("260.330578512396694260000000000000"), notaDebitoGuardada.getIva21Neto());
    assertEquals(
        new BigDecimal("2054.540000000000000260000000000000"), notaDebitoGuardada.getTotal());
    assertEquals(notaDebitoCalculada, notaDebitoGuardada);
    assertEquals(
        -2054.54,
        restTemplate
            .getForObject(apiPrefix + "/cuentas-corriente/proveedores/1/saldo", BigDecimal.class)
            .doubleValue());
  }

  @Test
  @DisplayName("Dar de alta un Cliente y levantar un Pedido.")
  @Order(4)
  void AltaClienteYPedido() {
    this.iniciarSesionComoAdministrador();
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
            .montoCompraMinima(BigDecimal.ONE)
            .nombreFiscal("Juan Fernando Cañete")
            .nombreFantasia("Menos mal que estamos nosotros.")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .idFiscal(30703176840L)
            .email("caniete@yahoo.com.br")
            .telefono("3785663322")
            .contacto("Ramon el hermano de Juan")
            .idCredencial(credencial.getIdUsuario())
            .build();
    ClienteDTO clienteRecuperado =
        restTemplate.postForObject(apiPrefix + "/clientes", cliente, ClienteDTO.class);
    assertEquals(cliente, clienteRecuperado);
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
            .observaciones("El pedido del primer cliente!")
            .renglones(renglonesPedidoDTO)
            .idSucursal(1L)
            .idCliente(clienteRecuperado.getIdCliente())
            .tipoDeEnvio(TipoDeEnvio.RETIRO_EN_SUCURSAL)
            .build();
    Pedido pedidoRecuperado =
        restTemplate.postForObject(apiPrefix + "/pedidos", pedidoDTO, Pedido.class);
    assertEquals(new BigDecimal("5947.200000000000000"), pedidoRecuperado.getTotalEstimado());
    assertEquals(pedidoRecuperado.getObservaciones(), pedidoDTO.getObservaciones());
    assertEquals(EstadoPedido.ABIERTO, pedidoRecuperado.getEstado());
  }

  @Test
  @DisplayName(
      "Vender productos al cliente RI con factura dividida, luego saldar la CC con efectivo")
  @Order(5)
  void VentaEscenario1() {
    this.iniciarSesionComoAdministrador();
    restTemplate.postForObject(
        apiPrefix + "/cajas/apertura/sucursales/1?saldoApertura=0", null, CajaDTO.class);
    List<RenglonFactura> renglones =
        Arrays.asList(
            restTemplate.getForObject(
                apiPrefix + "/facturas/renglones/pedidos/1?tipoDeComprobante=FACTURA_A",
                RenglonFactura[].class));
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
    UbicacionDTO ubicacionDeFacturacion =
        restTemplate.getForObject(apiPrefix + "/ubicaciones/1", UbicacionDTO.class);
    cliente.setUbicacionFacturacion(ubicacionDeFacturacion);
    restTemplate.put(apiPrefix + "/clientes", cliente);
    UsuarioDTO credencial = restTemplate.getForObject(apiPrefix + "/usuarios/2", UsuarioDTO.class);
    FacturaVentaDTO facturaVentaA =
        FacturaVentaDTO.builder()
            .nombreFiscalCliente(cliente.getNombreFiscal())
            .idCliente(cliente.getIdCliente())
            .build();
    facturaVentaA.setIdSucursal(1L);
    facturaVentaA.setIdCliente(1L);
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
    SucursalDTO sucursal =
        restTemplate.getForObject(apiPrefix + "/sucursales/1", SucursalDTO.class);
    int[] indices = new int[] {0};
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder().indices(indices).facturaVenta(facturaVentaA).build();
    FacturaVentaDTO[] facturas =
        restTemplate.postForObject(
            apiPrefix + "/facturas/venta", nuevaFacturaVentaDTO, FacturaVentaDTO[].class);
    assertEquals(2, facturas.length);
    assertEquals(cliente.getNombreFiscal(), facturas[0].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[0].getNombreSucursal());
    assertEquals(cliente.getNombreFiscal(), facturas[1].getNombreFiscalCliente());
    assertEquals(sucursal.getNombre(), facturas[1].getNombreSucursal());
    assertEquals(
        credencial.getNombre()
            + " "
            + credencial.getApellido()
            + " ("
            + credencial.getUsername()
            + ")",
        facturas[0].getNombreUsuario());
    assertEquals(
        credencial.getNombre()
            + " "
            + credencial.getApellido()
            + " ("
            + credencial.getUsername()
            + ")",
        facturas[1].getNombreUsuario());
    assertEquals(new BigDecimal("1600.000000000000000000000000000000"), facturas[0].getSubTotal());
    assertEquals(new BigDecimal("4000.000000000000000000000000000000"), facturas[1].getSubTotal());
    assertEquals(new BigDecimal("160.000000000000000"), facturas[0].getRecargoNeto());
    assertEquals(new BigDecimal("400.000000000000000"), facturas[1].getRecargoNeto());
    assertEquals(new BigDecimal("400.000000000000000"), facturas[0].getDescuentoNeto());
    assertEquals(new BigDecimal("1000.000000000000000"), facturas[1].getDescuentoNeto());
    assertEquals(BigDecimal.ZERO, facturas[0].getIva105Neto());
    assertEquals(
        new BigDecimal(
            "142.800000000000000000000000000000000000000000000000000000000000000000000000000"),
        facturas[1].getIva105Neto());
    assertEquals(new BigDecimal("0E-15"), facturas[0].getIva21Neto());
    assertEquals(
        new BigDecimal("428.400000000000000000000000000000000000000000000000000000000000"),
        facturas[1].getIva21Neto());
    assertEquals(
        new BigDecimal("1360.000000000000000000000000000000"), facturas[0].getSubTotalBruto());
    assertEquals(
        new BigDecimal("3400.000000000000000000000000000000"), facturas[1].getSubTotalBruto());
    assertEquals(new BigDecimal("1360.000000000000000000000000000000"), facturas[0].getTotal());
    assertEquals(
        new BigDecimal(
            "3971.200000000000000000000000000000000000000000000000000000000000000000000000000"),
        facturas[1].getTotal());
    List<RenglonFactura> renglonesFacturaUno =
        Arrays.asList(
            restTemplate.getForObject(apiPrefix + "/facturas/2/renglones", RenglonFactura[].class));
    List<RenglonFactura> renglonesFacturaDos =
        Arrays.asList(
            restTemplate.getForObject(apiPrefix + "/facturas/3/renglones", RenglonFactura[].class));
    assertEquals(2.0, renglonesFacturaUno.get(0).getCantidad().doubleValue());
    assertEquals(3.0, renglonesFacturaDos.get(0).getCantidad().doubleValue());
    assertEquals(2.0, renglonesFacturaDos.get(1).getCantidad().doubleValue());
    assertEquals(
        -5331.200000000000000,
        restTemplate
            .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
            .doubleValue());
    this.crearReciboParaCliente(5331.2, 1L, 1L, 1L);
    assertEquals(
        0.0,
        restTemplate
            .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
            .doubleValue());
  }

  @Test
  @DisplayName("Realizar devolución parcial de productos y verificar saldo CC")
  @Order(6)
  void VentaEscenario2() {
    this.iniciarSesionComoAdministrador();
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .tipoComprobante(TipoDeComprobante.FACTURA_X)
            .numSerie(0L)
            .numFactura(1L)
            .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    PaginaRespuestaRest<FacturaVentaDTO> resultadoBusqueda =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    List<FacturaVentaDTO> facturasRecuperadas = resultadoBusqueda.getContent();
    Long[] idsRenglonesFacutura = new Long[1];
    idsRenglonesFacutura[0] = 3L;
    BigDecimal[] cantidades = new BigDecimal[1];
    cantidades[0] = new BigDecimal("1");
    NuevaNotaCreditoDeFacturaDTO nuevaNotaCreditoDTO =
        NuevaNotaCreditoDeFacturaDTO.builder()
            .idFactura(facturasRecuperadas.get(0).getIdFactura())
            .idsRenglonesFactura(idsRenglonesFacutura)
            .cantidades(cantidades)
            .modificaStock(true)
            .motivo("No funcionan.")
            .build();
    NotaCreditoDTO notaCreditoParaPersistir =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito/calculos", nuevaNotaCreditoDTO, NotaCreditoDTO.class);
    NotaCreditoDTO notaGuardada =
        restTemplate.postForObject(
            apiPrefix + "/notas/credito", notaCreditoParaPersistir, NotaCreditoDTO.class);
    notaCreditoParaPersistir.setNroNota(notaGuardada.getNroNota());
    assertEquals(notaCreditoParaPersistir, notaGuardada);
    assertEquals(new BigDecimal("800.000000000000000000000000000000"), notaGuardada.getSubTotal());
    assertEquals(new BigDecimal("80.000000000000000"), notaGuardada.getRecargoNeto());
    assertEquals(new BigDecimal("200.000000000000000"), notaGuardada.getDescuentoNeto());
    assertEquals(
        new BigDecimal("680.000000000000000000000000000000"), notaGuardada.getSubTotalBruto());
    assertEquals(0.0, notaGuardada.getIva21Neto().doubleValue());
    assertEquals(BigDecimal.ZERO, notaGuardada.getIva105Neto());
    assertEquals(new BigDecimal("680.000000000000000000000000000000"), notaGuardada.getTotal());
    assertEquals(TipoDeComprobante.NOTA_CREDITO_X, notaGuardada.getTipoComprobante());
    restTemplate.getForObject(apiPrefix + "/notas/1/reporte", byte[].class);
    assertEquals(
        680.0,
        restTemplate
            .getForObject(apiPrefix + "/cuentas-corriente/clientes/1/saldo", BigDecimal.class)
            .doubleValue());
    BusquedaCajaCriteria criteriaCaja = BusquedaCajaCriteria.builder().idSucursal(1L).build();
    HttpEntity<BusquedaCajaCriteria> requestEntityCaja = new HttpEntity(criteria);
    PaginaRespuestaRest<CajaDTO> resultadoBusquedaCaja =
        restTemplate
            .exchange(
                apiPrefix + "/cajas/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<CajaDTO>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaCaja);
    List<CajaDTO> cajasRecuperadas = resultadoBusquedaCaja.getContent();
    restTemplate.put(
        apiPrefix + "/cajas/" + cajasRecuperadas.get(0).getIdCaja() + "/cierre?monto=5331.2",
        CajaDTO.class);
    List<MovimientoCaja> movimientoCajas =
        Arrays.asList(
            restTemplate.getForObject(
                apiPrefix
                    + "/cajas/"
                    + cajasRecuperadas.get(0).getIdCaja()
                    + "/movimientos?idFormaDePago=1",
                MovimientoCaja[].class));
    assertEquals(1, movimientoCajas.size());
    assertEquals(
        new BigDecimal("5331.200000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cajas/" + cajasRecuperadas.get(0).getIdCaja() + "/saldo-afecta-caja",
            BigDecimal.class));
    assertEquals(
        new BigDecimal("5331.200000000000000"),
        restTemplate.getForObject(
            apiPrefix + "/cajas/" + cajasRecuperadas.get(0).getIdCaja() + "/saldo-sistema",
            BigDecimal.class));
    resultadoBusquedaCaja =
        restTemplate
            .exchange(
                apiPrefix + "/cajas/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<CajaDTO>>() {})
            .getBody();
    assertNotNull(resultadoBusquedaCaja);
    cajasRecuperadas = resultadoBusquedaCaja.getContent();
    assertEquals(EstadoCaja.CERRADA, cajasRecuperadas.get(0).getEstado());
  }

  @Test
  @DisplayName("Un Usuario se registra y luego da de alta un Pedido.")
  @Order(7)
  void RegistraciónYPedidoDelNuevoCliente() {
    RegistracionClienteAndUsuarioDTO registro =
        RegistracionClienteAndUsuarioDTO.builder()
            .apellido("Stark")
            .nombre("Sansa")
            .categoriaIVA(CategoriaIVA.RESPONSABLE_INSCRIPTO)
            .email("sansa@got.com")
            .telefono("4157899667")
            .password("caraDeMala")
            .recaptcha(recaptchaTestKey)
            .nombreFiscal("theRedWolf")
            .build();
    restTemplate.postForObject(apiPrefix + "/registracion", registro, Void.class);
    this.iniciarSesionComoAdministrador();
    UsuarioDTO usuario = restTemplate.getForObject(apiPrefix + "/usuarios/4", UsuarioDTO.class);
    assertEquals("Sansa", usuario.getNombre());
    assertEquals("Stark", usuario.getApellido());
    assertTrue(usuario.isHabilitado());
    ClienteDTO cliente = restTemplate.getForObject(apiPrefix + "/clientes/2", ClienteDTO.class);
    assertEquals("theRedWolf", cliente.getNombreFiscal());
    cliente.setUbicacionFacturacion(
        UbicacionDTO.builder()
            .idLocalidad(2L)
            .idProvincia(2L)
            .calle("Avenida 334")
            .codigoPostal("3456")
            .build());
    restTemplate.put(apiPrefix + "/clientes", cliente);
    this.token =
        restTemplate
            .postForEntity(
                apiPrefix + "/login",
                new Credencial(usuario.getUsername(), "caraDeMala", Aplicacion.SIC_OPS_WEB),
                String.class)
            .getBody();
    restTemplate.postForObject(
        apiPrefix
            + "/carrito-compra/usuarios/"
            + usuario.getIdUsuario()
            + "/productos/1?cantidad=5",
        null,
        ItemCarritoCompra.class);
    restTemplate.postForObject(
        apiPrefix
            + "/carrito-compra/usuarios/"
            + usuario.getIdUsuario()
            + "/productos/2?cantidad=9",
        null,
        ItemCarritoCompra.class);
    ItemCarritoCompra item1 =
        restTemplate.getForObject(
            apiPrefix + "/carrito-compra/usuarios/" + usuario.getIdUsuario() + "/productos/1",
            ItemCarritoCompra.class);
    assertEquals(1L, item1.getProducto().getIdProducto().longValue());
    assertEquals(5, item1.getCantidad().doubleValue());
    assertTrue(item1.getProducto().isHayStock());
    ItemCarritoCompra item2 =
        restTemplate.getForObject(
            apiPrefix + "/carrito-compra/usuarios/4/productos/2", ItemCarritoCompra.class);
    assertEquals(2L, item2.getProducto().getIdProducto().longValue());
    assertEquals(9, item2.getCantidad().doubleValue());
    assertTrue(item2.getProducto().isHayStock());
    NuevaOrdenDeCompraDTO nuevaOrdenDeCompraDTO =
        NuevaOrdenDeCompraDTO.builder()
            .idCliente(2L)
            .idUsuario(4L)
            .tipoDeEnvio(TipoDeEnvio.USAR_UBICACION_FACTURACION)
            .observaciones("Por favor comunicarse conmigo antes de facturar.")
            .build();
    Pedido pedido =
        restTemplate.postForObject(
            apiPrefix + "/carrito-compra", nuevaOrdenDeCompraDTO, Pedido.class);
    assertEquals(14, pedido.getCantidadArticulos().doubleValue());
    assertEquals(
        new BigDecimal("12796.00000000000000000000000000000000000000000000000"),
        pedido.getTotalActual());
  }

  @Test
  @DisplayName("Autorizar una factura")
  @Order(8)
  void AutorizarFactura() throws IOException {
    File resource = new ClassPathResource("/certificadoAfipTest.p12").getFile();
    byte[] certificadoAfip = new byte[(int) resource.length()];
    FileInputStream fileInputStream = new FileInputStream(resource);
    fileInputStream.read(certificadoAfip);
    fileInputStream.close();
    this.iniciarSesionComoAdministrador();
    ConfiguracionSucursalDTO configuracionSucursalDTO =
        restTemplate.getForObject(
            apiPrefix + "/configuraciones-sucursal/1", ConfiguracionSucursalDTO.class);
    configuracionSucursalDTO.setCertificadoAfip(certificadoAfip);
    configuracionSucursalDTO.setFacturaElectronicaHabilitada(true);
    configuracionSucursalDTO.setFirmanteCertificadoAfip("globo");
    configuracionSucursalDTO.setPasswordCertificadoAfip("globo123");
    configuracionSucursalDTO.setNroPuntoDeVentaAfip(2);
    restTemplate.put(apiPrefix + "/configuraciones-sucursal", configuracionSucursalDTO);
    BusquedaFacturaVentaCriteria criteria =
        BusquedaFacturaVentaCriteria.builder()
            .idSucursal(1L)
            .tipoComprobante(TipoDeComprobante.FACTURA_A)
            .numSerie(0L)
            .numFactura(1L)
            .build();
    HttpEntity<BusquedaFacturaVentaCriteria> requestEntity = new HttpEntity(criteria);
    PaginaRespuestaRest<FacturaVentaDTO> resultadoBusqueda =
        restTemplate
            .exchange(
                apiPrefix + "/facturas/venta/busqueda/criteria",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PaginaRespuestaRest<FacturaVentaDTO>>() {})
            .getBody();
    assertNotNull(resultadoBusqueda);
    List<FacturaVentaDTO> facturasRecuperadas = resultadoBusqueda.getContent();
    restTemplate.postForObject(
        apiPrefix + "/facturas/" + facturasRecuperadas.get(0).getIdFactura() + "/autorizacion",
        null,
        FacturaVentaDTO.class);
    FacturaVentaDTO facturaVentaDTO =
        restTemplate.getForObject(
            apiPrefix + "/facturas/" + facturasRecuperadas.get(0).getIdFactura(),
            FacturaVentaDTO.class);
    assertNotEquals(0L, facturaVentaDTO.getCae());
  }

  @Test
  @DisplayName("Un producto es dado de alta con una imagen agregada")
  @Order(9)
  void subirImagenDeProductoEnAlta() throws IOException {
    BufferedImage bImage = ImageIO.read(getClass().getResource("/imagenProductoTest.jpeg"));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(bImage, "jpeg", bos);
    this.iniciarSesionComoAdministrador();
    NuevoProductoDTO productoUno =
        NuevoProductoDTO.builder()
            .codigo(RandomStringUtils.random(10, false, true))
            .descripcion("Llave termica SICA")
            .cantidadEnSucursal(
                new HashMap<Long, BigDecimal>() {
                  {
                    put(1L, BigDecimal.TEN);
                  }
                })
            .bulto(BigDecimal.ONE)
            .precioCosto(CIEN)
            .gananciaPorcentaje(new BigDecimal("900"))
            .gananciaNeto(new BigDecimal("900"))
            .precioVentaPublico(new BigDecimal("1000"))
            .ivaPorcentaje(new BigDecimal("21.0"))
            .ivaNeto(new BigDecimal("210"))
            .precioLista(new BigDecimal("1210"))
            .porcentajeBonificacionPrecio(new BigDecimal("20"))
            .nota("probando upload de imagen")
            .publico(true)
            .imagen(bos.toByteArray())
            .build();
    ProductoDTO productoConImagen =
        restTemplate.postForObject(
            apiPrefix + "/productos?idMedida=1&idRubro=1&idProveedor=1",
            productoUno,
            ProductoDTO.class);
    assertNotNull(productoConImagen.getUrlImagen());
  }

  @Test
  @DisplayName("Un pago es dado de alta por pago MercadoPago")
  @Order(10)
  @Ignore
  void agregarPagoMercadoPago() {
    this.iniciarSesionComoAdministrador();
    UsuarioDTO usuario = restTemplate.getForObject(apiPrefix + "/usuarios/4", UsuarioDTO.class);
    this.token =
            restTemplate
                    .postForEntity(
                            apiPrefix + "/login",
                            new Credencial(usuario.getUsername(), "caraDeMala", Aplicacion.SIC_OPS_WEB),
                            String.class)
                    .getBody();
 /*   List<LinkedHashMap<String, LinkedHashMap<String, String>>> installments = restTemplate.getForObject(
        "https://api.mercadopago.com/v1/payment_methods/installments?public_key=" + mpPublicKey
            + "&bin=450995",
        List.class);
    assertFalse(installments.isEmpty());
    assertEquals(1, installments.size());
    NuevoTokenCard nuevoTokenCard =
        NuevoTokenCard.builder()
            .email("correoelectronicoparatesting@gmail.com")
            .cardNumber("4170068810108020")
            .securityCode("132")
            .cardExpirationMonth(11)
            .cardExpirationYear(2025)
            .cardHoldName("APRO")
            .docType("DNI")
            .docNumber(12455698L)
            .installments(1)
            .build();
    TokenCard tokenCard =
        restTemplate.postForObject(
            "https://api.mercadopago.com/v1/card_tokens?public_key=" + mpPublicKey,
            nuevoTokenCard,
            TokenCard.class);*/
    NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO =
        NuevoPagoMercadoPagoDTO.builder()
            //.token(tokenCard.getId())
            .paymentMethodId("pagofacil")
            //.issuerId("1")
            .installments(1)
            .idCliente(1L)
            .idSucursal(1L)
            .monto(new Float("2000"))
            .build();
    String paymentId =
        restTemplate.postForObject(
            apiPrefix + "/pagos/mercado-pago", nuevoPagoMercadoPagoDTO, String.class);
    restTemplate.postForObject(apiPrefix + "/pagos/notificacion?data.id=" + paymentId + "&type=payment", null, void.class);
    assertNotNull(paymentId);
    /*
    this.iniciarSesionComoAdministrador();
    BusquedaReciboCriteria criteria = BusquedaReciboCriteria.builder().build();
    HttpEntity<BusquedaReciboCriteria> requestEntity = new HttpEntity(criteria);
    PaginaRespuestaRest<ReciboDTO> resultadoBusqueda =
            restTemplate
                    .exchange(
                            apiPrefix + "/recibos/busqueda/criteria",
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PaginaRespuestaRest<ReciboDTO>>() {})
                    .getBody();
    assertNotNull(resultadoBusqueda);
    List<ReciboDTO> recibosRecuperados = resultadoBusqueda.getContent();*/
  }
}
