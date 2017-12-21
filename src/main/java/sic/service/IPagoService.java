package sic.service;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.Factura;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.FormaDePago;
import sic.modelo.Pago;
import sic.modelo.Recibo;

public interface IPagoService {
    
    Pago getPagoPorId(long id_pago);

    List<Pago> getPagosDeLaFactura(long idFactura);
    
    Double getTotalPagosDeLaFactura(long idFactura);

    double getSaldoAPagarFactura(long idFactura); 
    
    double getSaldoAPagarNotaDebito(long idNota);
    
    long getSiguienteNroPago(Long idEmpresa);

    List<Pago> getPagosEntreFechasYFormaDePago(long id_Empresa, long id_FormaDePago, Date desde, Date hasta);
    
    List<Pago> getPagosDeNotas(long idNota);
    
    Double getTotalPagosDeNota(long idNota);
    
    Page<Pago> getPagosPorClienteEntreFechas(long idCliente, Date desde, Date hasta, Pageable page);
    
    List<Pago> getPagosRelacionadosAlRecibo(Recibo recibo);
    
    double calcularTotalPagos(List<Pago> pagos);
    
    double calcularTotalAdeudadoFacturasVenta(List<FacturaVenta> facturasVenta);
    
    double calcularTotalAdeudadoFacturasCompra(List<FacturaCompra> facturasCompra);
    
    double calcularTotalAdeudadoFacturas(List<Factura> facturas);
    
    double getSaldoPagosPorCliente(long idCliente, Date hasta);
            
    void pagarMultiplesFacturas(List<Factura> facturas, double monto, FormaDePago formaDePago, String nota);
    
    void validarOperacion(Pago pago);     

    Pago guardar(Pago pago);

    void eliminar(long idPago);
}
