package sic.repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.TipoDeComprobante;

public interface NotaRepository<T extends Nota> extends PagingAndSortingRepository<T, Long> {
    
    @Query("SELECT n FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
    Nota findById(@Param("idNota") long idNota);
    
    @Query("SELECT n.total FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
    Double getTotalById(@Param("idNota") long idNota);
    
    Nota findByTipoComprobanteAndNroNotaAndClienteAndEliminada(TipoDeComprobante tipoDeComprobante, long nroNota, Cliente cliente, boolean eliminada);
    
    List<Nota> findAllByClienteAndEmpresaAndEliminada(Cliente cliente, Empresa empresa, boolean eliminada);
    
    Page<Nota> findAllByFechaBetweenAndClienteAndEmpresaAndEliminada(Date desde, Date hasta, Cliente cliente, Empresa empresa, boolean eliminada, Pageable page);
            
    List<Nota> findAllByFacturaVentaAndEliminada(FacturaVenta facturaVenta, boolean eliminada);
    
    @Query("SELECT n.CAE FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
    Long getCAEById(@Param("idNota") long idNota);
   
    @Query("SELECT n FROM Pago p INNER JOIN p.notaDebito n WHERE p.id_Pago = :idPago AND n.eliminada = false")
    Nota getNotaDelPago(@Param("idPago") long idPago);

    boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta, boolean eliminada);
    
}
