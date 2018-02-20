package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FacturaVenta;
import sic.modelo.NotaCredito;
import sic.modelo.TipoDeComprobante;

public interface NotaCreditoRepository extends NotaRepository<NotaCredito> { 
    
    @Query("SELECT nc FROM NotaCredito nc WHERE nc.idNota = :idNotaCredito AND nc.eliminada = false")
    NotaCredito getById(@Param("idNotaCredito") long idNotaCredito);
    
    @Query("SELECT SUM(nc.total) FROM NotaCredito nc WHERE nc.empresa = :empresa AND nc.cliente = :cliente AND nc.eliminada = false AND nc.fecha <= :hasta")
    BigDecimal getTotalNotasCredito(@Param("hasta") Date hasta, @Param("cliente") Cliente cliente, @Param("empresa") Empresa empresa);
    
    @Query("SELECT SUM(nc.total) FROM NotaCredito nc WHERE nc.facturaVenta = :facturaVenta AND nc.eliminada = false")
    BigDecimal getTotalNotasCreditoPorFacturaVenta(@Param("facturaVenta") FacturaVenta facturaVenta);
    
    NotaCredito findTopByEmpresaAndTipoComprobanteOrderByNroNotaDesc(Empresa empresa, TipoDeComprobante tipoComprobante);
    
}
