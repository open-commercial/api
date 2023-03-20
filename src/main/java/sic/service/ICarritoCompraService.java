package sic.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import sic.entity.ItemCarritoCompra;
import sic.entity.Pedido;
import sic.entity.Usuario;
import sic.dto.CarritoCompraDTO;
import sic.dto.NuevaOrdenDePagoDTO;
import sic.dto.ProductoFaltanteDTO;

public interface ICarritoCompraService {

  CarritoCompraDTO getCarritoCompra(long idUsuario);

  Page<ItemCarritoCompra> getItemsDelCaritoCompra(long idUsuario, int pagina, boolean paginar);

  BigDecimal calcularTotal(long idUsuario);

  ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(long idUsuario, long idProducto, long idSucursal);

  void eliminarItemDelUsuario(long idUsuario, long idProducto);

  void eliminarItem(long idProducto);

  void eliminarTodosLosItemsDelUsuario(long idUsuario);

  void agregarOrModificarItem(long idUsuario, long idProducto, BigDecimal cantidad);

  List<ItemCarritoCompra> getItemsDelCarritoPorUsuario(Usuario usuario);

  Pedido crearPedido(NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO, Long idUsuario);

  List<ProductoFaltanteDTO> getProductosDelCarritoSinStockDisponible(Long idUsuario, long idSucursal);
}
