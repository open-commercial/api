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
      Map<Long, BigDecimal> idsYCantidades, Long idSucursal, TipoDeOperacion operacion, Movimiento movimiento, TipoDeComprobante tipoDeComprobante);

  Page<Producto> buscarProductos(BusquedaProductoCriteria criteria);

  BigDecimal calcularGananciaNeto(BigDecimal precioCosto, BigDecimal ganancia_porcentaje);

  Map<Long, BigDecimal> getProductosSinStockDisponible(Long idSucursal, long[] idProducto, BigDecimal[] cantidad);

  BigDecimal calcularGananciaPorcentaje(
      BigDecimal precioDeListaNuevo,
      BigDecimal precioDeListaAnterior,
      BigDecimal pvp,
      BigDecimal ivaPorcentaje,
      BigDecimal impInternoPorcentaje,
      BigDecimal precioCosto,
      boolean descendente);

  BigDecimal calcularIVANeto(BigDecimal precioCosto, BigDecimal iva_porcentaje);

  BigDecimal calcularPVP(BigDecimal precioCosto, BigDecimal ganancia_porcentaje);

  BigDecimal calcularPrecioLista(
      BigDecimal PVP, BigDecimal iva_porcentaje);

  void eliminarMultiplesProductos(long[] idProducto);

  Producto getProductoPorCodigo(String codigo);

  Producto getProductoPorDescripcion(String descripciona);

  Producto getProductoNoEliminadoPorId(long idProducto);

  Page<Producto> getProductosConPrecioBonificado(Page<Producto> productos, Cliente cliente);

  BigDecimal calcularValorStock(BusquedaProductoCriteria criteria);

  byte[] getListaDePreciosPorSucursal(List<Producto> productos, String formato);

  Producto guardar(@Valid Producto producto);

  List<Producto> actualizarMultiples(ProductosParaActualizarDTO productosParaActualizarDTO);

  String subirImagenProducto(long idProducto, byte[] imagen);

  List<Producto> getMultiplesProductosPorId(List<Long> idsProductos);

}
