package sic.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;
import java.util.List;
import java.util.Map;

import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.Resultados;

public interface IFacturaService {

  Factura getFacturaNoEliminadaPorId(long idFactura);

  void eliminarFactura(long idFactura);

  TipoDeComprobante[] getTiposDeComprobanteSegunSucursal(Sucursal sucursal);

  List<RenglonFactura> getRenglonesDeLaFactura(Long idFactura);

  List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(Long idFactura);

  RenglonFactura getRenglonFactura(Long idRenglonFactura);

  BigDecimal calcularIvaNetoFactura(
      TipoDeComprobante tipo,
      BigDecimal[] cantidades,
      BigDecimal[] ivaPorcentajeRenglones,
      BigDecimal[] ivaNetoRenglones,
      BigDecimal ivaPorcentaje,
      BigDecimal porcentajeDescuento,
      BigDecimal porcentajeRecargo);

  BigDecimal calcularIVANetoRenglon(
      Movimiento movimiento,
      TipoDeComprobante tipo,
      Producto producto,
      BigDecimal descuentoPorcentaje);

  BigDecimal calcularPrecioUnitario(
      Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto);

  RenglonFactura calcularRenglon(
      TipoDeComprobante tipoDeComprobante,
      Movimiento movimiento,
      BigDecimal cantidad,
      long idProducto,
      BigDecimal bonificacion);

  List<RenglonFactura> calcularRenglones(
      TipoDeComprobante tipoDeComprobante,
      Movimiento movimiento,
      BigDecimal[] cantidad,
      long[] idProducto,
      BigDecimal[] bonificacion);

  Resultados calcularResultadosFactura(NuevosResultadosComprobanteDTO nuevosResultadosComprobante);

  Pageable getPageable(Integer pagina, String ordenarPor, String sentido);

  void calcularValoresFactura(Factura factura);

  Factura procesarFactura(Factura factura);

  Map<Long, BigDecimal> getIdsProductosYCantidades(Factura factura);
}
