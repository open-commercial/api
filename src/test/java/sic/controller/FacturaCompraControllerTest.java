package sic.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sic.domain.Movimiento;
import sic.domain.Rol;
import sic.domain.TipoDeComprobante;
import sic.entity.*;
import sic.entity.criteria.BusquedaFacturaCompraCriteria;
import sic.dto.NuevaFacturaCompraDTO;
import sic.dto.NuevoRenglonFacturaDTO;
import sic.service.impl.*;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    NuevaFacturaCompraDTO nuevaFacturaCompra =
        NuevaFacturaCompraDTO.builder().idProveedor(1L).build();
    List<FacturaCompra> facturasCompra = new ArrayList<>();
    FacturaCompra facturaCompra = new FacturaCompra();
    facturasCompra.add(facturaCompra);
    when(facturaCompraService.guardar(any())).thenReturn(facturasCompra);
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
            .compact();
    Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
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
    when(facturaService.calcularRenglones(
            TipoDeComprobante.FACTURA_A, Movimiento.COMPRA, nuevosRenglonesFacturaDTO))
        .thenReturn(renglonesFacturas);
    facturaCompraController.calcularRenglonesCompra(
        nuevosRenglonesFacturaDTO, TipoDeComprobante.FACTURA_A);
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
