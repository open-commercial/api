package sic.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.dto.*;
import sic.repository.CarritoCompraRepository;
import sic.service.*;


@Service
@Transactional
public class CarritoCompraServiceImpl implements ICarritoCompraService {

  private final CarritoCompraRepository carritoCompraRepository;
  private final IUsuarioService usuarioService;
  private final IProductoService productoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public CarritoCompraServiceImpl(
      CarritoCompraRepository carritoCompraRepository,
      IUsuarioService usuarioService,
      IProductoService productoService) {
    this.carritoCompraRepository = carritoCompraRepository;
    this.usuarioService = usuarioService;
    this.productoService = productoService;
  }

  @Override
  public CarritoCompraDTO getCarritoCompra(long idUsuario, long idCliente) {
    CarritoCompraDTO carritoCompraDTO = new CarritoCompraDTO();
    BigDecimal cantArticulos = carritoCompraRepository.getCantArticulos(idUsuario);
    carritoCompraDTO.setCantRenglones(carritoCompraRepository.getCantRenglones(idUsuario));
    if (cantArticulos == null) cantArticulos = BigDecimal.ZERO;
    carritoCompraDTO.setCantArticulos(cantArticulos);
    carritoCompraDTO.setTotal(this.calcularTotal(idUsuario));
    return carritoCompraDTO;
  }

  @Override
  public BigDecimal calcularTotal(long idUsuario) {
    BigDecimal total = BigDecimal.ZERO;
    List<ItemCarritoCompra> itemCarritoCompra =
        this.getItemsDelCaritoCompra(idUsuario, 0, Integer.MAX_VALUE).getContent();
    for (ItemCarritoCompra i : itemCarritoCompra) {
        total = total.add(i.getImporte());
    }
    return total;
  }

  @Override
  public Page<ItemCarritoCompra> getItemsDelCaritoCompra(
      long idUsuario, int pagina, Integer tamanio) {
    Pageable pageable;
    if (tamanio != null) {
      pageable =
          PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.DESC, "idItemCarritoCompra"));
    } else {
      pageable =
          PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, "idItemCarritoCompra"));
    }
    Page<ItemCarritoCompra> items =
        carritoCompraRepository.findAllByUsuario(
            usuarioService.getUsuarioNoEliminadoPorId(idUsuario), pageable);
    items.forEach(this::calcularImporteBonificado);
    return items;
  }

  @Override
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(
      long idUsuario, long idProducto) {
    ItemCarritoCompra itemCarritoCompra =
        this.carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
    this.calcularImporteBonificado(itemCarritoCompra);
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
    ItemCarritoCompra item =
        carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
    if (item == null) {
      ItemCarritoCompra itemCC =
          carritoCompraRepository.save(
              new ItemCarritoCompra(null, cantidad, producto, null, usuario));
      logger.warn("Nuevo item de carrito de compra agregado: {}", itemCC);
    } else {
      if (cantidad.compareTo(BigDecimal.ZERO) < 0) {
        item.setCantidad(BigDecimal.ZERO);
      } else {
        item.setCantidad(cantidad);
      }
      ItemCarritoCompra itemCC = carritoCompraRepository.save(item);
      logger.warn("Item de carrito de compra modificado: {}", itemCC);
    }
  }

  private void calcularImporteBonificado(ItemCarritoCompra itemCarritoCompra) {
    if (itemCarritoCompra != null) {
      if (itemCarritoCompra.getCantidad().compareTo(itemCarritoCompra.getProducto().getBulto())
          >= 0) {
        itemCarritoCompra.setImporte(
            itemCarritoCompra
                .getProducto()
                .getPrecioBonificado()
                .multiply(itemCarritoCompra.getCantidad())
                .setScale(2, RoundingMode.HALF_UP));
      } else {
        itemCarritoCompra.setImporte(
            itemCarritoCompra
                .getProducto()
                .getPrecioLista()
                .multiply(itemCarritoCompra.getCantidad())
                .setScale(2, RoundingMode.HALF_UP));
      }
    }
  }

  @Override
  public List<ItemCarritoCompra> getItemsDelCarritoPorUsuario(Usuario usuario) {
    return carritoCompraRepository.findAllByUsuarioOrderByIdItemCarritoCompraDesc(usuario);
  }
}
