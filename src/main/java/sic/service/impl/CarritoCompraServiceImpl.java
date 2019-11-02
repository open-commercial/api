package sic.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.modelo.dto.CarritoCompraDTO;
import sic.modelo.dto.NuevaOrdenDeCompraDTO;
import sic.repository.CarritoCompraRepository;
import sic.service.*;

@Service
@Transactional
public class CarritoCompraServiceImpl implements ICarritoCompraService {

  private final CarritoCompraRepository carritoCompraRepository;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IProductoService productoService;
  private final IEmpresaService empresaService;
  private final IPedidoService pedidoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private static final BigDecimal CIEN = new BigDecimal("100");

  @Autowired
  public CarritoCompraServiceImpl(
    CarritoCompraRepository carritoCompraRepository,
    IUsuarioService usuarioService,
    IClienteService clienteService,
    IProductoService productoService,
    IEmpresaService empresaService,
    IPedidoService pedidoService) {
    this.carritoCompraRepository = carritoCompraRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.empresaService = empresaService;
    this.pedidoService = pedidoService;
  }

  @Override
  public CarritoCompraDTO getCarritoCompra(long idUsuario, long idCliente) {
    CarritoCompraDTO carritoCompraDTO = new CarritoCompraDTO();
    BigDecimal cantArticulos = carritoCompraRepository.getCantArticulos(idUsuario);
    carritoCompraDTO.setCantRenglones(carritoCompraRepository.getCantRenglones(idUsuario));
    if (cantArticulos == null) cantArticulos = BigDecimal.ZERO;
    carritoCompraDTO.setCantArticulos(cantArticulos);
    BigDecimal subtotal = carritoCompraRepository.calcularSubtotal(idUsuario);
    if (subtotal == null) subtotal = BigDecimal.ZERO;
    carritoCompraDTO.setSubtotal(subtotal);
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(idCliente);
    carritoCompraDTO.setBonificacionPorcentaje(cliente.getBonificacion());
    carritoCompraDTO.setBonificacionNeto(
      subtotal.multiply(cliente.getBonificacion()).divide(CIEN, RoundingMode.HALF_UP));
    carritoCompraDTO.setTotal(subtotal.subtract(carritoCompraDTO.getBonificacionNeto()));
    return carritoCompraDTO;
  }

  @Override
  public Page<ItemCarritoCompra> getItemsDelCaritoCompra(
      long idUsuario, long idCliente, int pagina) {
    Pageable pageable =
        PageRequest.of(
            pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    Page<ItemCarritoCompra> items =
        carritoCompraRepository.findAllByUsuario(
            usuarioService.getUsuarioNoEliminadoPorId(idUsuario), pageable);
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(idCliente);
    BigDecimal bonificacion = cliente.getBonificacion();
    items.forEach(
        i -> {
          i.getProducto()
              .setPrecioBonificado(
                  i.getProducto()
                      .getPrecioLista()
                      .multiply(
                          BigDecimal.ONE.subtract(
                              bonificacion.divide(CIEN, RoundingMode.HALF_UP))));
          i.setImporte(i.getProducto().getPrecioLista().multiply(i.getCantidad()));
          i.setImporteBonificado(i.getProducto().getPrecioBonificado().multiply(i.getCantidad()));
          i.getProducto().setHayStock(i.getProducto().getCantidad().compareTo(BigDecimal.ZERO) > 0);
        });
    return items;
  }

  @Override
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(
      long idUsuario, long idProducto) {
    ItemCarritoCompra itemCarritoCompra =
        this.carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
    if (itemCarritoCompra != null) {
      itemCarritoCompra
          .getProducto()
          .setHayStock(
              itemCarritoCompra.getProducto().getCantidad().compareTo(BigDecimal.ZERO) > 0);
    }
    return itemCarritoCompra;
  }

  @Override
  public void eliminarItemDelUsuario(long idUsuario, long idProducto) {
    carritoCompraRepository.eliminarItemDelUsuario(idUsuario, idProducto);
  }

  @Override
  public void eliminarItem(long idProducto) {
    carritoCompraRepository.eliminarItem(idProducto);
  }

  @Override
  public void eliminarTodosLosItemsDelUsuario(long idUsuario) {
    carritoCompraRepository.eliminarTodosLosItemsDelUsuario(idUsuario);
  }

  @Override
  public void agregarOrModificarItem(long idUsuario, long idProducto, BigDecimal cantidad) {
    Usuario usuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    ItemCarritoCompra item = carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
    if (item == null) {
      BigDecimal importe = producto.getPrecioLista().multiply(cantidad);
      ItemCarritoCompra itemCC =
        carritoCompraRepository.save(
          new ItemCarritoCompra(null, cantidad, producto, importe, null, usuario));
      logger.warn("Nuevo item de carrito de compra agregado: {}", itemCC);
    } else {
      if (cantidad.compareTo(BigDecimal.ZERO) < 0) {
        item.setCantidad(BigDecimal.ZERO);
      } else {
        item.setCantidad(cantidad);
      }
      item.setImporte(producto.getPrecioLista().multiply(cantidad));
      ItemCarritoCompra itemCC = carritoCompraRepository.save(item);
      logger.warn("Item de carrito de compra modificado: {}", itemCC);
    }
  }

  @Override
  public Pedido crearPedido(NuevaOrdenDeCompraDTO nuevaOrdenDeCompraDTO) {
    CarritoCompraDTO carritoCompraDTO =
        this.getCarritoCompra(
            nuevaOrdenDeCompraDTO.getIdUsuario(), nuevaOrdenDeCompraDTO.getIdCliente());
    Pedido pedido = new Pedido();
    pedido.setCliente(
        clienteService.getClienteNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdCliente()));
    pedido.setObservaciones(nuevaOrdenDeCompraDTO.getObservaciones());
    pedido.setSubTotal(carritoCompraDTO.getSubtotal());
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setRecargoNeto(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(carritoCompraDTO.getBonificacionPorcentaje());
    pedido.setDescuentoNeto(carritoCompraDTO.getBonificacionNeto());
    pedido.setTotalEstimado(carritoCompraDTO.getTotal());
    pedido.setEmpresa(empresaService.getEmpresaPorId(nuevaOrdenDeCompraDTO.getIdEmpresa()));
    pedido.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdUsuario()));
    List<ItemCarritoCompra> items = carritoCompraRepository.findAllByUsuarioOrderByIdItemCarritoCompraDesc(pedido.getUsuario());
    pedido.setRenglones(new ArrayList<>());
    items.forEach(
        i ->
            pedido
                .getRenglones()
                .add(
                    pedidoService.calcularRenglonPedido(
                        i.getProducto().getIdProducto(), i.getCantidad(), BigDecimal.ZERO)));
    pedido.setFecha(LocalDateTime.now());
    Pedido p =
        pedidoService.guardar(
            pedido, nuevaOrdenDeCompraDTO.getTipoDeEnvio(), nuevaOrdenDeCompraDTO.getIdSucursal());
    this.eliminarTodosLosItemsDelUsuario(nuevaOrdenDeCompraDTO.getIdUsuario());
    return p;
  }
}
