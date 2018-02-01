package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.AjusteCuentaCorriente;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.Nota;


public interface AjusteCuentaCorrienteRepository extends PagingAndSortingRepository<AjusteCuentaCorriente, Long> {
    
    AjusteCuentaCorriente findByNumAjusteAndEliminado(long nroAjuste, boolean eliminado);
    
    @Query("SELECT a.monto FROM AjusteCuentaCorriente a WHERE a.idAjusteCuentaCorriente = :idAjusteCuentaCorriente AND a.eliminado = false")
    BigDecimal getMontoById(@Param("idAjusteCuentaCorriente") long idAjusteCuentaCorriente);
    
    AjusteCuentaCorriente findTopByEmpresaAndNumSerieOrderByNumAjusteDesc(Empresa empresa, long numSerie);
    
    Page<AjusteCuentaCorriente> findAllByFechaBetweenAndClienteAndEmpresaAndEliminado(Date desde, Date hasta, Cliente cliente, Empresa empresa, boolean eliminado, Pageable page);
    
    AjusteCuentaCorriente findByNotaDebitoAndEliminado(Nota notaDebito, boolean eliminado);
    
    @Query("SELECT a FROM AjusteCuentaCorriente a WHERE a.idAjusteCuentaCorriente = :idAjusteCuentaCorriente AND a.eliminado = false")
    AjusteCuentaCorriente findById(@Param("idAjusteCuentaCorriente") long idAjusteCuentaCorriente);
    
}
