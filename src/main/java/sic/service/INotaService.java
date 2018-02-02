package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaNotaCriteria;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.NotaDebito;
import sic.modelo.Pago;
import sic.modelo.Recibo;
import sic.modelo.RenglonFactura;
import sic.modelo.RenglonNotaCredito;
import sic.modelo.RenglonNotaDebito;
import sic.modelo.TipoDeComprobante;

public interface INotaService {

    Nota guardarNota(Nota nota, long idEmpresa, long idCliente, long idUsuario, Long idRecibo, Long idFactura, boolean modificarStock);

    Nota autorizarNota(Nota nota);
    
    byte[] getReporteNota(Nota nota);

    Nota getNotaPorId(Long idNota);
    
    Long getCAEById(Long idNota);
    
    BigDecimal getTotalById(Long idNota);
    
    FacturaVenta getFacturaNota(Long idNota);
    
    Nota getNotaDelPago(long idPago);
    
    List<Pago> getPagosNota(Long idNota);
    
    boolean existeNotaDebitoPorRecibo(Recibo recibo);
    
    boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta);
    
    BigDecimal getTotalPagado(Long idNota);

    List<Nota> getNotasPorFactura(Long idFactura);

    Page<Nota> buscarNotasPorClienteYEmpresa(BusquedaNotaCriteria criteria);

    List<Nota> getNotasPorClienteYEmpresa(Long idCliente, Long idEmpresa);

    long getSiguienteNumeroNotaDebito(Long idEmpresa, TipoDeComprobante tipoComprobante);

    long getSiguienteNumeroNotaCredito(Long idEmpresa, TipoDeComprobante tipoComprobante);

    BigDecimal getSaldoNotas(Date hasta, Long idCliente, Long idEmpresa);

    TipoDeComprobante[] getTipoNota(Long idCliente, Long idEmpresa);

    List<RenglonNotaCredito> getRenglonesDeNotaCredito(Long idNota);

    List<RenglonNotaDebito> getRenglonesDeNotaDebito(Long idNota);
    
    List<RenglonFactura> getRenglonesFacturaModificadosParaNotaCredito(long idFactura);

    void eliminarNota(long[] idNota);

    BigDecimal calcularTotalNota(List<RenglonNotaCredito> renglonesNota);

    BigDecimal getIvaNetoNota(Long idNota);

    List<RenglonNotaCredito> calcularRenglonCredito(TipoDeComprobante tipo, BigDecimal[] cantidad, long[] idRenglonFactura);

    List<RenglonNotaDebito> calcularRenglonDebito(long idRecibo, BigDecimal monto, BigDecimal ivaPorcentaje);

    BigDecimal calcularSubTotalCredito(BigDecimal[] importesBrutos);

    BigDecimal calcularDecuentoNetoCredito(BigDecimal subTotal, BigDecimal descuentoPorcentaje);

    BigDecimal calcularRecargoNetoCredito(BigDecimal subTotal, BigDecimal recargoPorcentaje);

    BigDecimal calcularSubTotalBrutoCredito(TipoDeComprobante tipoDeComprobante, BigDecimal subTotal, BigDecimal recargoNeto, BigDecimal descuentoNeto, BigDecimal iva105_neto, BigDecimal iva21_neto);

    BigDecimal calcularIVANetoCredito(TipoDeComprobante tipoDeComprobante, BigDecimal[] cantidades, BigDecimal[] ivaPorcentajeRenglones, BigDecimal[] ivaNetoRenglones, BigDecimal ivaPorcentaje, BigDecimal descuentoPorcentaje, BigDecimal recargoPorcentaje);

    BigDecimal calcularTotalCredito(BigDecimal subTotal_bruto, BigDecimal iva105_neto, BigDecimal iva21_neto);
    
    BigDecimal calcularTotalDebito(BigDecimal subTotal_bruto, BigDecimal iva21_neto, BigDecimal montoNoGravado);
    
    BigDecimal calcularTotaCreditoPorFacturaVenta(FacturaVenta facturaVenta);
    
    Nota actualizarNotaDebitoEstadoPago(NotaDebito notaDebito);   
    
}
