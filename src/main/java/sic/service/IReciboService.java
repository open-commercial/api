package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.*;

public interface IReciboService {
    
    Recibo getById(long idRecibo);

    Page<Recibo> buscarRecibos(BusquedaReciboCriteria criteria, Movimiento movimiento);
    
    Recibo guardar(Recibo recibo);
    
    List<Recibo> construirRecibos(long[] formaDePago, Empresa empresa, Cliente cliente, Usuario usuario, BigDecimal[] monto, BigDecimal totalFactura, Date fecha);
    
    long getSiguienteNumeroRecibo(long idEmpresa, long serie);
    
    void eliminar(long idRecibo);
    
    byte[] getReporteRecibo(Recibo recibo);

    BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(long idEmpresa, long idFormaDePago, Date desde, Date hasta);

    BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(long idEmpresa,long idFormaDePago, Date desde, Date hasta);

    List<Recibo> getRecibosEntreFechasPorFormaDePago(Date desde, Date hasta, FormaDePago formaDePago, Empresa empresa);

    BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(long idEmpresa, Date desde, Date hasta);

    BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(long idEmpresa, Date desde, Date hasta);

    BigDecimal getTotalRecibosClientesEntreFechas(long idEmpresa, Date desde, Date hasta);

    BigDecimal getTotalRecibosProveedoresEntreFechas(long idEmpresa, Date desde, Date hasta);

    BigDecimal calcularMontosRecibos(BusquedaReciboCriteria criteria);
}
