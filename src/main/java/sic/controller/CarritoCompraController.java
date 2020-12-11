package sic.controller;

import java.math.BigDecimal;
import java.util.List;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Pedido;
import sic.modelo.dto.CarritoCompraDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class CarritoCompraController {

  private final ICarritoCompraService carritoCompraService;
  private final IAuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public CarritoCompraController(
      ICarritoCompraService carritoCompraService, IAuthService authService) {
    this.carritoCompraService = carritoCompraService;
    this.authService = authService;
  }

  @GetMapping("/carrito-compra")
  public CarritoCompraDTO getCarritoCompraDelUsuario(
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return carritoCompraService.getCarritoCompra(idUsuarioLoggedIn);
  }

  @GetMapping("/carrito-compra/items")
  public Page<ItemCarritoCompra> getAllItemsDelUsuario(
      @RequestParam(required = false) Integer pagina,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    if (pagina == null || pagina < 0) pagina = 0;
    return carritoCompraService.getItemsDelCaritoCompra(idUsuarioLoggedIn, pagina, null);
  }

  @GetMapping("/carrito-compra/productos/{idProducto}")
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(
      @PathVariable long idProducto, @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return carritoCompraService.getItemCarritoDeCompraDeUsuarioPorIdProducto(
        idUsuarioLoggedIn, idProducto);
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

  @GetMapping("/carrito-compra/disponibilidad-stock")
  public List<ProductoFaltanteDTO> getProductosDelCarritoSinStockDisponible(
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return carritoCompraService.getProductosDelCarritoSinStockDisponible(idUsuarioLoggedIn);
  }
}
