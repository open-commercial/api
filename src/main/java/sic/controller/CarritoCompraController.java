package sic.controller;

import java.math.BigDecimal;
import java.util.List;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.entity.ItemCarritoCompra;
import sic.entity.Pedido;
import sic.dto.CarritoCompraDTO;
import sic.dto.NuevaOrdenDePagoDTO;
import sic.dto.ProductoFaltanteDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class CarritoCompraController {

  private final ICarritoCompraService carritoCompraService;
  private final IProductoService productoService;
  private final IAuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public CarritoCompraController(
      ICarritoCompraService carritoCompraService,
      IProductoService productoService,
      IAuthService authService) {
    this.carritoCompraService = carritoCompraService;
    this.productoService = productoService;
    this.authService = authService;
  }

  @GetMapping("/carrito-compra")
  public CarritoCompraDTO getCarritoCompraDelUsuario(
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return carritoCompraService.getCarritoCompra(idUsuarioLoggedIn);
  }

  @GetMapping("/carrito-compra/items/sucursales/{idSucursal}")
  public Page<ItemCarritoCompra> getAllItemsDelUsuario(
      @RequestParam(required = false) Integer pagina,
      @PathVariable long idSucursal,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    if (pagina == null || pagina < 0) pagina = 0;
    Page<ItemCarritoCompra> itemsCarritoCompra =
        carritoCompraService.getItemsDelCaritoCompra(idUsuarioLoggedIn, pagina, true);
    itemsCarritoCompra.forEach(
        itemCarritoCompra ->
            productoService.calcularCantidadEnSucursalesDisponible(
                itemCarritoCompra.getProducto(), idSucursal));
    return itemsCarritoCompra;
  }

  @GetMapping("/carrito-compra/productos/{idProducto}/sucursales/{idSucursal}")
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(
      @PathVariable long idProducto,
      @PathVariable long idSucursal,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return carritoCompraService.getItemCarritoDeCompraDeUsuarioPorIdProducto(
        idUsuarioLoggedIn, idProducto, idSucursal);
  }

  @DeleteMapping("/carrito-compra/productos/{idProducto}")
  public void eliminarItem(
      @PathVariable long idProducto, @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    carritoCompraService.eliminarItemDelUsuario(idUsuarioLoggedIn, idProducto);
  }

  @DeleteMapping("/carrito-compra")
  public void eliminarTodosLosItems(@RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    carritoCompraService.eliminarTodosLosItemsDelUsuario(idUsuarioLoggedIn);
  }

  @PostMapping("/carrito-compra/productos/{idProducto}")
  public void agregarOrModificarItem(
      @PathVariable long idProducto,
      @RequestParam BigDecimal cantidad,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    carritoCompraService.agregarOrModificarItem(idUsuarioLoggedIn, idProducto, cantidad);
  }

  @PostMapping("/carrito-compra")
  public Pedido generarPedidoConItemsDelCarrito(
      @RequestBody NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return carritoCompraService.crearPedido(nuevaOrdenDePagoDTO, idUsuarioLoggedIn);
  }

  @GetMapping("/carrito-compra/disponibilidad-stock/sucursales/{idSucursal}")
  public List<ProductoFaltanteDTO> getProductosDelCarritoSinStockDisponible(
      @PathVariable Long idSucursal,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return carritoCompraService.getProductosDelCarritoSinStockDisponible(idUsuarioLoggedIn, idSucursal);
  }
}
