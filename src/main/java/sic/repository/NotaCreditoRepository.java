package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;
import java.util.List;

public interface NotaCreditoRepository
    extends NotaRepository<NotaCredito>, NotaCreditoRepositoryCustom {

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
