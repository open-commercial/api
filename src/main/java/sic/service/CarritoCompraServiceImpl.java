package sic.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ResourceBundle;
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
import sic.repository.CarritoCompraRepository;
import javax.persistence.EntityNotFoundException;

@Service
@Transactional
public class CarritoCompraServiceImpl implements ICarritoCompraService {

  private final CarritoCompraRepository carritoCompraRepository;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IProductoService productoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
  public Page<ItemCarritoCompra> getAllItemsDelUsuario(long idUsuario, Pageable pageable) {
    Page<ItemCarritoCompra> items = carritoCompraRepository.findAllByUsuario(
        usuarioService.getUsuarioPorId(idUsuario), pageable);
    items.forEach(i -> i.setImporte(i.getProducto().getPrecioLista().multiply(i.getCantidad())));
    return items;
  }

  @Override
  public BigDecimal getSubtotal(long idUsuario) {
    BigDecimal total = carritoCompraRepository.calcularSubtotal(idUsuario);
    if (total == null) {
      return BigDecimal.ZERO;
    } else {
      return total;
    }
  }

  @Override
  public BigDecimal getBonificacionNeta(long idUsuario, BigDecimal porcentajeBonificacion) {
    BigDecimal subtotal = this.getSubtotal(idUsuario);
    return subtotal
      .multiply(porcentajeBonificacion)
      .divide(new BigDecimal(100), RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getTotal(long idUsuario, long idCliente) {
    Cliente cliente = clienteService.getClientePorId(idCliente);
    BigDecimal subtotal = this.getSubtotal(idUsuario);
    return subtotal.subtract(this.getBonificacionNeta(idUsuario, cliente.getBonificacion()));
  }

  @Override
  public BigDecimal getCantArticulos(long idUsuario) {
    BigDecimal cantArticulos = carritoCompraRepository.getCantArticulos(idUsuario);
    if (cantArticulos == null) {
      return BigDecimal.ZERO;
    } else {
      return cantArticulos;
    }
  }

  @Override
  public long getCantRenglones(long idUsuario) {
    return carritoCompraRepository.getCantRenglones(idUsuario);
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
    Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
    Producto producto = productoService.getProductoPorId(idProducto);
    ItemCarritoCompra item = carritoCompraRepository.findByUsuarioAndProducto(usuario, producto);
    if (item == null) {
      BigDecimal importe = producto.getPrecioLista().multiply(cantidad);
      ItemCarritoCompra itemCC = carritoCompraRepository.save(
          new ItemCarritoCompra(null, cantidad, producto, importe, usuario));
      logger.warn("Nuevo item de carrito de compra agregado: {}", itemCC);
    } else {
      BigDecimal nuevaCantidad = item.getCantidad().add(cantidad);
      if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
        item.setCantidad(BigDecimal.ZERO);
      } else {
        item.setCantidad(nuevaCantidad);
      }
      item.setImporte(producto.getPrecioLista().multiply(nuevaCantidad));
      ItemCarritoCompra itemCC = carritoCompraRepository.save(item);
      logger.warn("Item de carrito de compra modificado: {}", itemCC);
    }
  }

  @Override
  public void modificarCantidadItem(long idUsuario, long idProducto, BigDecimal cantidad) {
    Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
    Producto producto = productoService.getProductoPorId(idProducto);
    ItemCarritoCompra item = carritoCompraRepository.findByUsuarioAndProducto(usuario, producto);
    if (item != null) {
      if (cantidad.compareTo(BigDecimal.ZERO) < 0) {
        item.setCantidad(BigDecimal.ZERO);
      } else {
        item.setCantidad(cantidad);
      }
      item.setImporte(producto.getPrecioLista().multiply(cantidad));
      carritoCompraRepository.save(item);
    } else {
      throw new EntityNotFoundException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_item_no_existente"));
    }
  }
}
