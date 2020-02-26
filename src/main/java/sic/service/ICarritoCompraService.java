package sic.service;

import java.math.BigDecimal;

import com.mercadopago.resources.Preference;
import org.springframework.data.domain.Page;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Pedido;
import sic.modelo.dto.CarritoCompraDTO;
import sic.modelo.dto.NuevaOrdenDeCompraDTO;

public interface ICarritoCompraService {

  CarritoCompraDTO getCarritoCompra(long idUsuario, long idCliente);

  Page<ItemCarritoCompra> getItemsDelCaritoCompra(long idUsuario, int pagina, Integer tamanio);

  Pedido crearPedido(NuevaOrdenDeCompraDTO nuevaOrdenDeCompraDTO);

  ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(long idUsuario, long idProducto);

  void eliminarItemDelUsuario(long idUsuario, long idProducto);

  void eliminarItem(long idProducto);

  void eliminarTodosLosItemsDelUsuario(long idUsuario);

  void agregarOrModificarItem(long idUsuario, long idProducto, BigDecimal cantidad);

  String crearPreferenceDeCarritoCompra(long idUsuario);
}
