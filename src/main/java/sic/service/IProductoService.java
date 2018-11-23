package sic.service;

import java.math.BigDecimal;
import sic.modelo.TipoDeOperacion;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaProductoCriteria;
import sic.modelo.Empresa;
import sic.modelo.Movimiento;
import sic.modelo.Producto;

public interface IProductoService {

  void actualizar(Producto productoPorActualizar, Producto productoPersistido);

  void actualizarStock(
      Map<Long, BigDecimal> idsYCantidades, TipoDeOperacion operacion, Movimiento movimiento);

  Page<Producto> buscarProductos(BusquedaProductoCriteria criteria);

  BigDecimal calcularGananciaNeto(BigDecimal precioCosto, BigDecimal ganancia_porcentaje);

  Map<Long, BigDecimal> getProductosSinStockDisponible(long[] idProducto, BigDecimal[] cantidad);

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

  Producto getProductoPorCodigo(String codigo, long idEmpresa);

  Producto getProductoPorDescripcion(String descripcion, Empresa empresa);

  Producto getProductoPorId(long idProducto);

  BigDecimal calcularValorStock(BusquedaProductoCriteria criteria);

  byte[] getListaDePreciosPorEmpresa(List<Producto> productos, long idEmpresa, String formato);

  Producto guardar(Producto producto);

  List<Producto> actualizarMultiples(
      long[] idProducto,
      boolean checkPrecios,
      boolean checkDescuentoRecargoPorcentaje,
      BigDecimal descuentoRecargoPorcentaje,
      BigDecimal gananciaNeto,
      BigDecimal gananciaPorcentaje,
      BigDecimal impuestoInternoNeto,
      BigDecimal impuestoInternoPorcentaje,
      BigDecimal IVANeto,
      BigDecimal IVAPorcentaje,
      BigDecimal precioCosto,
      BigDecimal precioLista,
      BigDecimal precioVentaPublico,
      boolean checkMedida,
      Long idMedida,
      boolean checkRubro,
      Long idRubro,
      boolean checkProveedor,
      Long idProveedor,
      boolean checkVisibilidad,
      Boolean publico);

  List<Producto> getMultiplesProductosPorId(List<Long> idsProductos);

  void subirImagenProducto(long idProducto, byte[] imagen);

}
