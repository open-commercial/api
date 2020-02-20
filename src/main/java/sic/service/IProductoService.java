package sic.service;

import java.math.BigDecimal;

import sic.modelo.*;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductosParaActualizarDTO;

import javax.validation.Valid;

public interface IProductoService {

  void actualizar(@Valid Producto productoPorActualizar, Producto productoPersistido, byte[] imagen);

  void actualizarStock(
      Map<Long, BigDecimal> idsYCantidades,
      Long idSucursal,
      TipoDeOperacion operacion,
      Movimiento movimiento,
      TipoDeComprobante tipoDeComprobante);

  Page<Producto> buscarProductos(BusquedaProductoCriteria criteria);

  List<Producto> buscarProductosParaReporte(BusquedaProductoCriteria criteria);

  BigDecimal calcularGananciaNeto(BigDecimal precioCosto, BigDecimal gananciaPorcentaje);

  Map<Long, BigDecimal> getProductosSinStockDisponible(ProductosParaVerificarStockDTO productosParaVerificarStockDTO);

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

  BigDecimal calcularPrecioLista(
      BigDecimal pvp, BigDecimal ivaPorcentaje);

  void eliminarMultiplesProductos(long[] idProducto);

  Producto getProductoPorCodigo(String codigo);

  Producto getProductoPorDescripcion(String descripciona);

  Producto getProductoNoEliminadoPorId(long idProducto);

  Page<Producto> getProductosConPrecioBonificado(Page<Producto> productos);

  BigDecimal calcularValorStock(BusquedaProductoCriteria criteria);

  byte[] getListaDePrecios(List<Producto> productos, String formato);

  Producto guardar(@Valid NuevoProductoDTO producto, long idMedida, long idRubro, long idProveedor);

  List<Producto> actualizarMultiples(ProductosParaActualizarDTO productosParaActualizarDTO);

  void guardarCantidadesDeSucursalNueva(Sucursal sucursal);

  String subirImagenProducto(long idProducto, byte[] imagen);

  List<Producto> getMultiplesProductosPorId(List<Long> idsProductos);

}
