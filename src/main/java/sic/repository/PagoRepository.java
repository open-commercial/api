package sic.repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.FormaDePago;
import sic.modelo.Nota;
import sic.modelo.Pago;

public interface PagoRepository extends PagingAndSortingRepository<Pago, Long> {

    @Query("SELECT p FROM Pago p WHERE p.id_Pago = :idPago AND p.eliminado = false")
    Pago findById(@Param("idPago") long idPago);
    
    List<Pago> findByFacturaAndEliminado(Factura factura, boolean eliminado);

    List<Pago> findByFechaBetweenAndEmpresaAndFormaDePagoAndEliminado(Date desde, Date hasta, Empresa empresa, FormaDePago formaDePago, boolean eliminado);

    @Query("SELECT p FROM FacturaVenta fv INNER JOIN fv.pagos p WHERE fv.cliente.id_Cliente = :idCliente AND p.eliminado = false AND p.fecha BETWEEN :desde AND :hasta")
    Page<Pago> getPagosPorClienteEntreFechas(@Param("idCliente") long idCliente, @Param("desde") Date desde, @Param("hasta") Date hasta, Pageable page);
    
    Pago findTopByEmpresaOrderByNroPagoDesc(Empresa empresa);
    
    @Query("SELECT SUM(p.monto) FROM FacturaVenta fv INNER JOIN fv.pagos p WHERE fv.cliente.id_Cliente = :idCliente AND p.eliminado = false AND p.fecha <= :hasta")
    Double getSaldoPagosPorCliente(@Param("idCliente") long idCliente, @Param("hasta") Date hasta);
    
    List<Pago> findByNotaDebitoAndEliminado(Nota nota, boolean eliminado);
    
    @Query("SELECT SUM(p.monto) FROM Factura f INNER JOIN f.pagos p WHERE f.id_Factura = :idFactura AND p.eliminado = false")
    Double getTotalPagosDeFactura(@Param("idFactura") long idFactura);
    
    @Query("SELECT SUM(p.monto) FROM Pago p INNER JOIN p.notaDebito n WHERE n.idNota = :idNota AND p.eliminado = false")
    Double getTotalPagosDeNota(@Param("idNota") long idNota);
    
}