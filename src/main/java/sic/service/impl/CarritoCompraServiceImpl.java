package sic.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Producto;
import sic.modelo.Usuario;
import sic.modelo.dto.CarritoCompraDTO;
import sic.repository.CarritoCompraRepository;
import sic.service.ICarritoCompraService;
import sic.service.IClienteService;
import sic.service.IProductoService;
import sic.service.IUsuarioService;

@Service
@Transactional
public class CarritoCompraServiceImpl implements ICarritoCompraService {

  private final CarritoCompraRepository carritoCompraRepository;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IProductoService productoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final BigDecimal CIEN = new BigDecimal("100");

  @Autowired
  public CarritoCompraServiceImpl(
    CarritoCompraRepository carritoCompraRepository,
    IUsuarioService usuarioService,
    IClienteService clienteService,
    IProductoService productoService) {
    this.carritoCompraRepository = carritoCompraRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
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
    long idUsuario, long idCliente, Pageable pageable) {
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
      });
    return items;
  }

  @Override
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(
    long idUsuario, long idProducto) {
    return this.carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
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
}
