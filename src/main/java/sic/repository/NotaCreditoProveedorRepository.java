package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.FacturaCompra;
import sic.modelo.NotaCredito;
import sic.modelo.NotaCreditoProveedor;

public interface NotaCreditoProveedorRepository extends NotaCreditoRepository<NotaCreditoProveedor>, QueryDslPredicateExecutor<NotaCreditoProveedor> {
    
    @Query("SELECT ncp FROM NotaCreditoProveedor ncp WHERE ncp.idNota = :idNotaCreditoProveedor AND ncp.eliminada = false")
    NotaCreditoProveedor getById(@Param("idNotaCreditoProveedor") long idNotaCreditoProveedor);
    
    List<NotaCredito> findAllByFacturaCompraAndEliminada(FacturaCompra factura, boolean eliminada);
    
}
