package org.opencommercial.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaFacturaCompraCriteria;
import org.opencommercial.model.criteria.BusquedaFacturaVentaCriteria;
import org.opencommercial.model.dto.NuevaFacturaCompraDTO;
import org.opencommercial.model.dto.NuevaFacturaVentaDTO;
import org.opencommercial.model.dto.NuevoRenglonFacturaDTO;
import org.opencommercial.model.dto.NuevosResultadosComprobanteDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FacturaController.class, MessageSource.class})
class FacturaControllerTest {

    @MockitoBean SucursalServiceImpl sucursalService;
    @MockitoBean FacturaServiceImpl facturaService;
    @MockitoBean FacturaCompraServiceImpl facturaCompraService;
    @MockitoBean ProveedorServiceImpl proveedorService;
    @MockitoBean UsuarioServiceImpl usuarioService;
    @MockitoBean TransportistaServiceImpl transportistaService;
    @MockitoBean FacturaVentaServiceImpl facturaVentaService;
    @MockitoBean ReciboServiceImpl reciboService;
    @MockitoBean AuthServiceImpl authService;
    @MockitoBean ClienteServiceImpl clienteService;
    @MockitoBean PedidoServiceImpl pedidoService;
    @MockitoBean MessageSource messageSource;

    @Autowired FacturaController facturaController;

    @Test
    void shouldGetTiposFacturaSegunSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setIdSucursal(3L);
        when(sucursalService.getSucursalPorId(3L)).thenReturn(sucursal);
        TipoDeComprobante[] tipoDeComprobantes =
                new TipoDeComprobante[]{TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
        when(facturaService.getTiposDeComprobanteSegunSucursal(sucursal))
                .thenReturn(tipoDeComprobantes);
        assertEquals(tipoDeComprobantes, facturaController.getTiposFacturaSegunSucursal(3L));
    }

    @Test
    void shouldCalcularResultadosFactura() {
        Resultados resultados = new Resultados();
        resultados.setSubTotal(new BigDecimal("11"));
        resultados.setTotal(new BigDecimal("11"));
        resultados.setIva21Neto(BigDecimal.ONE);
        resultados.setIva105Neto(BigDecimal.ZERO);
        resultados.setSubTotalBruto(new BigDecimal("11"));
        resultados.setDescuentoPorcentaje(BigDecimal.ONE);
        resultados.setDescuentoNeto(BigDecimal.TEN);
        resultados.setRecargoPorcentaje(BigDecimal.ONE);
        resultados.setRecargoNeto(BigDecimal.TEN);
        NuevosResultadosComprobanteDTO nuevosResultadosComprobanteDTO =
                NuevosResultadosComprobanteDTO.builder()
                        .importe(new BigDecimal[]{BigDecimal.TEN, BigDecimal.ONE})
                        .tipoDeComprobante(TipoDeComprobante.FACTURA_B)
                        .build();
        when(facturaService.calcularResultadosFactura(nuevosResultadosComprobanteDTO))
                .thenReturn(resultados);
        assertEquals(
                resultados, facturaController.calcularResultadosFactura(nuevosResultadosComprobanteDTO));
        nuevosResultadosComprobanteDTO =
                NuevosResultadosComprobanteDTO.builder()
                        .importe(new BigDecimal[]{BigDecimal.TEN, BigDecimal.ONE})
                        .tipoDeComprobante(TipoDeComprobante.FACTURA_C)
                        .build();
        when(facturaService.calcularResultadosFactura(nuevosResultadosComprobanteDTO))
                .thenReturn(resultados);
        assertEquals(
                resultados, facturaController.calcularResultadosFactura(nuevosResultadosComprobanteDTO));
    }

    @Test
    void shouldGuardarFacturaCompra() {
        NuevaFacturaCompraDTO nuevaFacturaCompra = NuevaFacturaCompraDTO.builder().idProveedor(1L).build();
        List<FacturaCompra> facturasCompra = new ArrayList<>();
        FacturaCompra facturaCompra = new FacturaCompra();
        facturasCompra.add(facturaCompra);
        when(facturaCompraService.guardar(any())).thenReturn(facturasCompra);
        var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
        when(authService.getClaimsDelToken("headers")).thenReturn(claims);
        assertEquals(
                facturasCompra,
                facturaController.guardarFacturaCompra(nuevaFacturaCompra, "headers"));
    }

    @Test
    void shouldGetTipoFacturaCompra() {
        Sucursal sucursal = new Sucursal();
        Proveedor proveedor = new Proveedor();
        when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
        when(proveedorService.getProveedorNoEliminadoPorId(2L)).thenReturn(proveedor);
        when(facturaCompraService.getTiposDeComprobanteCompra(sucursal, proveedor))
                .thenReturn(new TipoDeComprobante[]{TipoDeComprobante.FACTURA_A});
        assertEquals(
                new TipoDeComprobante[]{TipoDeComprobante.FACTURA_A}[0],
                facturaController.getTipoFacturaCompra(1L, 2L)[0]);
    }

    @Test
    void shouldCalcularRenglonesCompra() {
        var nuevosRenglonesFacturaDTO = new ArrayList<NuevoRenglonFacturaDTO>();
        var renglonesFacturas = new ArrayList<RenglonFactura>();
        when(facturaService.calcularRenglones(TipoDeComprobante.FACTURA_A, Movimiento.COMPRA, nuevosRenglonesFacturaDTO))
                .thenReturn(renglonesFacturas);
        var renglones = facturaController.calcularRenglonesCompra(nuevosRenglonesFacturaDTO, TipoDeComprobante.FACTURA_A);
        assertNotNull(renglones);
    }

