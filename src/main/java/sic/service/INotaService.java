package sic.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import com.querydsl.core.BooleanBuilder;
import sic.modelo.*;

public interface INotaService {

    Nota guardarNotaCliente(Nota nota, long idEmpresa, long idCliente, long idUsuario, Long idRecibo, Long idFactura, boolean modificarStock);
    
    Nota guardarNotaProveedor(Nota nota, long idEmpresa, long idProveedor, long idUsuario, Long idRecibo, Long idFactura, boolean modificarStock);

    Nota autorizarNota(Nota nota);
    
    byte[] getReporteNota(Nota nota);

    Nota getNotaPorId(Long idNota);
    
    Factura getFacturaNotaCredito(Long idNota);
    
    boolean existeNotaDebitoPorRecibo(Recibo recibo);
    
    boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta);

    List<NotaCredito> getNotasCreditoPorFactura(Long idFactura);

    Page<NotaCreditoCliente> buscarNotaCreditoCliente(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

    Page<NotaCreditoProveedor> buscarNotaCreditoProveedor(BusquedaNotaCriteria criteria);

    Page<NotaDebitoCliente> buscarNotaDebitoCliente(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

    Page<NotaDebitoProveedor> buscarNotaDebitoProveedor(BusquedaNotaCriteria criteria);

    BigDecimal calcularTotalNotaCreditoCliente(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

    BigDecimal calcularIVANotaCreditoCliente(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

    BigDecimal calcularTotalNotaCreditoProveedor(BusquedaNotaCriteria criteria);

    BigDecimal calcularIVANotaCreditoProveedor(BusquedaNotaCriteria criteria);

    BigDecimal calcularTotalNotaDebitoCliente(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

    BigDecimal calcularIVANotaDebitoCliente(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn);

    BigDecimal calcularTotalNotaDebitoProveedor(BusquedaNotaCriteria criteria);

    BigDecimal calcularIVANotaDebitoProveedor(BusquedaNotaCriteria criteria);

    TipoDeComprobante[] getTipoNotaCredito(Empresa empresa);

    TipoDeComprobante[] getTipoNotaDebito(Empresa empresa);

    long getSiguienteNumeroNotaDebitoCliente(Long idEmpresa, TipoDeComprobante tipoComprobante);

    long getSiguienteNumeroNotaCreditoCliente(Long idEmpresa, TipoDeComprobante tipoComprobante);

    TipoDeComprobante[] getTipoNotaCliente(Long idCliente, Long idEmpresa);

    List<RenglonNotaCredito> getRenglonesDeNotaCreditoCliente(Long idNota);

    List<RenglonNotaDebito> getRenglonesDeNotaDebitoCliente(Long idNota);
    
    List<RenglonNotaCredito> getRenglonesDeNotaCreditoProveedor(Long idNota);

    List<RenglonNotaDebito> getRenglonesDeNotaDebitoProveedor(Long idNota);
    
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
    
}
