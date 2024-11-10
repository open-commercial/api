package org.opencommercial.controller;

import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaFacturaCompraCriteria;
import org.opencommercial.model.dto.NuevaFacturaCompraDTO;
import org.opencommercial.model.dto.NuevoRenglonFacturaDTO;
import org.opencommercial.service.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class FacturaCompraControllerTest {

  @Mock FacturaCompraServiceImpl facturaCompraService;
  @Mock FacturaServiceImpl facturaService;
  @Mock SucursalServiceImpl sucursalService;
  @Mock ProveedorServiceImpl proveedorService;
  @Mock UsuarioServiceImpl usuarioService;
  @Mock AuthServiceImpl authService;

  @InjectMocks FacturaCompraController facturaCompraController;

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
        facturaCompraController.guardarFacturaCompra(nuevaFacturaCompra, "headers"));
  }

  @Test
  void shouldGetTipoFacturaCompra() {
    Sucursal sucursal = new Sucursal();
    Proveedor proveedor = new Proveedor();
    when(sucursalService.getSucursalPorId(1L)).thenReturn(sucursal);
    when(proveedorService.getProveedorNoEliminadoPorId(2L)).thenReturn(proveedor);
    when(facturaCompraService.getTiposDeComprobanteCompra(sucursal, proveedor))
        .thenReturn(new TipoDeComprobante[] {TipoDeComprobante.FACTURA_A});
    assertEquals(
        new TipoDeComprobante[] {TipoDeComprobante.FACTURA_A}[0],
        facturaCompraController.getTipoFacturaCompra(1L, 2L)[0]);
  }

  @Test
  void shouldCalcularRenglonesCompra() {
    List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO = new ArrayList<>();
    List<RenglonFactura> renglonesFacturas = new ArrayList<>();
    when(facturaService.calcularRenglones(TipoDeComprobante.FACTURA_A, Movimiento.COMPRA, nuevosRenglonesFacturaDTO))
            .thenReturn(renglonesFacturas);
    facturaCompraController.calcularRenglonesCompra(nuevosRenglonesFacturaDTO, TipoDeComprobante.FACTURA_A);
  }

  @Test
  void shouldCalcularTotalFacturadoCompra() {
    BusquedaFacturaCompraCriteria busquedaFacturaCompraCriteria =
        BusquedaFacturaCompraCriteria.builder().build();
    when(facturaCompraService.calcularTotalFacturadoCompra(busquedaFacturaCompraCriteria))
        .thenReturn(BigDecimal.TEN);
    assertEquals(
        BigDecimal.TEN,
        facturaCompraController.calcularTotalFacturadoCompra(busquedaFacturaCompraCriteria));
  }

  @Test
  void shouldCalcularTotalIvaCompra() {
    BusquedaFacturaCompraCriteria busquedaFacturaCompraCriteria =
        BusquedaFacturaCompraCriteria.builder().build();
    when(facturaCompraService.calcularIvaCompra(busquedaFacturaCompraCriteria))
        .thenReturn(BigDecimal.TEN);
    assertEquals(
        BigDecimal.TEN,
        facturaCompraController.calcularTotalIvaCompra(busquedaFacturaCompraCriteria));
  }
}
