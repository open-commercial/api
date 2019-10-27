package sic.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Pedido;
import sic.modelo.dto.CarritoCompraDTO;
import sic.modelo.dto.NuevaOrdenDeCompraDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class CarritoCompraController {

  private final ICarritoCompraService carritoCompraService;

  @Autowired
  public CarritoCompraController(
      ICarritoCompraService carritoCompraService) {
    this.carritoCompraService = carritoCompraService;
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/clientes/{idCliente}")
  public CarritoCompraDTO getCarritoCompraDelUsuario(
      @PathVariable long idUsuario, @PathVariable long idCliente) {
    return carritoCompraService.getCarritoCompra(idUsuario, idCliente);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/clientes/{idCliente}/items")
  public Page<ItemCarritoCompra> getAllItemsDelUsuario(
      @PathVariable long idUsuario,
      @PathVariable long idCliente,
      @RequestParam(required = false) Integer pagina) {
    if (pagina == null || pagina < 0) pagina = 0;
    return carritoCompraService.getItemsDelCaritoCompra(idUsuario, idCliente, pagina);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/productos/{idProducto}")
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(@PathVariable long idUsuario, @PathVariable long idProducto) {
    return carritoCompraService.getItemCarritoDeCompraDeUsuarioPorIdProducto(idUsuario, idProducto);
  }

  @DeleteMapping("/carrito-compra/usuarios/{idUsuario}/productos/{idProducto}")
  public void eliminarItem(@PathVariable long idUsuario, @PathVariable long idProducto) {
    carritoCompraService.eliminarItemDelUsuario(idUsuario, idProducto);
  }

  @DeleteMapping("/carrito-compra/usuarios/{idUsuario}")
  public void eliminarTodosLosItems(@PathVariable long idUsuario) {
    carritoCompraService.eliminarTodosLosItemsDelUsuario(idUsuario);
  }

  @PostMapping("/carrito-compra/usuarios/{idUsuario}/productos/{idProducto}")
  public void agregarOrModificarItem(
      @PathVariable long idUsuario,
      @PathVariable long idProducto,
      @RequestParam BigDecimal cantidad) {
    carritoCompraService.agregarOrModificarItem(idUsuario, idProducto, cantidad);
  }

  @PostMapping("/carrito-compra")
  public Pedido generarPedidoConItemsDelCarrito(
      @RequestBody NuevaOrdenDeCompraDTO nuevaOrdenDeCompraDTO) {
    return carritoCompraService.crearPedido(nuevaOrdenDeCompraDTO);
  }
}
