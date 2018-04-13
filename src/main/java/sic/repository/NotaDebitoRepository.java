package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.NotaDebito;
import sic.modelo.TipoDeComprobante;

public interface NotaDebitoRepository<T extends NotaDebito> extends NotaRepository<NotaDebito> {

    @Query("SELECT nd FROM NotaDebito nd WHERE nd.idNota= :idNotaDebito AND nd.eliminada = false")
    NotaDebito getById(@Param("idNotaDebito") long idNotaDebito);

    NotaDebito findTopByEmpresaAndTipoComprobanteAndSerieOrderByNroNotaDesc(Empresa empresa, TipoDeComprobante tipoComprobante, long serie);

}
