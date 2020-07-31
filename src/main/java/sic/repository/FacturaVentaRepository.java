package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

public interface FacturaVentaRepository
    extends FacturaRepository<FacturaVenta>,
        FacturaVentaRepositoryCustom,
        QuerydslPredicateExecutor<FacturaVenta> {

  @Query(
      "SELECT max(fv.numFactura) FROM FacturaVenta fv "
          + "WHERE fv.tipoComprobante = :tipoComprobante AND fv.numSerie = :numSerie AND fv.sucursal.idSucursal = :idSucursal")
  Long buscarMayorNumFacturaSegunTipo(
      @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
      @Param("numSerie") long numSerie,
      @Param("idSucursal") long idSucursal);
}
