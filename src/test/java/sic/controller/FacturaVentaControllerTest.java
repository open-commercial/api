package sic.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.dto.NuevaFacturaVentaDTO;
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.service.impl.*;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppTest.class)
class FacturaVentaControllerTest {

  @Autowired private MessageSource messageSourceTest;

  @Mock private MessageSource messageSourceTestMock;
  @Mock private SucursalServiceImpl sucursalService;
  @Mock private TransportistaServiceImpl transportistaService;
  @Mock private ClienteServiceImpl clienteService;
  @Mock private UsuarioServiceImpl usuarioService;
  @Mock private FacturaServiceImpl facturaService;
  @Mock private ReciboServiceImpl reciboService;
  @Mock private FacturaVentaServiceImpl facturaVentaService;
  @Mock private AuthServiceImpl authService;

  @InjectMocks private FacturaVentaController facturaVentaController;

  @Test
  void shouldGuardarFacturaVenta() {
    NuevaFacturaVentaDTO nuevaFacturaVentaDTO =
        NuevaFacturaVentaDTO.builder().tipoDeComprobante(TipoDeComprobante.PEDIDO).build();
    when(messageSourceTestMock.getMessage(
            "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()))
        .thenReturn(
            messageSourceTest.getMessage(
                "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
    BusinessServiceException thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> facturaVentaController.guardarFacturaVenta(nuevaFacturaVentaDTO, "headers"));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSourceTest.getMessage(
                    "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault())));
    Sucursal sucursal = new Sucursal();
    sucursal.setNombre("Sucursal prueba");
    when(sucursalService.getSucursalPorId(2L)).thenReturn(sucursal);
    Cliente cliente = new Cliente();
    cliente.setEmail("asd@asd.com");
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    NuevaFacturaVentaDTO nuevaFacturaVenta2DTO =
        NuevaFacturaVentaDTO.builder()
            .tipoDeComprobante(TipoDeComprobante.FACTURA_A)
            .idSucursal(2L)
            .idCliente(1L)
            .build();
    when(messageSourceTestMock.getMessage(
            "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault()))
        .thenReturn(
            messageSourceTest.getMessage(
                "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault()));
    thrown =
        assertThrows(
            BusinessServiceException.class,
            () -> facturaVentaController.guardarFacturaVenta(nuevaFacturaVenta2DTO, "headers"));
    assertNotNull(thrown.getMessage());
    assertTrue(
        thrown
            .getMessage()
            .contains(
                messageSourceTest.getMessage(
                    "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault())));
    Transportista transportista = new Transportista();
    transportista.setNombre("OCA");
    when(transportistaService.getTransportistaNoEliminadoPorId(4L)).thenReturn(transportista);
    Ubicacion ubicacion = new Ubicacion();
    cliente.setUbicacionFacturacion(ubicacion);
    when(clienteService.getClienteNoEliminadoPorId(1L)).thenReturn(cliente);
    NuevaFacturaVentaDTO nuevaFacturaVenta3DTO =
        NuevaFacturaVentaDTO.builder()
            .tipoDeComprobante(TipoDeComprobante.FACTURA_A)
            .idSucursal(2L)
            .idCliente(1L)
            .idTransportista(4L)
            .build();
    Usuario usuario = new Usuario();
    usuario.setUsername("usuario");
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    List<Rol> roles = Collections.singletonList(Rol.ADMINISTRADOR);
    SecretKey secretKey = MacProvider.generateKey();
    String token =
        Jwts.builder()
            .setIssuedAt(Date.from(zdtNow.toInstant()))
            .setExpiration(Date.from(zdtInOneMonth.toInstant()))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .claim("idUsuario", 1L)
            .claim("roles", roles)
            .claim("app", Aplicacion.SIC_COM)
            .compact();
    Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    List<RenglonFactura> renglones = new ArrayList<>();
    when(facturaService.calcularRenglones(any(), any(), any())).thenReturn(renglones);
    List<FacturaVenta> facturas = new ArrayList<>();
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setNumSerie(1L);
    facturaVenta.setNumFactura(123L);
    facturas.add(facturaVenta);
    when(facturaVentaService.guardar(any(), any(), any())).thenReturn(facturas);
    assertEquals(
        facturaVenta,
        facturaVentaController.guardarFacturaVenta(nuevaFacturaVenta3DTO, "headers").get(0));
  }

  @Test
  void shouldAutorizarFactura() {
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setNumSerie(1L);
    facturaVenta.setNumFactura(123L);
    when(facturaService.getFacturaNoEliminadaPorId(5L)).thenReturn(facturaVenta);
    when(facturaVentaService.autorizarFacturaVenta(facturaVenta)).thenReturn(facturaVenta);
    assertEquals(facturaVenta, facturaVentaController.autorizarFactura(5L));
  }

  @Test
  void shouldBuscarFacturaVenta() {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    SecretKey secretKey = MacProvider.generateKey();
    String token =
        Jwts.builder()
            .setIssuedAt(Date.from(zdtNow.toInstant()))
            .setExpiration(Date.from(zdtInOneMonth.toInstant()))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .claim("idUsuario", 1L)
            .claim("roles", Collections.singletonList(Rol.ADMINISTRADOR))
            .claim("app", Aplicacion.SIC_COM)
            .compact();
    Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    when(authService.getClaimsDelToken(any())).thenReturn(claims);
    List<FacturaVenta> facturas = new ArrayList<>();
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setNumSerie(1L);
    facturaVenta.setNumFactura(123L);
    facturas.add(facturaVenta);
    Page<FacturaVenta> pagina = new PageImpl<>(facturas);
    BusquedaFacturaVentaCriteria criteria = BusquedaFacturaVentaCriteria.builder().build();
    when(facturaVentaService.buscarFacturaVenta(criteria, 1L)).thenReturn(pagina);
    assertEquals(pagina, facturaVentaController.buscarFacturaVenta(criteria, "head"));
  }

  @Test
  void shouldGetTipoFacturaVenta() {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    SecretKey secretKey = MacProvider.generateKey();
    Claims claims =
        Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(
                Jwts.builder()
                    .setIssuedAt(Date.from(zdtNow.toInstant()))
                    .setExpiration(Date.from(zdtInOneMonth.toInstant()))
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .claim("idUsuario", 1L)
                    .claim("roles", Collections.singletonList(Rol.ADMINISTRADOR))
                    .claim("app", Aplicacion.SIC_COM)
                    .compact())
            .getBody();
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    Usuario usuario = new Usuario();
    usuario.setUsername("usuario");
    usuario.setRoles(Collections.singletonList(Rol.ADMINISTRADOR));
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    when(facturaVentaService.getTiposDeComprobanteVenta(any(), any()))
        .thenReturn(new TipoDeComprobante[] {TipoDeComprobante.FACTURA_A});
    assertEquals(
        new TipoDeComprobante[] {TipoDeComprobante.FACTURA_A}[0],
        facturaVentaController.getTipoFacturaVenta(1L, 1L, "headers")[0]);
    claims =
        Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(
                Jwts.builder()
                    .setIssuedAt(Date.from(zdtNow.toInstant()))
                    .setExpiration(Date.from(zdtInOneMonth.toInstant()))
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .claim("idUsuario", 1L)
                    .claim("roles", Collections.singletonList(Rol.VENDEDOR))
                    .claim("app", Aplicacion.SIC_COM)
                    .compact())
            .getBody();
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    when(facturaVentaService.getTiposDeComprobanteVenta(any(), any()))
        .thenReturn(new TipoDeComprobante[] {TipoDeComprobante.PEDIDO});
    assertEquals(
        new TipoDeComprobante[] {TipoDeComprobante.PEDIDO}[0],
        facturaVentaController.getTipoFacturaVenta(1L, 1L, "headers")[0]);
  }

  @Test
  void shouldGetReporteFacturaVenta() {
    FacturaVenta factura = new FacturaVenta();
    when(facturaService.getFacturaNoEliminadaPorId(1L)).thenReturn(factura);
    byte[] bytes = ("bytes[]").getBytes();
    when(facturaVentaService.getReporteFacturaVenta(factura)).thenReturn(bytes);
    assertEquals(bytes, facturaVentaController.getReporteFacturaVenta(1L).getBody());
  }

  @Test
  void shouldGetRenglonesPedidoParaFacturar() {
    List<RenglonFactura> renglonesFactura = new ArrayList<>();
    RenglonFactura renglonFactura = new RenglonFactura();
    renglonesFactura.add(renglonFactura);
    when(facturaVentaService.getRenglonesPedidoParaFacturar(1L, TipoDeComprobante.FACTURA_A))
        .thenReturn(renglonesFactura);
    assertEquals(
        renglonesFactura,
        facturaVentaController.getRenglonesPedidoParaFacturar(1L, TipoDeComprobante.FACTURA_A));
  }

  @Test
  void shouldCalcularRenglonesVenta() {
    List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO = new ArrayList<>();
    List<RenglonFactura> renglonesFacturas = new ArrayList<>();
    when(facturaService.calcularRenglones(
            TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevosRenglonesFacturaDTO))
        .thenReturn(renglonesFacturas);
    facturaVentaController.calcularRenglonesVenta(
        nuevosRenglonesFacturaDTO, TipoDeComprobante.FACTURA_A);
  }

  @Test
  void shouldCalcularTotalFacturadoVenta() {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    SecretKey secretKey = MacProvider.generateKey();
    Claims claims =
        Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(
                Jwts.builder()
                    .setIssuedAt(Date.from(zdtNow.toInstant()))
                    .setExpiration(Date.from(zdtInOneMonth.toInstant()))
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .claim("idUsuario", 1L)
                    .claim("roles", Collections.singletonList(Rol.ADMINISTRADOR))
                    .claim("app", Aplicacion.SIC_COM)
                    .compact())
            .getBody();
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    BusquedaFacturaVentaCriteria busquedaFacturaVentaCriteria =
        BusquedaFacturaVentaCriteria.builder().build();
    when(facturaVentaService.calcularTotalFacturadoVenta(busquedaFacturaVentaCriteria, 1L))
        .thenReturn(BigDecimal.TEN);
    assertEquals(
        BigDecimal.TEN,
        facturaVentaController.calcularTotalFacturadoVenta(
            busquedaFacturaVentaCriteria, "headers"));
  }

  @Test
  void shouldCalcularIvaVenta() {
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    SecretKey secretKey = MacProvider.generateKey();
    Claims claims =
        Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(
                Jwts.builder()
                    .setIssuedAt(Date.from(zdtNow.toInstant()))
                    .setExpiration(Date.from(zdtInOneMonth.toInstant()))
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .claim("idUsuario", 1L)
                    .claim("roles", Collections.singletonList(Rol.ADMINISTRADOR))
                    .claim("app", Aplicacion.SIC_COM)
                    .compact())
            .getBody();
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    BusquedaFacturaVentaCriteria busquedaFacturaVentaCriteria =
        BusquedaFacturaVentaCriteria.builder().build();
    when(facturaVentaService.calcularIvaVenta(busquedaFacturaVentaCriteria, 1L))
        .thenReturn(BigDecimal.TEN);
    assertEquals(
        BigDecimal.TEN,
        facturaVentaController.calcularIvaVenta(busquedaFacturaVentaCriteria, "headers"));
  }

  @Test
  void shouldCalcularGananciaTotal() {
    //    @RequestBody BusquedaFacturaVentaCriteria criteria,
    //    @RequestHeader("Authorization") String authorizationHeader) {
    //      Claims claims = authService.getClaimsDelToken(authorizationHeader);
    //      return facturaVentaService.calcularGananciaTotal(criteria, (int)
    // claims.get(CLAIM_ID_USUARIO));
    LocalDateTime today = LocalDateTime.now();
    ZonedDateTime zdtNow = today.atZone(ZoneId.systemDefault());
    ZonedDateTime zdtInOneMonth = today.plusMonths(1L).atZone(ZoneId.systemDefault());
    SecretKey secretKey = MacProvider.generateKey();
    Claims claims =
        Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(
                Jwts.builder()
                    .setIssuedAt(Date.from(zdtNow.toInstant()))
                    .setExpiration(Date.from(zdtInOneMonth.toInstant()))
                    .signWith(SignatureAlgorithm.HS512, secretKey)
                    .claim("idUsuario", 1L)
                    .claim("roles", Collections.singletonList(Rol.ADMINISTRADOR))
                    .claim("app", Aplicacion.SIC_COM)
                    .compact())
            .getBody();
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    BusquedaFacturaVentaCriteria busquedaFacturaVentaCriteria =
        BusquedaFacturaVentaCriteria.builder().build();
    when(facturaVentaService.calcularGananciaTotal(busquedaFacturaVentaCriteria, 1L))
        .thenReturn(BigDecimal.TEN);
    assertEquals(
        BigDecimal.TEN,
        facturaVentaController.calcularGananciaTotal(busquedaFacturaVentaCriteria, "headers"));
  }

  @Test
  void shouldEnviarFacturaVentaPorEmail() {
    facturaVentaController.enviarFacturaVentaPorEmail(1L);
  }
}
