package sic.repository;

import java.util.Date;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.NotaDebito;
import sic.modelo.TipoDeComprobante;

public interface NotaDebitoRepository extends NotaRepository<NotaDebito> {
       
    @Query("SELECT nd FROM NotaDebito nd WHERE nd.idNota= :idNotaDebito AND nd.eliminada = false")
    NotaDebito getById(@Param("idNotaDebito") long idNotaDebito);
    
    @Query("SELECT SUM(nd.total) FROM NotaDebito nd WHERE nd.empresa = :empresa AND nd.cliente = :cliente AND nd.eliminada = false AND nd.fecha <= :hasta")
    Double totalNotasDebito(@Param("hasta") Date hasta, @Param("cliente") Cliente cliente, @Param("empresa") Empresa empresa);
    
    NotaDebito findTopByEmpresaAndTipoComprobanteOrderByNroNotaDesc(Empresa empresa, TipoDeComprobante tipoComprobante);
    
    NotaDebito findByPagoIdAndEliminada(Long pagoId, boolean eliminada);
    
}
