package sic.service;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.Usuario;

public interface IReciboService {
    
    Recibo getById(long idRecibo);
    
    Recibo getReciboDelPago(long idPago);
    
    Double getMontoById(long idRecibo);
    
    Recibo guardar(Recibo recibo);
    
    Recibo actualizarSaldoSobrante(long idRecibo, double monto);
    
    List<Recibo> construirRecibos(long[] formaDePago, Empresa empresa, Cliente cliente, Usuario usuario, double[] monto, double totalFactura, Date fecha);
    
    long getSiguienteNumeroRecibo(long idEmpresa, long serie);
    
    void eliminar(long idRecibo);
    
    List<Recibo> getByClienteAndEmpresaAndEliminado(Cliente cliente, Empresa empresa, boolean eliminado);
    
    List<Recibo> getByUsuarioAndEmpresaAndEliminado(Usuario usuario, Empresa empresa, boolean eliminado);
    
    Page<Recibo> getByFechaBetweenAndClienteAndEmpresaAndEliminado(Date desde, Date hasta, Cliente cliente, Empresa empresa, boolean eliminado, Pageable page);
    
    List<Recibo> getByFechaBetweenAndFormaDePagoAndEmpresaAndEliminado(Date desde, Date hasta, FormaDePago formaDePago, Empresa empresa);
    
    double pagarMultiplesComprobantesCliente(List<RenglonCuentaCorriente> renglonesCC, Recibo recibo, double monto, FormaDePago formaDePago, String nota);
    
    double pagarMultiplesComprobantesProveedor(List<RenglonCuentaCorriente> renglonesCC, Recibo recibo, double monto, FormaDePago formaDePago, String nota);
    
    List<Recibo> getRecibosConSaldoSobrante(long idEmpresa, long idCliente);
    
    byte[] getReporteRecibo(Recibo recibo);    
    
}
