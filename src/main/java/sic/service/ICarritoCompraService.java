package sic.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Pedido;
import sic.modelo.Usuario;
import sic.modelo.dto.CarritoCompraDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;

public interface ICarritoCompraService {

  CarritoCompraDTO getCarritoCompra(long idUsuario);

  Page<ItemCarritoCompra> getItemsDelCaritoCompra(long idUsuario, int pagina, Integer tamanio);

  BigDecimal calcularTotal(long idUsuario);

  ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(long idUsuario, long idProducto);

  void eliminarItemDelUsuario(long idUsuario, long idProducto);

  void eliminarItem(long idProducto);

  void eliminarTodosLosItemsDelUsuario(long idUsuario);

  void agregarOrModificarItem(long idUsuario, long idProducto, BigDecimal cantidad);

  List<ItemCarritoCompra> getItemsDelCarritoPorUsuario(Usuario usuario);

  Pedido crearPedido(NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO, Long idUsuario);

  List<ProductoFaltanteDTO> getProductosDelCarritoSinStockDisponible(Long idUsuario);
}
