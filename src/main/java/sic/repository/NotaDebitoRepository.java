package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

import java.util.List;

public interface NotaDebitoRepository
    extends NotaRepository<NotaDebito>, NotaDebitoRepositoryCustom {

  @Query(
      "SELECT max(nd.nroNota) FROM NotaDebito nd "
          + "WHERE nd.tipoComprobante = :tipoComprobante AND nd.serie = :serie AND nd.empresa.id_Empresa = :idEmpresa "
          + "AND nd.cliente IS NOT null")
  Long buscarMayorNumNotaDebitoClienteSegunTipo(
      @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
      @Param("serie") long serie,
      @Param("idEmpresa") long idEmpresa);

  boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);

  List<NotaDebito> findTop2ByTipoComprobanteAndEliminadaAndEmpresaOrderByFechaDesc(TipoDeComprobante tipo, boolean eliminada, Empresa empresa);
}
