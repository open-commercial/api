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
import sic.modelo.dto.NuevaOrdenDeCarritoDeCompraDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class CarritoCompraController {

  private final ICarritoCompraService carritoCompraService;
  private final IPedidoService pedidoService;
  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IPagoMercadoPagoService pagoMercadoPagoService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public CarritoCompraController(
      ICarritoCompraService carritoCompraService,
      IPedidoService pedidoService,
      IEmpresaService empresaService,
      IUsuarioService usuarioService,
      IClienteService clienteService,
      IPagoMercadoPagoService pagoMercadoPagoService) {
    this.carritoCompraService = carritoCompraService;
    this.pedidoService = pedidoService;
    this.empresaService = empresaService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.pagoMercadoPagoService = pagoMercadoPagoService;
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
      @RequestBody NuevaOrdenDeCarritoDeCompraDTO nuevaOrdenDeCarritoDeCompraDTO) {
    CarritoCompraDTO carritoCompraDTO =
        carritoCompraService.getCarritoCompra(
            nuevaOrdenDeCarritoDeCompraDTO.getIdUsuario(),
            nuevaOrdenDeCarritoDeCompraDTO.getIdCliente());
    Pedido pedido = new Pedido();
    pedido.setCliente(
        clienteService.getClienteNoEliminadoPorId(nuevaOrdenDeCarritoDeCompraDTO.getIdCliente()));
    pedido.setObservaciones(nuevaOrdenDeCarritoDeCompraDTO.getObservaciones());
    pedido.setSubTotal(carritoCompraDTO.getSubtotal());
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setRecargoNeto(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(carritoCompraDTO.getBonificacionPorcentaje());
    pedido.setDescuentoNeto(carritoCompraDTO.getBonificacionNeto());
    pedido.setTotalActual(carritoCompraDTO.getTotal());
    pedido.setTotalEstimado(pedido.getTotalActual());
    pedido.setEmpresa(
        empresaService.getEmpresaPorId(nuevaOrdenDeCarritoDeCompraDTO.getIdEmpresa()));
    pedido.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(nuevaOrdenDeCarritoDeCompraDTO.getIdUsuario()));
    Pageable pageable =
        PageRequest.of(0, Integer.MAX_VALUE, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    List<ItemCarritoCompra> items =
        carritoCompraService
            .getItemsDelCaritoCompra(
                nuevaOrdenDeCarritoDeCompraDTO.getIdUsuario(),
                nuevaOrdenDeCarritoDeCompraDTO.getIdCliente(),
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
            nuevaOrdenDeCarritoDeCompraDTO.getTipoDeEnvio(),
            nuevaOrdenDeCarritoDeCompraDTO.getIdSucursal());
    carritoCompraService.eliminarTodosLosItemsDelUsuario(
        nuevaOrdenDeCarritoDeCompraDTO.getIdUsuario());
    if (nuevaOrdenDeCarritoDeCompraDTO.getPago() != null) {
      pagoMercadoPagoService.crearNuevoPago(
          nuevaOrdenDeCarritoDeCompraDTO.getPago(),
          p.getCliente(),
          p.getTotalEstimado().floatValue());
    }
    return p;
  }
}
