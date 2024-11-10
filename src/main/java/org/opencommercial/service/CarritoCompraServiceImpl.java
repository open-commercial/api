package org.opencommercial.service;

import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.dto.CarritoCompraDTO;
import org.opencommercial.model.dto.NuevaOrdenDePagoDTO;
import org.opencommercial.model.dto.ProductoFaltanteDTO;
import org.opencommercial.model.dto.ProductosParaVerificarStockDTO;
import org.opencommercial.repository.CarritoCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
@Slf4j
public class CarritoCompraServiceImpl implements CarritoCompraService {

  private final CarritoCompraRepository carritoCompraRepository;
  private final UsuarioService usuarioService;
  private final ProductoService productoService;
  private final SucursalService sucursalService;
  private final ClienteService clienteService;
  private final PedidoService pedidoService;
  private final MessageSource messageSource;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public CarritoCompraServiceImpl(
          CarritoCompraRepository carritoCompraRepository,
          UsuarioService usuarioService,
          ProductoService productoService,
          SucursalService sucursalService,
          ClienteService clienteService,
          PedidoService pedidoService,
          MessageSource messageSource) {
    this.carritoCompraRepository = carritoCompraRepository;
    this.usuarioService = usuarioService;
    this.productoService = productoService;
    this.sucursalService = sucursalService;
    this.clienteService = clienteService;
    this.pedidoService = pedidoService;
    this.messageSource = messageSource;
  }

  @Override
  public CarritoCompraDTO getCarritoCompra(long idUsuario) {
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
    var itemCarritoCompra = this.getItemsDelCaritoCompra(idUsuario, 0, false).getContent();
    for (ItemCarritoCompra i : itemCarritoCompra) {
      total = total.add(i.getImporte());
    }
    return total;
  }

  @Override
  public Page<ItemCarritoCompra> getItemsDelCaritoCompra(long idUsuario, int pagina, boolean paginar) {
    Pageable pageable;
    if (paginar) {
      pageable = PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, "idItemCarritoCompra"));
    } else {
      pageable = PageRequest.of(
              pagina, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "idItemCarritoCompra"));
    }
    var items = carritoCompraRepository.findAllByUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario), pageable);
    items.forEach(this::calcularImporteBonificado);
    return items;
  }

  @Override
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(long idUsuario, long idProducto, long idSucursal) {
    var itemCarritoCompra = this.carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
    this.calcularImporteBonificado(itemCarritoCompra);
    if (itemCarritoCompra != null)
      productoService.calcularCantidadEnSucursalesDisponible(itemCarritoCompra.getProducto(), idSucursal);
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
    Cliente clienteRelacionado = clienteService.getClientePorIdUsuario(idUsuario);
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    if (clienteRelacionado.isPuedeComprarAPlazo() && !producto.isParaCatalogo()) {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_producto_no_existente", null, Locale.getDefault()));
    }
    ItemCarritoCompra item = carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
    if (item == null) {
      ItemCarritoCompra itemCC =
              carritoCompraRepository.save(
                      new ItemCarritoCompra(null, cantidad, producto, null, usuario));
      log.info("Nuevo item de carrito de compra agregado: {}", itemCC);
    } else {
      if (cantidad.compareTo(BigDecimal.ZERO) < 0) {
        item.setCantidad(BigDecimal.ZERO);
      } else {
        item.setCantidad(cantidad);
      }
      ItemCarritoCompra itemCC = carritoCompraRepository.save(item);
      log.info("Item de carrito de compra modificado: {}", itemCC);
    }
  }

  private void calcularImporteBonificado(ItemCarritoCompra itemCarritoCompra) {
    if (itemCarritoCompra != null) {
      if (itemCarritoCompra
              .getCantidad()
              .compareTo(itemCarritoCompra.getProducto().getCantidadProducto().getCantMinima())
          >= 0) {
        itemCarritoCompra.setImporte(
            itemCarritoCompra
                .getProducto()
                .getPrecioProducto()
                .getPrecioBonificado()
                .multiply(itemCarritoCompra.getCantidad())
                .setScale(2, RoundingMode.HALF_UP));
      } else {
        itemCarritoCompra.setImporte(
            itemCarritoCompra
                .getProducto()
                .getPrecioProducto()
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

  @Override
  @Transactional
  public Pedido crearPedido(NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO, Long idUsuario) {
    Usuario usuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    List<ItemCarritoCompra> items = //validar items contra la capacidad de compra a plazo del cliente
        carritoCompraRepository.findAllByUsuarioOrderByIdItemCarritoCompraDesc(usuario);
    Pedido nuevoPedido = new Pedido();
    nuevoPedido.setCliente(clienteService.getClientePorIdUsuario(idUsuario));
    nuevoPedido.setRecargoPorcentaje(BigDecimal.ZERO);
    nuevoPedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    nuevoPedido.setSucursal(sucursalService.getSucursalPorId(nuevaOrdenDePagoDTO.getIdSucursal()));
    nuevoPedido.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    items.forEach(
            i -> renglonesPedido.add(
                    pedidoService.calcularRenglonPedido(i.getProducto().getIdProducto(), i.getCantidad())));
    nuevoPedido.setRenglones(renglonesPedido);
    nuevoPedido.setTipoDeEnvio(nuevaOrdenDePagoDTO.getTipoDeEnvio());
    Pedido pedido = pedidoService.guardar(nuevoPedido, null);
    this.eliminarTodosLosItemsDelUsuario(idUsuario);
    return pedido;
  }

  @Override
  public List<ProductoFaltanteDTO> getProductosDelCarritoSinStockDisponible(Long idUsuario, long idSucursal) {
    var items = this.getItemsDelCarritoPorUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    long[] idProducto = new long[items.size()];
    BigDecimal[] cantidad = new BigDecimal[items.size()];
    int indice = 0;
    for (ItemCarritoCompra item : items) {
      idProducto[indice] = item.getProducto().getIdProducto();
      cantidad[indice] = item.getCantidad();
      indice++;
    }
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder()
                .idProducto(idProducto)
                .cantidad(cantidad)
                .idSucursal(sucursalService.getSucursalPorId(idSucursal).getIdSucursal())
                .build();
    return productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
  }
}
