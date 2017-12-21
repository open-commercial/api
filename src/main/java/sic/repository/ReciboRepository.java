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
import sic.modelo.Recibo;
import sic.modelo.Usuario;

public interface ReciboRepository extends PagingAndSortingRepository<Recibo, Long> {
    
    @Query("SELECT r FROM Recibo r WHERE r.idRecibo= :idRecibo AND r.eliminado = false")
    Recibo findById(@Param("idRecibo") long idRecibo);
    
    @Query("SELECT r.monto FROM Recibo r WHERE r.idRecibo= :idRecibo AND r.eliminado = false")
    Double getMontoById(@Param("idRecibo") long idRecibo);
    
    List<Recibo> findAllByClienteAndEmpresaAndEliminado(Cliente cliente, Empresa empresa, boolean eliminado);
    
    List<Recibo> findAllByUsuarioAndEmpresaAndEliminado(Usuario usuario, Empresa empresa, boolean eliminado);
    
    Page<Recibo> findAllByFechaBetweenAndClienteAndEmpresaAndEliminado(Date desde, Date hasta, Cliente cliente, Empresa empresa, boolean eliminado, Pageable page);
    
}
