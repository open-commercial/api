package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.NotaDebitoCliente;
import sic.modelo.NotaDebitoProveedor;
import sic.modelo.Proveedor;
import sic.modelo.Recibo;
import sic.modelo.TipoDeComprobante;

public interface NotaDebitoProveedorRepository extends NotaDebitoRepository<NotaDebitoCliente> {
    
    @Query("SELECT ndp FROM NotaDebitoProveedor ndp WHERE ndp.idNota = :idNotaDebitoProveedor AND ndp.eliminada = false")
    NotaDebitoProveedor getById(@Param("idNotaDebitoProveedor") long idNotaDebitoProveedor);
    
    @Query("SELECT max(ndp.nroNota) FROM NotaDebitoProveedor ndp WHERE ndp.tipoComprobante = :tipoComprobante AND ndp.serie = :serie AND ndp.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumNotaDebitoSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);
    
    List<NotaDebitoProveedor> findAllByProveedorAndEmpresaAndEliminada(Proveedor proveedor, Empresa empresa, boolean eliminada);
    
    boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);
    
}
