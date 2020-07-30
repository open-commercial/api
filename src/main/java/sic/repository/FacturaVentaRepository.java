package sic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

public interface FacturaVentaRepository
    extends FacturaRepository<FacturaVenta>,
        FacturaVentaRepositoryCustom,
        QuerydslPredicateExecutor<FacturaVenta> {

  @Override
  List<Factura> findAllByPedidoAndEliminada(Pedido pedido, boolean eliminada);

  @Modifying
  @Query(
          "UPDATE FacturaVenta fv SET fv.remito = :remito WHERE fv.idFactura = :idFactura")
  int modificarFacturaParaAgregarRemito(@Param("remito") Remito remito, @Param("idFactura") long idFactura);

  @Query("SELECT fv FROM FacturaVenta fv WHERE fv.remito = :remito")
  FacturaVenta buscarFacturaPorRemito(@Param("remito") Remito remito);

  @Query(
      "SELECT max(fv.numFactura) FROM FacturaVenta fv " +
        "WHERE fv.tipoComprobante = :tipoComprobante AND fv.numSerie = :numSerie AND fv.sucursal.idSucursal = :idSucursal")
  Long buscarMayorNumFacturaSegunTipo(
      @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
      @Param("numSerie") long numSerie,
      @Param("idSucursal") long idSucursal);
}
