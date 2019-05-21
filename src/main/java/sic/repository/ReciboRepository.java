package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Recibo;

public interface ReciboRepository
    extends PagingAndSortingRepository<Recibo, Long>,
        QuerydslPredicateExecutor<Recibo>,
        ReciboRepositoryCustom {

  Recibo findTopByEmpresaAndNumSerieOrderByNumReciboDesc(Empresa empresa, long serie);

  @Query(
      "SELECT r FROM Recibo r "
          + "WHERE r.empresa.id_Empresa = :idEmpresa "
          + "AND r.formaDePago.id_FormaDePago = :idFormaDePago "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  List<Recibo> getRecibosEntreFechasPorFormaDePago(
      @Param("idEmpresa") long idEmpresa,
      @Param("idFormaDePago") long idFormaDePago,
      @Param("desde") Date desde,
      @Param("hasta") Date hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.empresa.id_Empresa = :idEmpresa "
          + "AND (r.proveedor is null) "
          + "AND r.formaDePago.id_FormaDePago = :idFormaDePago "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      @Param("idEmpresa") long idEmpresa,
      @Param("idFormaDePago") long idFormaDePago,
      @Param("desde") Date desde,
      @Param("hasta") Date hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.empresa.id_Empresa = :idEmpresa "
          + "AND (r.cliente is null) "
          + "AND r.formaDePago.id_FormaDePago = :idFormaDePago "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      @Param("idEmpresa") long idEmpresa,
      @Param("idFormaDePago") long idFormaDePago,
      @Param("desde") Date desde,
      @Param("hasta") Date hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.empresa.id_Empresa = :idEmpresa "
          + "AND (r.proveedor is null) "
          + "AND r.formaDePago.afectaCaja = true "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      @Param("idEmpresa") long idEmpresa, @Param("desde") Date desde, @Param("hasta") Date hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.empresa.id_Empresa = :idEmpresa "
          + "AND (r.cliente is null) "
          + "AND r.formaDePago.afectaCaja = true "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      @Param("idEmpresa") long idEmpresa, @Param("desde") Date desde, @Param("hasta") Date hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.empresa.id_Empresa = :idEmpresa "
          + "AND (r.proveedor is null) "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosClientesEntreFechas(
      @Param("idEmpresa") long idEmpresa, @Param("desde") Date desde, @Param("hasta") Date hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.empresa.id_Empresa = :idEmpresa "
          + "AND (r.cliente is null) "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosProveedoresEntreFechas(
      @Param("idEmpresa") long idEmpresa, @Param("desde") Date desde, @Param("hasta") Date hasta);
}
