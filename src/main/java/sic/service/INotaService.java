package sic.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import sic.modelo.*;

public interface INotaService {

  Nota guardarNotaCredito(NotaCredito nota);

  Nota guardarNotaDebito(NotaDebito nota);

  Nota autorizarNota(Nota nota);

  byte[] getReporteNota(Nota nota);

  Nota getNotaPorId(Long idNota);

  Page<Nota> buscarNotas(BusquedaNotaCriteria busquedaNotaCriteria, long idUsuarioLoggedIn);

  Factura getFacturaDeLaNotaCredito(Long idNota);

  boolean existsNotaDebitoPorRecibo(Recibo recibo);

  boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta);

  List<NotaCredito> getNotasCreditoPorFactura(Long idFactura);

  long getSiguienteNumeroNotaDebitoCliente(Long idEmpresa, TipoDeComprobante tipoComprobante);

  long getSiguienteNumeroNotaCreditoCliente(Long idEmpresa, TipoDeComprobante tipoComprobante);

  TipoDeComprobante[] getTipoNotaCliente(Long idCliente, Long idEmpresa);

  TipoDeComprobante[] getTiposNota(Empresa empresa);

  List<RenglonNotaCredito> getRenglonesDeNotaCredito(Long idNota);

  List<RenglonNotaDebito> getRenglonesDeNotaDebito(Long idNota);

  List<RenglonFactura> getRenglonesFacturaModificadosParaNotaCredito(long idFactura);

  BigDecimal calcularTotalNota(List<RenglonNotaCredito> renglonesNota);

  BigDecimal getIvaNetoNota(Long idNota);

  List<RenglonNotaCredito> calcularRenglonCredito(
      TipoDeComprobante tipo, Map<Long, BigDecimal> idsYCantidades);

  List<RenglonNotaDebito> calcularRenglonDebito(
      long idRecibo, BigDecimal monto, BigDecimal ivaPorcentaje);

  BigDecimal calcularSubTotalCredito(BigDecimal[] importesBrutos);

  BigDecimal calcularDecuentoNetoCredito(BigDecimal subTotal, BigDecimal descuentoPorcentaje);

  BigDecimal calcularRecargoNetoCredito(BigDecimal subTotal, BigDecimal recargoPorcentaje);

  BigDecimal calcularSubTotalBrutoCredito(
      TipoDeComprobante tipoDeComprobante,
      BigDecimal subTotal,
      BigDecimal recargoNeto,
      BigDecimal descuentoNeto,
      BigDecimal iva105Neto,
      BigDecimal iva21Neto);

  BigDecimal calcularIVANetoCredito(
      TipoDeComprobante tipoDeComprobante,
      BigDecimal[] cantidades,
      BigDecimal[] ivaPorcentajeRenglones,
      BigDecimal[] ivaNetoRenglones,
      BigDecimal ivaPorcentaje,
      BigDecimal descuentoPorcentaje,
      BigDecimal recargoPorcentaje);

  BigDecimal calcularTotalCredito(
      BigDecimal subTotalBruto, BigDecimal iva105Neto, BigDecimal iva21Neto);

  BigDecimal calcularTotalDebito(
      BigDecimal subTotalBruto, BigDecimal iva21Neto, BigDecimal montoNoGravado);

  BigDecimal calcularTotalCredito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

  BigDecimal calcularTotalDebito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

  BigDecimal calcularTotalIVACredito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

  BigDecimal calcularTotalIVADebito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);
}
