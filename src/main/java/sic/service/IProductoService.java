package sic.service;

import java.math.BigDecimal;
import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.*;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.modelo.embeddable.PrecioProductoEmbeddable;

public interface IProductoService {

  void actualizar(Producto productoPorActualizar, Producto productoPersistido, byte[] imagen);

  void devolverStockPedido(
      Pedido pedido, TipoDeOperacion tipoDeOperacion, List<RenglonPedido> renglonesAnteriores, Long idSucursalOrigen);

  void actualizarStockPedido(Pedido pedido, TipoDeOperacion tipoDeOperacion);

  void actualizarStockFacturaCompra(
      Map<Long, BigDecimal> idsYCantidades,
      Long idSucursal,
      TipoDeOperacion operacion,
      Movimiento movimiento);

  void actualizarStockNotaCredito(
          Map<Long, BigDecimal> idsYCantidades, Long idSucursal, TipoDeOperacion operacion, Movimiento movimiento);

  void actualizarStockTraspaso(Traspaso traspaso, TipoDeOperacion tipoDeOperacion);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Producto producto);

  void validarCalculos(Producto producto);

  Page<Producto> buscarProductos(BusquedaProductoCriteria criteria, Long idSucursal);

  Page<Producto> buscarProductosDeCatalogoParaUsuario(BusquedaProductoCriteria criteria, Long idSucursal, Long isSucursal);

  Page<Producto> buscarProductosDeCatalogoParaVenta(BusquedaProductoCriteria criteria, Long idSucursal, Long idUsuario, Long idCliente);

  void marcarFavoritos(Page<Producto> productos, long idUsuario);

  BooleanBuilder getBuilder(BusquedaProductoCriteria criteria);

  List<Producto> buscarProductosParaReporte(BusquedaProductoCriteria criteria);

  BigDecimal calcularGananciaNeto(BigDecimal precioCosto, BigDecimal gananciaPorcentaje);

  List<ProductoFaltanteDTO> getProductosSinStockDisponible(
      ProductosParaVerificarStockDTO productosParaVerificarStockDTO);

  List<ProductoFaltanteDTO> getProductosSinStockDisponibleParaTraspaso(
          ProductosParaVerificarStockDTO productosParaVerificarStockDTO);

  BigDecimal calcularGananciaPorcentaje(
      BigDecimal precioDeListaNuevo,
      BigDecimal precioDeListaAnterior,
      BigDecimal pvp,
      BigDecimal ivaPorcentaje,
      BigDecimal impInternoPorcentaje,
      BigDecimal precioCosto,
      boolean descendente);

  BigDecimal calcularIVANeto(BigDecimal precioCosto, BigDecimal ivaPorcentaje);

  BigDecimal calcularPVP(BigDecimal precioCosto, BigDecimal gananciaPorcentaje);

  BigDecimal calcularPrecioLista(BigDecimal pvp, BigDecimal ivaPorcentaje);

  void eliminarMultiplesProductos(long[] idProducto);

  Producto getProductoPorCodigo(String codigo);

  Producto getProductoPorDescripcion(String descripciona);

  Producto getProductoNoEliminadoPorId(long idProducto);

  BigDecimal calcularValorStock(BusquedaProductoCriteria criteria);

  void getListaDePreciosEnXls(BusquedaProductoCriteria criteria, long idSucursal);

  void getListaDePreciosEnPdf(BusquedaProductoCriteria criteria, long idSucursal);

  void enviarListaDeProductosPorEmail(String mailTo, byte[] listaDeProductos, String formato, String mensaje);

  void enviarListaDeProductosParaUsuariosSegunRol(Rol rol, BusquedaProductoCriteria criteria, String formato);

  void enviarCatalogoParaViajantes();

  Producto guardar(NuevoProductoDTO producto, long idMedida, long idRubro, long idProveedor);

  void actualizarMultiples(ProductosParaActualizarDTO productosParaActualizarDTO, Usuario usuarioLogueado);

  void guardarCantidadesDeSucursalNueva(Sucursal sucursal);

  String subirImagenProducto(long idProducto, byte[] imagen);

  Pageable getPageable(Integer pagina, List<String> ordenarPor, String sentido, int tamanioPagina);

  Producto calcularCantidadEnSucursalesDisponible(Producto producto, long idSucursalSeleccionada);

  Producto guardarProductoFavorito(long idUsuario, long idProducto);

  Page<Producto> getPaginaProductosFavoritosDelCliente(long idUsuario, long idSucursal, int pagina);

  List<Producto> getProductosFavoritosDelClientePorIdUsuario(long idUsuario);

  boolean isFavorito(long idUsuario, long idProducto);

  void quitarProductoDeFavoritos(long idUsuario, long idProducto);

  void quitarProductoDeFavoritos(long idProducto);

  void quitarProductosDeFavoritos(long idUsuario);

  Long getCantidadDeProductosFavoritos(long idUsuario);

  void validarLongitudDeArrays(int longitudIds, int longitudCantidades);

  ProductoFaltanteDTO construirNuevoProductoFaltante(Producto producto, BigDecimal cantidadSolicitada, BigDecimal cantidadDisponible, long idSucursal);

  PrecioProductoEmbeddable construirPrecioProductoEmbeddable(ProductoDTO productoDTO);

  CantidadProductoEmbeddable construirCantidadProductoEmbeddable(ProductoDTO productoDTO);

  Page<Producto> getProductosRelacionados(long idProducto, long idSucursal, int pagina);

  void agregarCantidadReservada(long idProducto, BigDecimal cantidadParaAgregar);

  void quitarCantidadReservada(long idProducto, BigDecimal cantidadParaQuitar);
}
