package sic.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import sic.modelo.*;

import javax.validation.Valid;

public interface INotaService {

  Nota guardarNotaCredito(@Valid NotaCredito nota);

  Nota guardarNotaDebito(@Valid NotaDebito nota);

  Nota autorizarNota(Nota nota);

  byte[] getReporteNota(Nota nota);

  Nota getNotaNoEliminadaPorId(long idNota);

  Page<Nota> buscarNotas(BusquedaNotaCriteria busquedaNotaCriteria, long idUsuarioLoggedIn);

  Factura getFacturaDeLaNotaCredito(Long idNota);

  boolean existsNotaDebitoPorRecibo(Recibo recibo);

  List<NotaCredito> getNotasCreditoPorFactura(Long idFactura);

  long getSiguienteNumeroNotaDebitoCliente(Long idEmpresa, TipoDeComprobante tipoComprobante);

  long getSiguienteNumeroNotaCreditoCliente(Long idEmpresa, TipoDeComprobante tipoComprobante);

  TipoDeComprobante[] getTipoNotaCreditoCliente(Long idCliente, Long idEmpresa);

  TipoDeComprobante[] getTipoNotaDebitoCliente(Long idCliente, Long idEmpresa);

  TipoDeComprobante[] getTiposNota(Empresa empresa);

  TipoDeComprobante getTipoDeNotaCreditoSegunFactura(TipoDeComprobante tipo);

  List<RenglonNotaCredito> getRenglonesDeNotaCredito(Long idNota);

  List<RenglonNotaDebito> getRenglonesDeNotaDebito(long idNota);

  List<RenglonFactura> getRenglonesFacturaModificadosParaNotaCredito(long idFactura);

  BigDecimal calcularTotalNota(List<RenglonNotaCredito> renglonesNota);

  List<RenglonNotaCredito> calcularRenglonCreditoProducto(
      TipoDeComprobante tipo, BigDecimal[] cantidad, Long[] idRenglonFactura);

  RenglonNotaCredito calcularRenglonCredito(TipoDeComprobante tipo, String Detalle, BigDecimal monto);

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
