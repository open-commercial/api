package sic.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
      ISucursalService sucursalService,
      IPedidoService pedidoService,
      IMercadoPagoService mercadoPagoService,
      MessageSource messageSource) {
    this.carritoCompraRepository = carritoCompraRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.pedidoService = pedidoService;
    this.mercadoPagoService = mercadoPagoService;
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
    items.forEach(i -> this.calcularImporteBonificado(i, bonificacion));
    return items;
  }

  @Override
  public ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(
      long idUsuario, long idProducto) {
    ItemCarritoCompra itemCarritoCompra =
        this.carritoCompraRepository.findByUsuarioAndProducto(idUsuario, idProducto);
    BigDecimal bonificacion = clienteService.getClientePorIdUsuario(idUsuario).getBonificacion();
    this.calcularImporteBonificado(itemCarritoCompra, bonificacion);
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
    CarritoCompraDTO carritoCompraDTO =
        this.getCarritoCompra(
            nuevaOrdenDeCompraDTO.getIdUsuario(), nuevaOrdenDeCompraDTO.getIdCliente());
    List<ItemCarritoCompra> items =
        carritoCompraRepository.findAllByUsuarioOrderByIdItemCarritoCompraDesc(usuario);
    NuevoPedidoDTO nuevoPedido =
        NuevoPedidoDTO.builder()
            .idCliente(nuevaOrdenDeCompraDTO.getIdCliente())
            .idUsuario(nuevaOrdenDeCompraDTO.getIdUsuario())
            .renglones(new ArrayList<>())
            .idSucursal(nuevaOrdenDeCompraDTO.getIdSucursal())
            .tipoDeEnvio(nuevaOrdenDeCompraDTO.getTipoDeEnvio())
            .observaciones(nuevaOrdenDeCompraDTO.getObservaciones())
            .recargoPorcentaje(BigDecimal.ZERO)
            .descuentoPorcentaje(carritoCompraDTO.getBonificacionPorcentaje())
            .build();
    items.forEach(
        i ->
            nuevoPedido
                .getRenglones()
                .add(
                    NuevoRenglonPedidoDTO.builder()
                        .idProductoItem(i.getProducto().getIdProducto())
                        .cantidad(i.getCantidad())
                        .build()));
    Pedido p = pedidoService.guardar(nuevoPedido);
    this.eliminarTodosLosItemsDelUsuario(nuevaOrdenDeCompraDTO.getIdUsuario());
    return p;
  }

  private void calcularImporteBonificado(
      ItemCarritoCompra itemCarritoCompra, BigDecimal bonificacion) {
    if (itemCarritoCompra != null) {
      itemCarritoCompra
          .getProducto()
          .setHayStock(
              itemCarritoCompra
                      .getProducto()
                      .getCantidadTotalEnSucursales()
                      .compareTo(BigDecimal.ZERO)
                  > 0);
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
            .setPrecioListaBonificado(
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
                  .getPrecioListaBonificado()
                  .multiply(itemCarritoCompra.getCantidad())
                  .setScale(2, RoundingMode.HALF_UP));
        }
      } else if (bonificacion != null && bonificacion.compareTo(BigDecimal.ZERO) > 0) {
        itemCarritoCompra
            .getProducto()
            .setPrecioListaBonificado(
                itemCarritoCompra
                    .getProducto()
                    .getPrecioLista()
                    .multiply(
                        BigDecimal.ONE.subtract(bonificacion.divide(CIEN, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP));
        if (itemCarritoCompra.getCantidad().compareTo(itemCarritoCompra.getProducto().getBulto())
            >= 0) {
          itemCarritoCompra.setImporteBonificado(
              itemCarritoCompra
                  .getProducto()
                  .getPrecioListaBonificado()
                  .multiply(itemCarritoCompra.getCantidad())
                  .setScale(2, RoundingMode.HALF_UP));
        }
      }
      itemCarritoCompra
          .getProducto()
          .setHayStock(
              itemCarritoCompra
                      .getProducto()
                      .getCantidadTotalEnSucursales()
                      .compareTo(BigDecimal.ZERO)
                  > 0);
    }
  }
}
