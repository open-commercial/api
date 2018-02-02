package sic.service;

import java.math.BigDecimal;
import java.util.HashMap;
import sic.modelo.TipoDeOperacion;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaProductoCriteria;
import sic.modelo.Empresa;
import sic.modelo.Medida;
import sic.modelo.Movimiento;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.Rubro;

public interface IProductoService {

    void actualizar(Producto producto);

    void actualizarStock(HashMap<Long, BigDecimal> idsYCantidades, TipoDeOperacion operacion, Movimiento movimiento);

    Page<Producto> buscarProductos(BusquedaProductoCriteria criteria);

    BigDecimal calcularGanancia_Neto(BigDecimal precioCosto, BigDecimal ganancia_porcentaje);

    Map<BigDecimal, Producto> getProductosSinStockDisponible(long[] idProducto, BigDecimal[] cantidad);

    Map<BigDecimal, Producto> getProductosNoCumplenCantidadVentaMinima(long[] idProducto, BigDecimal[] cantidad);
    
    BigDecimal calcularGanancia_Porcentaje(BigDecimal precioDeListaNuevo, 
            BigDecimal precioDeListaAnterior, BigDecimal pvp, BigDecimal ivaPorcentaje, 
            BigDecimal impInternoPorcentaje, BigDecimal precioCosto, boolean descendente);

    BigDecimal calcularIVA_Neto(BigDecimal precioCosto, BigDecimal iva_porcentaje);

    BigDecimal calcularImpInterno_Neto(BigDecimal precioCosto, BigDecimal impInterno_porcentaje);

    BigDecimal calcularPVP(BigDecimal precioCosto, BigDecimal ganancia_porcentaje);

    BigDecimal calcularPrecioLista(BigDecimal PVP, BigDecimal iva_porcentaje, BigDecimal impInterno_porcentaje);    

    void eliminarMultiplesProductos(long[] idProducto);

    Producto getProductoPorCodigo(String codigo, Empresa empresa);

    Producto getProductoPorDescripcion(String descripcion, Empresa empresa);

    Producto getProductoPorId(long id_Producto);
    
    BigDecimal calcularValorStock(BusquedaProductoCriteria criteria);
  
    byte[] getReporteListaDePreciosPorEmpresa(List<Producto> productos, Empresa empresa);

    Producto guardar(Producto producto);

    List<Producto> modificarMultiplesProductos(long[] idProducto,
                                               boolean checkPrecios,            
                                               BigDecimal gananciaNeto,
                                               BigDecimal gananciaPorcentaje,
                                               BigDecimal impuestoInternoNeto,
                                               BigDecimal impuestoInternoPorcentaje,
                                               BigDecimal IVANeto,
                                               BigDecimal IVAPorcentaje,
                                               BigDecimal precioCosto,
                                               BigDecimal precioLista,
                                               BigDecimal precioVentaPublico,                                                                     
                                               boolean checkMedida, Medida medida,
                                               boolean checkRubro, Rubro rubro,
                                               boolean checkProveedor, Proveedor proveedor);    
    
}
