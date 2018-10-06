package sic.service;

import java.math.BigDecimal;
import sic.modelo.*;
import java.util.List;
import org.springframework.data.domain.Page;

public interface IFacturaService {
    
    Factura getFacturaPorId(Long id_Factura);

    List<Factura> getFacturasDelPedido(Long idPedido);

    TipoDeComprobante[] getTipoFacturaCompra(Empresa empresa, Proveedor proveedor);

    TipoDeComprobante[] getTipoFacturaVenta(Empresa empresa, Cliente cliente);

    TipoDeComprobante[] getTiposFacturaSegunEmpresa(Empresa empresa);

    List<RenglonFactura> getRenglonesDeLaFactura(Long id_Factura);
    
    List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(Long id_Factura);
    
    RenglonFactura getRenglonFactura(Long idRenglonFactura);
 
    Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria);

    Page<FacturaVenta> buscarFacturaVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);
           
    List<FacturaVenta> guardar(List<FacturaVenta> facturas, Long idPedido, List<Recibo> recibos);

    List<FacturaCompra> guardar(List<FacturaCompra> facturas);

    void eliminar(long[] idFactura);
    
    FacturaVenta autorizarFacturaVenta(FacturaVenta fv);

    BigDecimal calcularSubTotal(BigDecimal[] importes);

    BigDecimal calcularDescuentoNeto(BigDecimal subtotal, BigDecimal descuento_porcentaje);

    BigDecimal calcularRecargoNeto(BigDecimal subtotal, BigDecimal recargo_porcentaje);

    BigDecimal calcularSubTotalBruto(TipoDeComprobante tipoDeComprobante, BigDecimal subTotal, BigDecimal recargoNeto, BigDecimal descuentoNeto, BigDecimal iva105Neto, BigDecimal iva21Neto);

    BigDecimal calcularIvaNetoFactura(TipoDeComprobante tipo, BigDecimal[] cantidades, BigDecimal[] ivaPorcentajeRenglones, BigDecimal[] ivaNetoRenglones, BigDecimal ivaPorcentaje, BigDecimal porcentajeDescuento, BigDecimal porcentajeRecargo);

    BigDecimal calcularImpInternoNeto(TipoDeComprobante tipoDeComprobante, BigDecimal descuento_porcentaje, BigDecimal recargo_porcentaje, BigDecimal[] importes, BigDecimal[] impuestoPorcentajes);

    BigDecimal calcularTotal(BigDecimal subTotal_bruto, BigDecimal iva105_neto, BigDecimal iva21_neto);

    BigDecimal calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);

    BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

    BigDecimal calcularIvaVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);

    BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria);

    BigDecimal calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);

    BigDecimal calcularIVANetoRenglon(Movimiento movimiento, TipoDeComprobante tipo, Producto producto, BigDecimal descuento_porcentaje);

    BigDecimal calcularPrecioUnitario(Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto, BigDecimal bonificacionPorcentaje);

    long calcularNumeroFacturaVenta(TipoDeComprobante tipoDeComprobante, long serie, long idEmpresa);

    BigDecimal calcularImporte(BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal descuento_neto);    

    byte[] getReporteFacturaVenta(Factura factura);

    List<FacturaVenta> dividirFactura(FacturaVenta factura, int[] indices);

    List<RenglonFactura> getRenglonesPedidoParaFacturar(Pedido pedido, TipoDeComprobante tipoDeComprobante);

    boolean pedidoTotalmenteFacturado(Pedido pedido);

  RenglonFactura calcularRenglon(
      TipoDeComprobante tipoDeComprobante,
      Movimiento movimiento,
      BigDecimal cantidad,
      long idProducto,
      Long idCliente,
      BigDecimal descuentoPorcentaje,
      boolean dividiendoRenglonFactura);

}
