package org.opencommercial.repository;

import org.opencommercial.model.FacturaCompra;
import org.opencommercial.repository.projection.EntidadMontoProjection;
import org.opencommercial.repository.projection.PeriodoMontoProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface FacturaCompraRepository extends
        FacturaRepository<FacturaCompra>,
        FacturaCompraRepositoryCustom,
        QuerydslPredicateExecutor<FacturaCompra> {

  @Query("SELECT year(fc.fecha) as periodo, round(sum(fc.total)) as monto " +
          "FROM FacturaCompra fc " +
          "WHERE fc.eliminada = false AND fc.sucursal.idSucursal = :idSucursal " +
          "GROUP BY periodo " +
          "ORDER BY periodo DESC")
  List<PeriodoMontoProjection> getMontoNetoCompradoPorAnio(long idSucursal, Pageable pageable);

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
