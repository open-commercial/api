package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.opencommercial.model.ItemCarritoCompra;
import org.opencommercial.model.Pedido;
import org.opencommercial.model.dto.CarritoCompraDTO;
import org.opencommercial.model.dto.NuevaOrdenDePagoDTO;
import org.opencommercial.model.dto.ProductoFaltanteDTO;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.CarritoCompraService;
import org.opencommercial.service.ProductoService;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class CarritoCompraController {

  private final CarritoCompraService carritoCompraService;
  private final ProductoService productoService;
  private final AuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public CarritoCompraController(CarritoCompraService carritoCompraService,
                                 ProductoService productoService,
                                 AuthService authService) {
    this.carritoCompraService = carritoCompraService;
    this.productoService = productoService;
    this.authService = authService;
  }

  @GetMapping("/api/v1/carrito-compra")
  public CarritoCompraDTO getCarritoCompraDelUsuario(@RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return carritoCompraService.getCarritoCompra(idUsuarioLoggedIn);
  }

  @GetMapping("/api/v1/carrito-compra/items/sucursales/{idSucursal}")
  public Page<ItemCarritoCompra> getAllItemsDelUsuario(@RequestParam(required = false) Integer pagina,
                                                       @PathVariable long idSucursal,
                                                       @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    if (pagina == null || pagina < 0) pagina = 0;
    Page<ItemCarritoCompra> itemsCarritoCompra =
            carritoCompraService.getItemsDelCaritoCompra(idUsuarioLoggedIn, pagina, true);
    itemsCarritoCompra.forEach(
            itemCarritoCompra ->
                    productoService.calcularCantidadEnSucursalesDisponible(
                            itemCarritoCompra.getProducto(), idSucursal));
    return itemsCarritoCompra;
  }

  @GetMapping("/api/v1/carrito-compra/productos/{idProducto}/sucursales/{idSucursal}")
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(
          @PathVariable long idProducto,
          @PathVariable long idSucursal,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return carritoCompraService.getItemCarritoDeCompraDeUsuarioPorIdProducto(
            idUsuarioLoggedIn, idProducto, idSucursal);
  }

  @DeleteMapping("/api/v1/carrito-compra/productos/{idProducto}")
  public void eliminarItem(@PathVariable long idProducto,
                           @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    carritoCompraService.eliminarItemDelUsuario(idUsuarioLoggedIn, idProducto);
  }

  @DeleteMapping("/api/v1/carrito-compra")
  public void eliminarTodosLosItems(@RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    carritoCompraService.eliminarTodosLosItemsDelUsuario(idUsuarioLoggedIn);
  }

  @PostMapping("/api/v1/carrito-compra/productos/{idProducto}")
  public void agregarOrModificarItem(@PathVariable long idProducto,
                                     @RequestParam BigDecimal cantidad,
                                     @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    carritoCompraService.agregarOrModificarItem(idUsuarioLoggedIn, idProducto, cantidad);
  }

  @PostMapping("/api/v1/carrito-compra")
  public Pedido generarPedidoConItemsDelCarrito(@RequestBody NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO,
                                                @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return carritoCompraService.crearPedido(nuevaOrdenDePagoDTO, idUsuarioLoggedIn);
  }

  @GetMapping("/api/v1/carrito-compra/disponibilidad-stock/sucursales/{idSucursal}")
  public List<ProductoFaltanteDTO> getProductosDelCarritoSinStockDisponible(
          @PathVariable Long idSucursal,
          @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return carritoCompraService.getProductosDelCarritoSinStockDisponible(idUsuarioLoggedIn, idSucursal);
  }
}
