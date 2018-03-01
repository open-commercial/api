package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.NotaCreditoProveedor;

public interface NotaCreditoProveedorRepository extends NotaCreditoRepository<NotaCreditoProveedor> {
    
    @Query("SELECT ncp FROM NotaCreditoProveedor ncp WHERE ncp.idNota = :idNotaCreditoProveedor AND ncp.eliminada = false")
    NotaCreditoProveedor getById(@Param("idNotaCreditoProveedor") long idNotaCreditoProveedor);
    
}
