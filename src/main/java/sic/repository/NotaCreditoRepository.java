package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.NotaCredito;
import sic.modelo.TipoDeComprobante;

public interface NotaCreditoRepository<T extends NotaCredito> extends PagingAndSortingRepository<T, Long> { 
    
    @Query("SELECT nc FROM NotaCredito nc WHERE nc.idNota = :idNotaCredito AND nc.eliminada = false")
    NotaCredito getById(@Param("idNotaCredito") long idNotaCredito);
    
    NotaCredito findTopByEmpresaAndTipoComprobanteAndSerieOrderByNroNotaDesc(Empresa empresa, TipoDeComprobante tipoComprobante, long serie);
    
}
