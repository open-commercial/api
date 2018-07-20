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
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Pedido;
import sic.modelo.RenglonPedido;
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
    if (tamanio == null || tamanio <= 0) {
      tamanio = TAMANIO_PAGINA_DEFAULT;
    }
    if (pagina == null || pagina < 0) {
      pagina = 0;
    }
    Pageable pageable =
        new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    return carritoCompraService.getAllItemsDelUsuario(idUsuario, pageable);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/total")
  @ResponseStatus(HttpStatus.OK)
  public BigDecimal getTotal(@PathVariable long idUsuario) {
    return carritoCompraService.getTotal(idUsuario);
  }

  @GetMapping("/carrito-compra/usuarios/{idUsuario}/cantidad-articulos")
  @ResponseStatus(HttpStatus.OK)
  public BigDecimal getCantArticulos(@PathVariable long idUsuario) {
    return carritoCompraService.getCantArticulos(idUsuario);
  }

  @DeleteMapping("/carrito-compra/usuarios/{idUsuario}/productos/{idProducto}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminarItem(@PathVariable long idUsuario, @PathVariable long idProducto) {
    carritoCompraService.eliminarItem(idUsuario, idProducto);
  }

  @DeleteMapping("/carrito-compra/usuarios/{idUsuario}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminarTodosLosItems(@PathVariable long idUsuario) {
    carritoCompraService.eliminarTodosLosItems(idUsuario);
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
      @RequestBody Pedido pedido) {
    pedido.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    pedido.setUsuario(usuarioService.getUsuarioPorId(idUsuario));
    pedido.setCliente(clienteService.getClientePorId(idCliente));
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
                    new RenglonPedido(
                        0,
                        i.getProducto(),
                        i.getCantidad(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        i.getImporte())));
    Pedido p = pedidoService.guardar(pedido);
    carritoCompraService.eliminarTodosLosItems(idUsuario);
    return p;
  }
}
