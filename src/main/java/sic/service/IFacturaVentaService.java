package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

public interface IFacturaVentaService {

  TipoDeComprobante[] getTipoFacturaVenta(Sucursal sucursal, Cliente cliente);

  List<Factura> getFacturasDelPedido(Long idPedido);

  List<RenglonFactura> getRenglonesPedidoParaFacturar(
      long idPedido, TipoDeComprobante tipoDeComprobante);

  boolean pedidoTotalmenteFacturado(Pedido pedido);

  Page<FacturaVenta> buscarFacturaVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);

  List<FacturaVenta> guardar(
      @Valid List<FacturaVenta> facturas, Long idPedido, List<Recibo> recibos);

  FacturaVenta autorizarFacturaVenta(FacturaVenta fv);

  BigDecimal calcularTotalFacturadoVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);

  BigDecimal calcularIvaVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);

  BigDecimal calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn);

  long calcularNumeroFacturaVenta(TipoDeComprobante tipoDeComprobante, long serie, long idSucursal);

  byte[] getReporteFacturaVenta(Factura factura);

  List<FacturaVenta> dividirFactura(FacturaVenta factura, int[] indices);

  boolean existeFacturaVentaAnteriorSinAutorizar(ComprobanteAFIP comprobante);

  void enviarFacturaVentaPorEmail(long idFactura);

  FacturaVenta agregarRenglonesAFacturaConIVA(
      FacturaVenta facturaConIVA, int[] indices, List<RenglonFactura> renglones);

  FacturaVenta agregarRenglonesAFacturaSinIVA(
      FacturaVenta facturaSinIVA, int[] indices, List<RenglonFactura> renglones);
}
