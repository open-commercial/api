package org.opencommercial.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.opencommercial.model.ItemCarritoCompra;
import org.opencommercial.model.Pedido;
import org.opencommercial.model.Usuario;
import org.opencommercial.model.dto.CarritoCompraDTO;
import org.opencommercial.model.dto.NuevaOrdenDePagoDTO;
import org.opencommercial.model.dto.ProductoFaltanteDTO;

public interface CarritoCompraService {

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
