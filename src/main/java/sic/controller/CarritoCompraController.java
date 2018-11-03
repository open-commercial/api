package sic.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sic.modelo.Cliente;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Pedido;
import sic.service.ICarritoCompraService;
import sic.service.IClienteService;
import sic.service.IEmpresaService;
import sic.service.IPedidoService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class CarritoCompraController {

  private final ICarritoCompraService carritoCompraService;
  private final IPedidoService pedidoService;
  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;

  @Autowired
  public CarritoCompraController(
    ICarritoCompraService carritoCompraService,
    IPedidoService pedidoService,
    IEmpresaService empresaService,
    IUsuarioService usuarioService,
    IClienteService clienteService) {
    this.carritoCompraService = carritoCompraService;
    this.pedidoService = pedidoService;
    this.empresaService = empresaService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}")
  @ResponseStatus(HttpStatus.OK)
  public Page<ItemCarritoCompra> getAllItemsDelUsuario(
    @PathVariable long idUsuario,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) Integer tamanio) {
    final int TAMANIO_PAGINA_DEFAULT = 10;
    if (tamanio == null || tamanio <= 0) tamanio = TAMANIO_PAGINA_DEFAULT;
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable =
      new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    return carritoCompraService.getAllItemsDelUsuario(idUsuario, pageable);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/subtotal")
  @ResponseStatus(HttpStatus.OK)
  public BigDecimal getSubtotal(@PathVariable long idUsuario) {
    return carritoCompraService.getSubtotal(idUsuario);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/clientes/{idCliente}/total")
  @ResponseStatus(HttpStatus.OK)
  public BigDecimal getTotal(@PathVariable long idUsuario,
                             @PathVariable long idCliente) {
    return carritoCompraService.getTotal(idUsuario, idCliente);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/cantidad-articulos")
  @ResponseStatus(HttpStatus.OK)
  public BigDecimal getCantArticulos(@PathVariable long idUsuario) {
    return carritoCompraService.getCantArticulos(idUsuario);
  }

  @DeleteMapping("/carrito-compra/usuarios/{idUsuario}/productos/{idProducto}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminarItem(@PathVariable long idUsuario, @PathVariable long idProducto) {
    carritoCompraService.eliminarItemDelUsuario(idUsuario, idProducto);
  }

  @DeleteMapping("/carrito-compra/usuarios/{idUsuario}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminarTodosLosItems(@PathVariable long idUsuario) {
    carritoCompraService.eliminarTodosLosItemsDelUsuario(idUsuario);
  }

  @PostMapping("/carrito-compra/usuarios/{idUsuario}/productos/{idProducto}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void agregarOrModificarItem(
    @PathVariable long idUsuario,
    @PathVariable long idProducto,
    @RequestParam BigDecimal cantidad) {
    carritoCompraService.agregarOrModificarItem(idUsuario, idProducto, cantidad);
  }

  @PutMapping("/carrito-compra/usuarios/{idUsuario}/productos/{idProducto}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void modificarCantidadItem(
    @PathVariable long idUsuario,
    @PathVariable long idProducto,
    @RequestParam BigDecimal cantidad) {
    carritoCompraService.modificarCantidadItem(idUsuario, idProducto, cantidad);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/cantidad-renglones")
  @ResponseStatus(HttpStatus.OK)
  public long getCantRenglones(@PathVariable long idUsuario) {
    return carritoCompraService.getCantRenglones(idUsuario);
  }

  @PostMapping("/carrito-compra")
  @ResponseStatus(HttpStatus.CREATED)
  public Pedido generarPedidoConItemsDelCarrito(
    @RequestParam Long idEmpresa,
    @RequestParam Long idUsuario,
    @RequestParam Long idCliente,
    @RequestBody(required = false) String observaciones) {
    Cliente cliente = clienteService.getClientePorId(idCliente);
    Pedido pedido = new Pedido();
    pedido.setObservaciones(observaciones);
    pedido.setSubTotal(carritoCompraService.getSubtotal(idUsuario));
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setRecargoNeto(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(cliente.getBonificacion());
    pedido.setDescuentoNeto(carritoCompraService.getBonificacionNeta(idUsuario, cliente.getBonificacion()));
    pedido.setTotalActual(carritoCompraService.getTotal(idUsuario, idCliente));
    pedido.setTotalEstimado(pedido.getTotalActual());
    pedido.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    pedido.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
    pedido.setCliente(cliente);
    Pageable pageable =
      new PageRequest(0, Integer.MAX_VALUE, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    List<ItemCarritoCompra> items =
      carritoCompraService.getAllItemsDelUsuario(idUsuario, pageable).getContent();
    pedido.setRenglones(new ArrayList<>());
    items.forEach(
      i ->
        pedido
          .getRenglones()
          .add(
            pedidoService.calcularRenglonPedido(
              i.getProducto().getId_Producto(), i.getCantidad(), BigDecimal.ZERO)));
    Pedido p = pedidoService.guardar(pedido);
    carritoCompraService.eliminarTodosLosItemsDelUsuario(idUsuario);
    return p;
  }
}
