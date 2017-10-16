package sic.service;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaNotaCriteria;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.NotaDebito;
import sic.modelo.Pago;
import sic.modelo.RenglonFactura;
import sic.modelo.RenglonNotaCredito;
import sic.modelo.RenglonNotaDebito;
import sic.modelo.TipoDeComprobante;

public interface INotaService {

    Nota guardarNota(Nota nota, long idEmpresa, long idCliente, long idUsuario, Long idFactura, Long idPago, boolean modificarStock);

    Nota autorizarNota(Nota nota);
    
    byte[] getReporteNota(Nota nota);

    Nota getNotaPorId(Long idNota);
    
    Long getCAEById(Long idNota);
    
    Double getTotalById(Long idNota);
    
    FacturaVenta getFacturaNota(Long idNota);

    NotaDebito getNotaDebitoPorPago(Long idPago);
    
    List<Pago> getPagosNota(Long idNota);
    
    double getTotalPagado(Long idNota);

    List<Nota> getNotasPorFactura(Long idFactura);

    Page<Nota> buscarNotasPorClienteYEmpresa(BusquedaNotaCriteria criteria);

    List<Nota> getNotasPorClienteYEmpresa(Long idCliente, Long idEmpresa);

    long getSiguienteNumeroNotaDebito(Long idCliente, Long idEmpresa);

    long getSiguienteNumeroNotaCredito(Long idCliente, Long idEmpresa);

    double getSaldoNotas(Date hasta, Long idCliente, Long idEmpresa);

    TipoDeComprobante[] getTipoNota(Long idCliente, Long idEmpresa);

    List<RenglonNotaCredito> getRenglonesDeNotaCredito(Long idNota);

    List<RenglonNotaDebito> getRenglonesDeNotaDebito(Long idNota);
    
    List<RenglonFactura> getRenglonesFacturaModificadosParaNotaCredito(long idFactura);

    void eliminarNota(long[] idNota);

    double calcularTotalNota(List<RenglonNotaCredito> renglonesNota);

    double getIvaNetoNota(Long idNota);

    List<RenglonNotaCredito> calcularRenglonCredito(TipoDeComprobante tipo, double[] cantidad, long[] idRenglonFactura);

    List<RenglonNotaDebito> calcularRenglonDebito(Long idPago, double monto, double ivaPorcentaje);

    double calcularSubTotal(double[] importesBrutos);

    double calcularDecuentoNeto(double subTotal, double descuentoPorcentaje);

    double calcularRecargoNeto(double subTotal, double recargoPorcentaje);

    double calcularSubTotalBruto(TipoDeComprobante tipoDeComprobante, double subTotal, double recargoNeto, double descuentoNeto, double iva105_neto, double iva21_neto);

    double calcularIVANeto(TipoDeComprobante tipoDeComprobante, double[] cantidades, double[] ivaPorcentajeRenglones, double[] ivaNetoRenglones, double ivaPorcentaje, double descuentoPorcentaje, double recargoPorcentaje);

    double calcularTotal(double subTotal_bruto, double iva105_neto, double iva21_neto);
}
