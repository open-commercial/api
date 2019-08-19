package sic.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  private final IPedidoService pedidoService;
  private final ISucursalService sucursalService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public CarritoCompraController(
      ICarritoCompraService carritoCompraService,
      IPedidoService pedidoService,
      ISucursalService sucursalService,
      IUsuarioService usuarioService,
      IClienteService clienteService) {
    this.carritoCompraService = carritoCompraService;
    this.pedidoService = pedidoService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/clientes/{idCliente}")
  public CarritoCompraDTO getCarritoCompraDelUsuario(
      @PathVariable long idUsuario, @PathVariable long idCliente) {
    return carritoCompraService.getCarritoCompra(idUsuario, idCliente);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/carrito-compra/usuarios/{idUsuario}/clientes/{idCliente}/items")
  public Page<ItemCarritoCompra> getAllItemsDelUsuario(
      @PathVariable long idUsuario,
      @PathVariable long idCliente,
      @RequestParam(required = false) Integer pagina) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable =
        PageRequest.of(
            pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    return carritoCompraService.getItemsDelCaritoCompra(idUsuario, idCliente, pageable);
  }

  @JsonView(Views.Public.class)
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
    CarritoCompraDTO carritoCompraDTO =
        carritoCompraService.getCarritoCompra(
            nuevaOrdenDeCompraDTO.getIdUsuario(),
            nuevaOrdenDeCompraDTO.getIdCliente());
    Pedido pedido = new Pedido();
    pedido.setCliente(
        clienteService.getClienteNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdCliente()));
    pedido.setObservaciones(nuevaOrdenDeCompraDTO.getObservaciones());
    pedido.setSubTotal(carritoCompraDTO.getSubtotal());
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setRecargoNeto(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(carritoCompraDTO.getBonificacionPorcentaje());
    pedido.setDescuentoNeto(carritoCompraDTO.getBonificacionNeto());
    pedido.setTotalActual(carritoCompraDTO.getTotal());
    pedido.setTotalEstimado(pedido.getTotalActual());
    pedido.setSucursal(
        sucursalService.getSucursalPorId(nuevaOrdenDeCompraDTO.getIdSucursal()));
    pedido.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdUsuario()));
    Pageable pageable =
        PageRequest.of(0, Integer.MAX_VALUE, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    List<ItemCarritoCompra> items =
        carritoCompraService
            .getItemsDelCaritoCompra(
                nuevaOrdenDeCompraDTO.getIdUsuario(),
                nuevaOrdenDeCompraDTO.getIdCliente(),
                pageable)
            .getContent();
    pedido.setRenglones(new ArrayList<>());
    items.forEach(
        i ->
            pedido
                .getRenglones()
                .add(
                    pedidoService.calcularRenglonPedido(
                        i.getProducto().getIdProducto(), i.getCantidad(), BigDecimal.ZERO)));
    Pedido p =
        pedidoService.guardar(
            pedido,
            nuevaOrdenDeCompraDTO.getTipoDeEnvio(),
            nuevaOrdenDeCompraDTO.getIdSucursal());
    carritoCompraService.eliminarTodosLosItemsDelUsuario(
        nuevaOrdenDeCompraDTO.getIdUsuario());
    return p;
  }
}
