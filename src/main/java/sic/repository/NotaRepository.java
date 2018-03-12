package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Nota;
import sic.modelo.TipoDeComprobante;

public interface NotaRepository<T extends Nota> extends PagingAndSortingRepository<T, Long> {
    
    @Query("SELECT n FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
    Nota findById(@Param("idNota") long idNota);
    
    @Query("SELECT n.total FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
    BigDecimal getTotalById(@Param("idNota") long idNota);
    
    Nota findByTipoComprobanteAndNroNotaAndEliminada(TipoDeComprobante tipoDeComprobante, long nroNota, boolean eliminada);
    
    List<Nota> findAllByEmpresaAndEliminada(Empresa empresa, boolean eliminada);
    
    Page<Nota> findAllByFechaBetweenAndEmpresaAndEliminada(Date desde, Date hasta, Empresa empresa, boolean eliminada, Pageable page);
    
    @Query("SELECT n.CAE FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
    Long getCAEById(@Param("idNota") long idNota);
    
}
