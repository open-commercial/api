package org.opencommercial.repository;

import org.opencommercial.model.NotaDebito;
import org.opencommercial.model.Recibo;
import org.opencommercial.model.TipoDeComprobante;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface NotaDebitoRepository extends
        NotaRepository<NotaDebito>,
        NotaDebitoRepositoryCustom,
        QuerydslPredicateExecutor<NotaDebito> {

  @Query("SELECT max(nd.nroNota) FROM NotaDebito nd "
          + "WHERE nd.tipoComprobante = :tipoComprobante AND nd.serie = :serie AND nd.sucursal.idSucursal = :idSucursal "
          + "AND nd.cliente IS NOT null")
  Long buscarMayorNumNotaDebitoClienteSegunTipo(
      @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
      @Param("serie") long serie,
      @Param("idSucursal") long idSucursal);

  boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);
}