    @Test
    void shouldCalcularTotalFacturadoCompra() {
        BusquedaFacturaCompraCriteria busquedaFacturaCompraCriteria =
                BusquedaFacturaCompraCriteria.builder().build();
        when(facturaCompraService.calcularTotalFacturadoCompra(busquedaFacturaCompraCriteria))
                .thenReturn(BigDecimal.TEN);
        assertEquals(
                BigDecimal.TEN,
                facturaController.calcularTotalFacturadoCompra(busquedaFacturaCompraCriteria));
    }

    @Test
    void shouldCalcularTotalIvaCompra() {
        BusquedaFacturaCompraCriteria busquedaFacturaCompraCriteria =
                BusquedaFacturaCompraCriteria.builder().build();
        when(facturaCompraService.calcularIvaCompra(busquedaFacturaCompraCriteria))
                .thenReturn(BigDecimal.TEN);
        assertEquals(
                BigDecimal.TEN,
                facturaController.calcularTotalIvaCompra(busquedaFacturaCompraCriteria));
    }

    @Test
    void shouldGuardarFacturaVenta() {
        var nuevaFacturaVentaDTO = NuevaFacturaVentaDTO.builder().tipoDeComprobante(TipoDeComprobante.PEDIDO).build();
        assertThrows(
                BusinessServiceException.class,
                () -> facturaController.guardarFacturaVenta(nuevaFacturaVentaDTO, 1L, "headers"));
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
        facturaController.guardarFacturaVenta(nuevaFacturaVenta2DTO, 1L, "headers");
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
        when(facturaVentaService.buscarFacturaVenta(criteria)).thenReturn(pagina);
        assertEquals(pagina, facturaController.buscarFacturaVenta(criteria));
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
                facturaController.getTipoFacturaVenta(1L, 1L, "headers")[0]);
        claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("VENDEDOR")));
        when(authService.getClaimsDelToken("headers")).thenReturn(claims);
        when(facturaVentaService.getTiposDeComprobanteVenta(any(), any(), any()))
                .thenReturn(new TipoDeComprobante[] {TipoDeComprobante.PEDIDO});
        assertEquals(
                TipoDeComprobante.PEDIDO,
                facturaController.getTipoFacturaVenta(1L, 1L, "headers")[0]);
    }

    @Test
    void shouldGetReporteFacturaVenta() {
        FacturaVenta factura = new FacturaVenta();
        when(facturaService.getFacturaNoEliminadaPorId(1L)).thenReturn(factura);
        byte[] bytes = ("bytes[]").getBytes();
        when(facturaVentaService.getReporteFacturaVenta(factura)).thenReturn(bytes);
        assertEquals(bytes, facturaController.getReporteFacturaVenta(1L).getBody());
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
                facturaController.getRenglonesPedidoParaFacturar(1L, TipoDeComprobante.FACTURA_A));
    }

    @Test
    void shouldCalcularRenglonesVenta() {
        List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO = new ArrayList<>();
        List<RenglonFactura> renglonesFacturas = new ArrayList<>();
        when(facturaService.calcularRenglones(TipoDeComprobante.FACTURA_A, Movimiento.VENTA, nuevosRenglonesFacturaDTO))
                .thenReturn(renglonesFacturas);
        assertNotNull(facturaController.calcularRenglonesVenta(nuevosRenglonesFacturaDTO, TipoDeComprobante.FACTURA_A));
    }

    @Test
    void shouldCalcularTotalFacturadoVenta() {
        var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
        when(authService.getClaimsDelToken("headers")).thenReturn(claims);
        var busquedaFacturaVentaCriteria = BusquedaFacturaVentaCriteria.builder().build();
        when(facturaVentaService.calcularTotalFacturadoVenta(busquedaFacturaVentaCriteria)).thenReturn(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, facturaController.calcularTotalFacturadoVenta(busquedaFacturaVentaCriteria));
    }

    @Test
    void shouldCalcularIvaVenta() {
        var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
        when(authService.getClaimsDelToken("headers")).thenReturn(claims);
        var busquedaFacturaVentaCriteria = BusquedaFacturaVentaCriteria.builder().build();
        when(facturaVentaService.calcularIvaVenta(busquedaFacturaVentaCriteria)).thenReturn(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, facturaController.calcularIvaVenta(busquedaFacturaVentaCriteria));
    }

    @Test
    void shouldCalcularGananciaTotal() {
        var claims = new DefaultClaims(Map.of("idUsuario", 1L, "roles", List.of("ADMINISTRADOR")));
        when(authService.getClaimsDelToken("headers")).thenReturn(claims);
        var busquedaFacturaVentaCriteria = BusquedaFacturaVentaCriteria.builder().build();
        when(facturaVentaService.calcularGananciaTotal(busquedaFacturaVentaCriteria)).thenReturn(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, facturaController.calcularGananciaTotal(busquedaFacturaVentaCriteria));
    }

    @Test
    void shouldEnviarFacturaVentaPorEmail() {
        facturaController.enviarFacturaVentaPorEmail(1L);
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
