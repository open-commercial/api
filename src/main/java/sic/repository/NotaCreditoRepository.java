package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.entity.FacturaCompra;
import sic.entity.FacturaVenta;
import sic.entity.NotaCredito;
import sic.domain.TipoDeComprobante;
import java.util.List;

public interface NotaCreditoRepository
    extends NotaRepository<NotaCredito>, NotaCreditoRepositoryCustom, QuerydslPredicateExecutor<NotaCredito> {

  List<NotaCredito> findAllByFacturaVentaAndEliminada(FacturaVenta factura, boolean eliminada);

  @Query(
      "SELECT max(nc.nroNota) FROM NotaCredito nc "
          + "WHERE nc.tipoComprobante = :tipoComprobante "
          + "AND nc.serie = :serie "
          + "AND nc.sucursal.idSucursal = :idSucursal "
          + "AND nc.cliente IS NOT null")
  Long buscarMayorNumNotaCreditoClienteSegunTipo(
      @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
      @Param("serie") long serie,
      @Param("idSucursal") long idSucursal);

  boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta, boolean eliminada);

  List<NotaCredito> findAllByFacturaCompraAndEliminada(FacturaCompra factura, boolean eliminada);
}
