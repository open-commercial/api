package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

public interface NotaDebitoRepository
    extends NotaRepository<NotaDebito>, NotaDebitoRepositoryCustom {

  @Query(
      "SELECT max(nd.nroNota) FROM NotaDebito nd "
          + "WHERE nd.tipoComprobante = :tipoComprobante AND nd.serie = :serie AND nd.sucursal.idSucursal = :idSucursal "
          + "AND nd.cliente IS NOT null")
  Long buscarMayorNumNotaDebitoClienteSegunTipo(
      @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
      @Param("serie") long serie,
      @Param("idSucursal") long idSucursal);

  boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);
}
