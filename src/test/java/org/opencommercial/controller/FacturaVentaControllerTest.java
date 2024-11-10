package org.opencommercial.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaFacturaVentaCriteria;
import org.opencommercial.model.dto.NuevaFacturaVentaDTO;
import org.opencommercial.model.dto.NuevoRenglonFacturaDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FacturaVentaController.class, MessageSource.class})
class FacturaVentaControllerTest {

  @MockBean SucursalServiceImpl sucursalService;
  @MockBean TransportistaServiceImpl transportistaService;
  @MockBean ClienteServiceImpl clienteService;
  @MockBean UsuarioServiceImpl usuarioService;
  @MockBean FacturaServiceImpl facturaService;
  @MockBean ReciboServiceImpl reciboService;
  @MockBean FacturaVentaServiceImpl facturaVentaService;
  @MockBean AuthServiceImpl authService;
  @MockBean PedidoServiceImpl pedidoService;
  @MockBean MessageSource messageSource;

  @Autowired FacturaVentaController facturaVentaController;

  @Test
  void shouldGuardarFacturaVenta() {
    var nuevaFacturaVentaDTO = NuevaFacturaVentaDTO.builder().tipoDeComprobante(TipoDeComprobante.PEDIDO).build();
    assertThrows(
        BusinessServiceException.class,
        () -> facturaVentaController.guardarFacturaVenta(nuevaFacturaVentaDTO, 1L, "headers"));
    verify(messageSource).getMessage(eq("mensaje_tipo_de_comprobante_no_valido"), any(), any());
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
    Pedido pedido = new Pedido();
    pedido.setIdPedido(1L);
    pedido.setSucursal(sucursal);
    when(pedidoService.getPedidoNoEliminadoPorId(1L)).thenReturn(new Pedido());
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    Usuario usuario = new Usuario();
    usuario.setUsername("usuario");
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    FacturaVenta facturaVenta = new FacturaVenta();
    facturaVenta.setCliente(cliente);
    facturaVenta.setUsuario(usuario);
    facturaVenta.setTipoComprobante(TipoDeComprobante.FACTURA_A);
    when(facturaVentaService.construirFacturaVenta(any(), anyLong(), anyLong())).thenReturn(facturaVenta);
    facturaVentaController.guardarFacturaVenta(nuevaFacturaVenta2DTO, 1L, "headers");
    List<FacturaVenta> facturaVentas = new ArrayList<>();
    facturaVentas.add(facturaVenta);
    verify(facturaVentaService, times(1)).guardar(facturaVentas, 1L, Collections.emptyList());
    facturaVenta.setTipoComprobante(TipoDeComprobante.FACTURA_X);
    when(facturaVentaService.construirFacturaVenta(any(), anyLong(), anyLong())).thenReturn(facturaVenta);
    verify(facturaVentaService, times(1)).guardar(facturaVentas, 1L, Collections.emptyList());
  }

  @Test
  void shouldBuscarFacturaVenta() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
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
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    Usuario usuario = new Usuario();
    usuario.setUsername("usuario");
    usuario.setRoles(Collections.singletonList(Rol.ADMINISTRADOR));
    when(usuarioService.getUsuarioNoEliminadoPorId(1L)).thenReturn(usuario);
    when(facturaVentaService.getTiposDeComprobanteVenta(any(), any(), any()))
        .thenReturn(new TipoDeComprobante[] {TipoDeComprobante.FACTURA_A});
    assertEquals(
            TipoDeComprobante.FACTURA_A,
            facturaVentaController.getTipoFacturaVenta(1L, 1L, "headers")[0]);
    claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("VENDEDOR")));
    when(authService.getClaimsDelToken("headers")).thenReturn(claims);
    when(facturaVentaService.getTiposDeComprobanteVenta(any(), any(), any()))
        .thenReturn(new TipoDeComprobante[] {TipoDeComprobante.PEDIDO});
    assertEquals(
            TipoDeComprobante.PEDIDO,
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
    assertNotNull(
        facturaVentaController.calcularRenglonesVenta(
            nuevosRenglonesFacturaDTO, TipoDeComprobante.FACTURA_A));
  }

  @Test
  void shouldCalcularTotalFacturadoVenta() {
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
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
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
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
    var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
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
    verify(facturaVentaService).enviarFacturaVentaPorEmail(1L);
  }

  @Test
  void shouldGetFacturasVentaPorId() {
    List<FacturaVenta> facturasVenta = new ArrayList<>();
    FacturaVenta facturaVentaUno = new FacturaVenta();
    facturaVentaUno.setIdFactura(2L);
    FacturaVenta facturaVentaDos = new FacturaVenta();
    facturaVentaDos.setIdFactura(3L);
    facturasVenta.add(facturaVentaUno);
    facturasVenta.add(facturaVentaDos);
    when(facturaVentaService.getFacturasVentaPorId(new long[] {2L, 3L})).thenReturn(facturasVenta);
    List<FacturaVenta> facturasRecuperadas = facturaVentaService.getFacturasVentaPorId(new long[] {2L, 3L});
    assertEquals(facturasVenta, facturasRecuperadas);
  }
}
