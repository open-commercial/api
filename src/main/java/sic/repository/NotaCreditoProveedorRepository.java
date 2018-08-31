package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.FacturaCompra;
import sic.modelo.NotaCredito;
import sic.modelo.NotaCreditoProveedor;
import sic.modelo.Proveedor;
import sic.modelo.TipoDeComprobante;

public interface NotaCreditoProveedorRepository extends NotaCreditoRepository<NotaCreditoProveedor>, QueryDslPredicateExecutor<NotaCreditoProveedor> {
    
    @Query("SELECT ncp FROM NotaCreditoProveedor ncp WHERE ncp.idNota = :idNotaCreditoProveedor AND ncp.eliminada = false")
    NotaCreditoProveedor getById(@Param("idNotaCreditoProveedor") long idNotaCreditoProveedor);
    
    @Query("SELECT max(ncp.nroNota) FROM NotaCreditoProveedor ncp WHERE ncp.tipoComprobante = :tipoComprobante AND ncp.serie = :serie AND ncp.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumNotaCreditoSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);
    
    List<NotaCredito> findAllByProveedorAndEmpresaAndEliminada(Proveedor proveedor, Empresa empresa, boolean eliminada);
    
    List<NotaCredito> findAllByFacturaCompraAndEliminada(FacturaCompra factura, boolean eliminada);
    
    boolean existsByFacturaCompraAndEliminada(FacturaCompra facturaCompra, boolean eliminada);
    
}
