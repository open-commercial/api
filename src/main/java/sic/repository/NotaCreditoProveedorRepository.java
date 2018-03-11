package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.FacturaCompra;
import sic.modelo.NotaCreditoProveedor;
import sic.modelo.TipoDeComprobante;

public interface NotaCreditoProveedorRepository extends NotaCreditoRepository<NotaCreditoProveedor> {
    
    @Query("SELECT ncp FROM NotaCreditoProveedor ncp WHERE ncp.idNota = :idNotaCreditoProveedor AND ncp.eliminada = false")
    NotaCreditoProveedor getById(@Param("idNotaCreditoProveedor") long idNotaCreditoProveedor);
    
    @Query("SELECT max(ncp.nroNota) FROM NotaCreditoProveedor ncp WHERE ncp.tipoComprobante = :tipoComprobante AND ncp.serie = :serie AND ncp.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumNotaCreditoSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);
    
    List<NotaCreditoProveedor> findAllByFacturaCompraAndEliminada(FacturaCompra factura, boolean eliminada);
    
    boolean existsByFacturaCompraAndEliminada(FacturaCompra facturaCompra, boolean eliminada);
    
}
