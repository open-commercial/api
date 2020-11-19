package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

import java.util.List;

public interface FacturaVentaRepository
    extends FacturaRepository<FacturaVenta>,
        FacturaVentaRepositoryCustom,
        QuerydslPredicateExecutor<FacturaVenta> {

  @Modifying
  @Query(
          "UPDATE FacturaVenta fv SET fv.remito = :remito WHERE fv.idFactura = :idFactura")
  void modificarFacturaParaAgregarRemito(@Param("remito") Remito remito, @Param("idFactura") long idFactura);

  @Query("SELECT fv FROM FacturaVenta fv WHERE fv.remito = :remito")
  List<FacturaVenta> buscarFacturaPorRemito(@Param("remito") Remito remito);

  @Query(
      "SELECT max(fv.numFactura) FROM FacturaVenta fv "
          + "WHERE fv.tipoComprobante = :tipoComprobante AND fv.numSerie = :numSerie AND fv.sucursal.idSucursal = :idSucursal")
  Long buscarMayorNumFacturaSegunTipo(
      @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
      @Param("numSerie") long numSerie,
      @Param("idSucursal") long idSucursal);

  List<FacturaVenta> findByIdFacturaIn(long[] idFactura);
}
