package sic.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mercadopago.exceptions.MPException;
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
  private final IClienteService clienteService;
  private final IProductoService productoService;
  private final IPedidoService pedidoService;
  private final IMercadoPagoService mercadoPagoService;
  private final ISucursalService sucursalService;
  private final MessageSource messageSource;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private static final Long ID_SUCURSAL_DEFAULT = 1L;
  private static final BigDecimal CIEN = new BigDecimal("100");

  @Autowired
  public CarritoCompraServiceImpl(
      CarritoCompraRepository carritoCompraRepository,
      IUsuarioService usuarioService,
      IClienteService clienteService,
      IProductoService productoService,
      IPedidoService pedidoService,
      ISucursalService sucursalService,
      MessageSource messageSource,
      IMercadoPagoService mercadoPagoService) {
    this.carritoCompraRepository = carritoCompraRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.pedidoService = pedidoService;
    this.sucursalService = sucursalService;
    this.messageSource = messageSource;
    this.mercadoPagoService = mercadoPagoService;
  }

  @Override
  public CarritoCompraDTO getCarritoCompra(long idUsuario, long idCliente) {
    CarritoCompraDTO carritoCompraDTO = new CarritoCompraDTO();
    BigDecimal cantArticulos = carritoCompraRepository.getCantArticulos(idUsuario);
    carritoCompraDTO.setCantRenglones(carritoCompraRepository.getCantRenglones(idUsuario));
    if (cantArticulos == null) cantArticulos = BigDecimal.ZERO;
    carritoCompraDTO.setCantArticulos(cantArticulos);
    carritoCompraDTO.setTotal(this.calcularTotal(idUsuario, idCliente));
    return carritoCompraDTO;
  }

  private BigDecimal calcularTotal(long idUsuario, long idCliente) {
    BigDecimal total = BigDecimal.ZERO;
    List<ItemCarritoCompra> itemCarritoCompra =
        this.getItemsDelCaritoCompra(idUsuario, idCliente, 0, Integer.MAX_VALUE).getContent();
    for (ItemCarritoCompra i : itemCarritoCompra) {
      if (i.getImporteBonificado() != null && i.getImporteBonificado().compareTo(BigDecimal.ZERO) > 0) {
        total = total.add(i.getImporteBonificado());
      } else {
        total = total.add(i.getImporte());
      }
    }
    return total;
  }

  @Override
  public Page<ItemCarritoCompra> getItemsDelCaritoCompra(
      long idUsuario, long idCliente, int pagina, Integer tamanio) {
    Pageable pageable = null;
    if (tamanio != null) {
      pageable =
          PageRequest.of(
              pagina, tamanio, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
    } else {
      pageable =
          PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, "idItemCarritoCompra"));
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
    Usuario usuario =
        usuarioService.getUsuarioNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdUsuario());
    if (nuevaOrdenDeCompraDTO.getNuevoPagoMercadoPago() != null) {
      nuevaOrdenDeCompraDTO.setIdSucursal(ID_SUCURSAL_DEFAULT);
      try {
        mercadoPagoService.crearNuevoPago(nuevaOrdenDeCompraDTO.getNuevoPagoMercadoPago(), usuario);
      } catch (MPException ex) {
        mercadoPagoService.logExceptionMercadoPago(ex);
      }
    }
    List<ItemCarritoCompra> items =
        carritoCompraRepository.findAllByUsuarioOrderByIdItemCarritoCompraDesc(usuario);
    Pedido pedido = new Pedido();
    pedido.setObservaciones(nuevaOrdenDeCompraDTO.getObservaciones());
    pedido.setRecargoPorcentaje(BigDecimal.ZERO);
    pedido.setDescuentoPorcentaje(BigDecimal.ZERO);
    if (nuevaOrdenDeCompraDTO.getIdSucursal() == null) {
      if (!nuevaOrdenDeCompraDTO.getTipoDeEnvio().equals(TipoDeEnvio.RETIRO_EN_SUCURSAL)) {
        pedido.setSucursal(sucursalService.getSucursalPorId(ID_SUCURSAL_DEFAULT));
      } else {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_pedido_retiro_sucursal_no_seleccionada", null, Locale.getDefault()));
      }
    } else {
      pedido.setSucursal(sucursalService.getSucursalPorId(nuevaOrdenDeCompraDTO.getIdSucursal()));
    }
    pedido.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdUsuario()));
    pedido.setCliente(
        clienteService.getClienteNoEliminadoPorId(nuevaOrdenDeCompraDTO.getIdCliente()));
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    items.forEach(
        i ->
            renglonesPedido.add(
                pedidoService.calcularRenglonPedido(
                    i.getProducto().getIdProducto(), i.getCantidad())));
    pedido.setRenglones(renglonesPedido);
    pedido.setRenglones(renglonesPedido);
    pedido.setTipoDeEnvio(nuevaOrdenDeCompraDTO.getTipoDeEnvio());
    Pedido p = pedidoService.guardar(pedido);
    this.eliminarTodosLosItemsDelUsuario(nuevaOrdenDeCompraDTO.getIdUsuario());
    return p;
  }

  private void calcularImporteBonificado(ItemCarritoCompra itemCarritoCompra) {
    if (itemCarritoCompra != null) {
      itemCarritoCompra.setImporte(
          itemCarritoCompra
              .getProducto()
              .getPrecioLista()
              .multiply(itemCarritoCompra.getCantidad())
              .setScale(2, RoundingMode.HALF_UP));
      if (itemCarritoCompra.getProducto().isOferta()
          && itemCarritoCompra.getProducto().getPorcentajeBonificacionOferta() != null) {
        itemCarritoCompra
            .getProducto()
            .setPrecioBonificado(
                itemCarritoCompra
                    .getProducto()
                    .getPrecioLista()
                    .multiply(
                        BigDecimal.ONE.subtract(
                            itemCarritoCompra
                                .getProducto()
                                .getPorcentajeBonificacionOferta()
                                .divide(CIEN, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP));
        if (itemCarritoCompra.getCantidad().compareTo(itemCarritoCompra.getProducto().getBulto())
            >= 0) {
          itemCarritoCompra.setImporteBonificado(
              itemCarritoCompra
                  .getProducto()
                  .getPrecioBonificado()
                  .multiply(itemCarritoCompra.getCantidad())
                  .setScale(2, RoundingMode.HALF_UP));
        }
      } else if (itemCarritoCompra.getProducto().getPorcentajeBonificacionPrecio() != null
          && itemCarritoCompra
                  .getProducto()
                  .getPorcentajeBonificacionPrecio()
                  .compareTo(BigDecimal.ZERO)
              > 0) {
        itemCarritoCompra
            .getProducto()
            .setPrecioBonificado(
                itemCarritoCompra
                    .getProducto()
                    .getPrecioLista()
                    .multiply(
                        BigDecimal.ONE.subtract(
                            itemCarritoCompra
                                .getProducto()
                                .getPorcentajeBonificacionPrecio()
                                .divide(CIEN, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP));
        if (itemCarritoCompra.getCantidad().compareTo(itemCarritoCompra.getProducto().getBulto())
            >= 0) {
          itemCarritoCompra.setImporteBonificado(
              itemCarritoCompra
                  .getProducto()
                  .getPrecioBonificado()
                  .multiply(itemCarritoCompra.getCantidad())
                  .setScale(2, RoundingMode.HALF_UP));
        }
      }
    }
  }
}
