package sic.service;

import java.util.Date;
import sic.modelo.Movimiento;
import java.util.List;
import org.springframework.data.domain.Page;
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

    Long getCAEById(long idFactura);
    
    Double getTotalById(long idFactura);
    
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
    
    List<Factura> guardar(List<Factura> facturas, Long idPedido, List<Recibo> recibos);

    void eliminar(long[] idFactura);
    
    FacturaVenta autorizarFacturaVenta(FacturaVenta fv);
    
    Factura actualizarFacturaEstadoPago(Factura factura);
    
    Double getTotalPagado(long idFactura);

    List<Factura> ordenarFacturasPorFechaAsc(List<Factura> facturas);

    boolean validarFacturasParaPagoMultiple(List<Factura> facturas, Movimiento movimiento);

    boolean validarClienteProveedorParaPagosMultiples(List<Factura> facturas, Movimiento movimiento);

    boolean validarFacturasImpagasParaPagoMultiple(List<Factura> facturas);

    boolean validarCantidadMaximaDeRenglones(int cantidad, Empresa empresa);

    double calcularSubTotal(double[] importes);

    double calcularDescuentoNeto(double subtotal, double descuento_porcentaje);

    double calcularRecargoNeto(double subtotal, double recargo_porcentaje);

    double calcularSubTotalBruto(TipoDeComprobante tipoDeComprobante, double subTotal, double recargoNeto, double descuentoNeto, double iva105Neto, double iva21Neto);

    double calcularIvaNetoFactura(TipoDeComprobante tipo, double[] cantidades, double[] ivaPorcentajeRenglones, double[] ivaNetoRenglones, double ivaPorcentaje, double porcentajeDescuento, double porcentajeRecargo);

    double calcularImpInternoNeto(TipoDeComprobante tipoDeComprobante, double descuento_porcentaje, double recargo_porcentaje, double[] importes, double [] impuestoPorcentajes);

    double calcularImpInternoNeto(Movimiento movimiento, Producto producto, double descuento_neto);

    public double calcularTotal(double subTotal_bruto, double iva105_neto, double iva21_neto);

    double calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria);
    
    double getSaldoFacturasVentaSegunClienteYEmpresa(long empresa, long cliente, Date hasta);

    double calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

    double calcularIvaVenta(BusquedaFacturaVentaCriteria criteria);

    double calcularIvaCompra(BusquedaFacturaCompraCriteria criteria);

    double calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria);

    double calcularIVANetoRenglon(Movimiento movimiento, TipoDeComprobante tipo, Producto producto, double descuento_porcentaje);

    double calcularPrecioUnitario(Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto);

    long calcularNumeroFacturaVenta(TipoDeComprobante tipoDeComprobante, long serie, long idEmpresa);

    double calcularImporte(double cantidad, double precioUnitario, double descuento_neto);    

    byte[] getReporteFacturaVenta(Factura factura);

    List<Factura> dividirFactura(FacturaVenta factura, int[] indices);

    List<RenglonFactura> convertirRenglonesPedidoARenglonesFactura(Pedido pedido, TipoDeComprobante tipoDeComprobante);

    RenglonFactura calcularRenglon(TipoDeComprobante tipoDeComprobante, Movimiento movimiento, double cantidad, Long idProducto, double descuento_porcentaje);

}
