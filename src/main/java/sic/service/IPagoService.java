package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.Factura;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.FormaDePago;
import sic.modelo.Pago;

public interface IPagoService {
    
    Pago getPagoPorId(long id_pago);

    List<Pago> getPagosDeLaFactura(long idFactura);
    
    Double getTotalPagosDeLaFactura(long idFactura);

    BigDecimal getSaldoAPagarFactura(long idFactura); 
    
    BigDecimal getSaldoAPagarNotaDebito(long idNota);
    
    long getSiguienteNroPago(Long idEmpresa);

    List<Pago> getPagosCompraEntreFechasYFormaDePago(long id_Empresa, long id_FormaDePago, Date desde, Date hasta);
    
    List<Pago> getPagosDeNotas(long idNota);
    
    BigDecimal getTotalPagosDeNota(long idNota);
    
    Page<Pago> getPagosPorClienteEntreFechas(long idCliente, Date desde, Date hasta, Pageable page);
    
    List<Pago> getPagosRelacionadosAlRecibo(long idRecibo);
    
    double calcularTotalPagos(List<Pago> pagos);
    
    BigDecimal calcularTotalAdeudadoFacturasVenta(List<FacturaVenta> facturasVenta);
    
    BigDecimal calcularTotalAdeudadoFacturasCompra(List<FacturaCompra> facturasCompra);
    
    BigDecimal calcularTotalAdeudadoFacturas(List<Factura> facturas);
    
    double getSaldoPagosPorCliente(long idCliente, Date hasta);
            
    void pagarMultiplesFacturasCompra(List<Factura> facturas, BigDecimal monto, FormaDePago formaDePago, String nota);
    
    void validarOperacion(Pago pago);     

    Pago guardar(Pago pago);

    void eliminar(long idPago);
    
    void eliminarPagoDeCompra(long idPago);
    
}
