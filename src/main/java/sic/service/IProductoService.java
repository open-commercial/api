package sic.service;

import java.math.BigDecimal;

import sic.modelo.*;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.ProductosParaActualizarDTO;

import javax.validation.Valid;

public interface IProductoService {

  void actualizar(@Valid Producto productoPorActualizar, Producto productoPersistido);

  void actualizarStock(
      Map<Long, BigDecimal> idsYCantidades,
      TipoDeOperacion operacion,
      Movimiento movimiento,
      TipoDeComprobante tipoDeComprobante);

  Page<Producto> buscarProductos(BusquedaProductoCriteria criteria);

  List<Producto> buscarProductosParaReporte(BusquedaProductoCriteria criteria);

  BigDecimal calcularGananciaNeto(BigDecimal precioCosto, BigDecimal gananciaPorcentaje);

  Map<Long, BigDecimal> getProductosSinStockDisponible(long[] idProducto, BigDecimal[] cantidad);

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

  Producto getProductoPorCodigo(String codigo, long idEmpresa);

  Producto getProductoPorDescripcion(String descripcion, Empresa empresa);

  Producto getProductoNoEliminadoPorId(long idProducto);

  Page<Producto> getProductosConPrecioBonificado(Page<Producto> productos, Cliente cliente);

  BigDecimal calcularValorStock(BusquedaProductoCriteria criteria);

  byte[] getListaDePreciosPorEmpresa(List<Producto> productos, long idEmpresa, String formato);

  Producto guardar(@Valid Producto producto);

  List<Producto> actualizarMultiples(ProductosParaActualizarDTO productosParaActualizarDTO);

  String subirImagenProducto(long idProducto, byte[] imagen);

  void eliminarImagenProducto(long idProducto);

  List<Producto> getMultiplesProductosPorId(List<Long> idsProductos);

}
