package sic.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import sic.domain.Movimiento;
import sic.domain.Resultados;
import sic.domain.TipoDeComprobante;
import sic.entity.*;
import java.util.List;
import java.util.Map;

import sic.dto.NuevosResultadosComprobanteDTO;
import sic.dto.NuevoRenglonFacturaDTO;

public interface IFacturaService {

  Factura getFacturaNoEliminadaPorId(long idFactura);

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
      NuevoRenglonFacturaDTO nuevoRenglonFacturaDTO);

  List<RenglonFactura> calcularRenglones(
      TipoDeComprobante tipoDeComprobante,
      Movimiento movimiento,
      List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO);

  Resultados calcularResultadosFactura(NuevosResultadosComprobanteDTO nuevosResultadosComprobante);

  Pageable getPageable(Integer pagina, String ordenarPor, String sentido);

  void calcularValoresFactura(Factura factura);

  Factura procesarFactura(Factura factura);

  Map<Long, BigDecimal> getIdsProductosYCantidades(Factura factura);

  void validarReglasDeNegocio(Factura factura);

  void aplicarBonificacion(
          RenglonFactura nuevoRenglon, Producto producto, boolean aplicaBonificacion);

  boolean marcarRenglonParaAplicarBonificacion(long idProducto, BigDecimal cantidad);
}
