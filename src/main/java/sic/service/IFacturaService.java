package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import sic.modelo.Movimiento;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import sic.modelo.BusquedaFacturaCompraCriteria;
import sic.modelo.BusquedaFacturaVentaCriteria;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.Pedido;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.Recibo;
import sic.modelo.RenglonFactura;
import sic.modelo.TipoDeComprobante;

public interface IFacturaService {
    
    Factura getFacturaPorId(Long id_Factura);
    
    FacturaCompra getFacturaCompraPorId(Long idFactura);

    Long getCAEById(long idFactura);
    
    BigDecimal getTotalById(long idFactura);
    
    List<Factura> getFacturasDelPedido(Long idPedido);

    TipoDeComprobante[] getTipoFacturaCompra(Empresa empresa, Proveedor proveedor);

    TipoDeComprobante[] getTipoFacturaVenta(Empresa empresa, Cliente cliente);

    TipoDeComprobante[] getTiposFacturaSegunEmpresa(Empresa empresa);

    List<RenglonFactura> getRenglonesDeLaFactura(Long id_Factura);
    
    Factura getFacturaDelPago(long idPago);
    
    List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(Long id_Factura);
    
    RenglonFactura getRenglonFactura(Long idRenglonFactura);
 
    Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria);

    Page<FacturaVenta> buscarFacturaVenta(BusquedaFacturaVentaCriteria criteria);
    
    Slice<FacturaCompra> getFacturasCompraProveedor(@Param("id_Proveedor") long id_Proveedor, Pageable page);
    
    List<Factura> guardar(List<Factura> facturas, Long idPedido, List<Recibo> recibos);

    void eliminar(long[] idFactura);
    
    FacturaVenta autorizarFacturaVenta(FacturaVenta fv);
    
    Factura actualizarFacturaEstadoPago(Factura factura);
    
    BigDecimal getTotalPagado(long idFactura);

    List<Factura> ordenarFacturasPorFechaAsc(List<Factura> facturas);

    boolean validarFacturasParaPagoMultiple(List<Factura> facturas, Movimiento movimiento);

    boolean validarClienteProveedorParaPagosMultiples(List<Factura> facturas, Movimiento movimiento);

    boolean validarFacturasImpagasParaPagoMultiple(List<Factura> facturas);

    boolean validarCantidadMaximaDeRenglones(int cantidad, Empresa empresa);

    BigDecimal calcularSubTotal(BigDecimal[] importes);

    BigDecimal calcularDescuentoNeto(BigDecimal subtotal, BigDecimal descuento_porcentaje);

    BigDecimal calcularRecargoNeto(BigDecimal subtotal, BigDecimal recargo_porcentaje);

    BigDecimal calcularSubTotalBruto(TipoDeComprobante tipoDeComprobante, BigDecimal subTotal, BigDecimal recargoNeto, BigDecimal descuentoNeto, BigDecimal iva105Neto, BigDecimal iva21Neto);

    BigDecimal calcularIvaNetoFactura(TipoDeComprobante tipo, BigDecimal[] cantidades, BigDecimal[] ivaPorcentajeRenglones, BigDecimal[] ivaNetoRenglones, BigDecimal ivaPorcentaje, BigDecimal porcentajeDescuento, BigDecimal porcentajeRecargo);

    BigDecimal calcularImpInternoNeto(TipoDeComprobante tipoDeComprobante, BigDecimal descuento_porcentaje, BigDecimal recargo_porcentaje, BigDecimal[] importes, BigDecimal[] impuestoPorcentajes);

    BigDecimal calcularImpInternoNeto(Movimiento movimiento, Producto producto, BigDecimal descuento_neto);

    BigDecimal calcularTotal(BigDecimal subTotal_bruto, BigDecimal iva105_neto, BigDecimal iva21_neto);

    BigDecimal calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria);
    
    BigDecimal getSaldoFacturasVentaSegunClienteYEmpresa(long empresa, long cliente, Date hasta);

    BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

    BigDecimal calcularIvaVenta(BusquedaFacturaVentaCriteria criteria);

    BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria);

    BigDecimal calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria);

    BigDecimal calcularIVANetoRenglon(Movimiento movimiento, TipoDeComprobante tipo, Producto producto, BigDecimal descuento_porcentaje);

    BigDecimal calcularPrecioUnitario(Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto);

    long calcularNumeroFacturaVenta(TipoDeComprobante tipoDeComprobante, long serie, long idEmpresa);

    BigDecimal calcularImporte(BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuento_neto);    

    byte[] getReporteFacturaVenta(Factura factura);

    List<Factura> dividirFactura(FacturaVenta factura, int[] indices);

    List<RenglonFactura> convertirRenglonesPedidoEnRenglonesFactura(Pedido pedido, TipoDeComprobante tipoDeComprobante);

    boolean pedidoTotalmenteFacturado(Pedido pedido);
    
    RenglonFactura calcularRenglon(TipoDeComprobante tipoDeComprobante, Movimiento movimiento, BigDecimal cantidad, long idProducto, BigDecimal descuentoPorcentaje, BigDecimal dividiendoRenglonFactura);

}
