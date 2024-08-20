package sic.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.modelo.FacturaVenta;
import sic.modelo.Remito;
import sic.modelo.TipoDeComprobante;
import sic.repository.projection.EntidadMontoProjection;
import sic.repository.projection.PeriodoMontoProjection;
import java.util.List;

public interface FacturaVentaRepository extends
        FacturaRepository<FacturaVenta>,
        FacturaVentaRepositoryCustom,
        QuerydslPredicateExecutor<FacturaVenta> {

  @Modifying
  @Query("UPDATE FacturaVenta fv SET fv.remito = :remito WHERE fv.idFactura = :idFactura")
  void modificarFacturaParaAgregarRemito(Remito remito, long idFactura);

  @Query("SELECT fv FROM FacturaVenta fv WHERE fv.remito = :remito")
  List<FacturaVenta> buscarFacturaPorRemito(Remito remito);

  @Query("SELECT max(fv.numFactura) " +
          "FROM FacturaVenta fv " +
          "WHERE fv.tipoComprobante = :tipoComprobante " +
          "AND fv.numSerie = :numSerie " +
          "AND fv.sucursal.idSucursal = :idSucursal")
  Long buscarMayorNumFacturaSegunTipo(TipoDeComprobante tipoComprobante,
                                      long numSerie,
                                      long idSucursal);

  List<FacturaVenta> findByIdFacturaIn(long[] idFactura);

  @Query("SELECT year(fv.fecha) as periodo, round(sum(fv.total)) as monto " +
          "FROM FacturaVenta fv " +
          "WHERE fv.eliminada = false AND fv.sucursal.idSucursal = :idSucursal " +
          "GROUP BY periodo " +
          "ORDER BY periodo desc")
  List<PeriodoMontoProjection> getMontoNetoVendidoPorAnio(long idSucursal, Pageable pageable);

  @Query("SELECT month(fv.fecha) as periodo, round(sum(fv.total)) as monto " +
          "FROM FacturaVenta fv " +
          "WHERE fv.eliminada = false AND fv.sucursal.idSucursal = :idSucursal AND year(fv.fecha) = :anio " +
          "GROUP BY periodo " +
          "ORDER BY periodo")
  List<PeriodoMontoProjection> getMontoNetoVendidoPorMes(long idSucursal, int anio);

  @Query("SELECT c.nombreFiscal as entidad, round(sum(fv.total)) as monto " +
          "FROM FacturaVenta fv INNER JOIN fv.cliente c " +
          "WHERE fv.eliminada = false AND c.eliminado = false " +
          "AND year(fv.fecha) = :anio AND fv.sucursal.idSucursal = :idSucursal " +
          "GROUP BY c.idCliente " +
          "ORDER BY monto desc")
  List<EntidadMontoProjection> getMontoNetoVendidoPorRubroPorAnio(long idSucursal, int anio);

  @Query("SELECT c.nombreFiscal as entidad, round(sum(fv.total)) as monto " +
          "FROM FacturaVenta fv INNER JOIN fv.cliente c " +
          "WHERE fv.eliminada = false AND c.eliminado = false " +
          "AND year(fv.fecha) = :anio AND month(fv.fecha) = :mes AND fv.sucursal.idSucursal = :idSucursal " +
          "GROUP BY c.idCliente " +
          "ORDER BY monto desc")
  List<EntidadMontoProjection> getMontoNetoVendidoPorRubroPorMes(long idSucursal, int anio, int mes);
}
