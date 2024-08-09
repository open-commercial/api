package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.modelo.FacturaCompra;
import sic.modelo.dto.EntidadMontoDTO;
import sic.repository.projection.EntidadMontoProjection;
import sic.repository.projection.PeriodoMontoProjection;
import java.util.List;

public interface FacturaCompraRepository extends
        FacturaRepository<FacturaCompra>,
        FacturaCompraRepositoryCustom,
        QuerydslPredicateExecutor<FacturaCompra> {

  @Query("SELECT year(fc.fecha) as periodo, round(sum(fc.total)) as monto " +
          "FROM FacturaCompra fc " +
          "WHERE fc.eliminada = false AND fc.sucursal.idSucursal = :idSucursal " +
          "GROUP BY periodo " +
          "ORDER BY periodo desc")
  List<PeriodoMontoProjection> getMontoNetoCompradoPorAnio(long idSucursal);

  @Query("SELECT month(fc.fecha) as periodo, round(sum(fc.total)) as monto " +
          "FROM FacturaCompra fc " +
          "WHERE fc.eliminada = false AND fc.sucursal.idSucursal = :idSucursal AND year(fc.fecha) = :anio " +
          "GROUP BY periodo " +
          "ORDER BY periodo")
  List<PeriodoMontoProjection> getMontoNetoCompradoPorMes(long idSucursal, int anio);

  @Query("SELECT p.razonSocial as entidad, round(sum(fc.total)) as monto " +
          "FROM FacturaCompra fc INNER JOIN fc.proveedor p " +
          "WHERE fc.eliminada = false AND p.eliminado = false " +
          "AND year(fc.fecha) = :anio AND fc.sucursal.idSucursal = :idSucursal " +
          "GROUP BY p.idProveedor " +
          "ORDER BY monto desc")
  List<EntidadMontoProjection> getMontoNetoCompradoPorProveedorPorAnio(long idSucursal, int anio);

  @Query("SELECT p.razonSocial as entidad, round(sum(fc.total)) as monto " +
          "FROM FacturaCompra fc INNER JOIN fc.proveedor p " +
          "WHERE fc.eliminada = false AND p.eliminado = false " +
          "AND year(fc.fecha) = :anio AND month(fc.fecha) = :mes AND fc.sucursal.idSucursal = :idSucursal " +
          "GROUP BY p.idProveedor " +
          "ORDER BY monto desc")
  List<EntidadMontoProjection> getMontoNetoCompradoPorProveedorPorMes(long idSucursal, int anio, int mes);

}
