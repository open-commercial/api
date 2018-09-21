package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.NotaDebitoProveedor;
import sic.modelo.Recibo;

public interface NotaDebitoProveedorRepository extends NotaDebitoRepository<NotaDebitoProveedor>, NotaDebitoProveedorRepositoryCustom, QueryDslPredicateExecutor<NotaDebitoProveedor> {
    
    @Query("SELECT ndp FROM NotaDebitoProveedor ndp WHERE ndp.idNota = :idNotaDebitoProveedor AND ndp.eliminada = false")
    NotaDebitoProveedor getById(@Param("idNotaDebitoProveedor") long idNotaDebitoProveedor);
    
    boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);
    
}
