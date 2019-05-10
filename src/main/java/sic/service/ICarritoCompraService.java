package sic.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.dto.CarritoCompraDTO;

public interface ICarritoCompraService {

  CarritoCompraDTO getCarritoCompra(long idUsuario, long idCliente);

  Page<ItemCarritoCompra> getItemsDelCaritoCompra(
      long idUsuario, long idCliente, Pageable pageable);

  ItemCarritoCompra getItemCarritoDeCompraDeUsuarioPorIdProducto(long idUsuario, long idProducto);

  void eliminarItemDelUsuario(long idUsuario, long idProducto);

  void eliminarItem(long idProducto);

  void eliminarTodosLosItemsDelUsuario(long idUsuario);

  void agregarOrModificarItem(long idUsuario, long idProducto, BigDecimal cantidad);

  void modificarCantidadItem(long idUsuario, long idProducto, BigDecimal cantidad);
}
