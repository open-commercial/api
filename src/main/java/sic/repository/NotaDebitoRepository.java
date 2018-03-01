package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.NotaDebito;
import sic.modelo.Recibo;
import sic.modelo.TipoDeComprobante;

public interface NotaDebitoRepository extends NotaRepository<NotaDebito> {
       
    @Query("SELECT nd FROM NotaDebito nd WHERE nd.idNota= :idNotaDebito AND nd.eliminada = false")
    NotaDebito getById(@Param("idNotaDebito") long idNotaDebito);
    
    NotaDebito findTopByEmpresaAndTipoComprobanteAndSerieOrderByNroNotaDesc(Empresa empresa, TipoDeComprobante tipoComprobante, long serie);
    
    boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);

}
