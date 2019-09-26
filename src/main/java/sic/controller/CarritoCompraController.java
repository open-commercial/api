package sic.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sic.exception.BusinessServiceException;
import sic.modelo.Cliente;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Pedido;
import sic.modelo.Sucursal;
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
  private final IConfiguracionSucursalService configuracionSucursal;
  private final MessageSource messageSource;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public CarritoCompraController(
      ICarritoCompraService carritoCompraService,
      IPedidoService pedidoService,
      ISucursalService sucursalService,
      IUsuarioService usuarioService,
      IClienteService clienteService,
      IConfiguracionSucursalService configuracionSucursal,
      MessageSource messageSource) {
    this.carritoCompraService = carritoCompraService;
    this.pedidoService = pedidoService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.configuracionSucursal = configuracionSucursal;
    this.messageSource = messageSource;
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
    Pageable pageable =
        PageRequest.of(
            pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    return carritoCompraService.getItemsDelCaritoCompra(idUsuario, idCliente, pageable);
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
    CarritoCompraDTO carritoCompraDTO =
        carritoCompraService.getCarritoCompra(
            nuevaOrdenDeCompraDTO.getIdUsuario(),
            nuevaOrdenDeCompraDTO.getIdCliente());
    Pedido pedido = new Pedido();
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdCliente());
    pedido.setCliente(cliente);
    pedido.setObservaciones(nuevaOrdenDeCompraDTO.getObservaciones());
    pedido.setSubTotal(carritoCompraDTO.getSubtotal());
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setRecargoNeto(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(carritoCompraDTO.getBonificacionPorcentaje());
    pedido.setDescuentoNeto(carritoCompraDTO.getBonificacionNeto());
    pedido.setTotalActual(carritoCompraDTO.getTotal());
    pedido.setTotalEstimado(pedido.getTotalActual());
    if (nuevaOrdenDeCompraDTO.getIdSucursal() != null) {
      Sucursal sucursal = sucursalService.getSucursalPorId(nuevaOrdenDeCompraDTO.getIdSucursal());
      if (!configuracionSucursal
          .getConfiguracionSucursal(sucursal)
          .isPuntoDeRetiro()) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_pedido_sucursal_entrega_no_valida", null, Locale.getDefault()));
      }
      pedido.setSucursal(sucursal);
    } else {
      pedido.setSucursal(sucursalService.getSucursalPorId(1L));
    }
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
                        i.getProducto().getIdProducto(), i.getCantidad(), cliente)));
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
