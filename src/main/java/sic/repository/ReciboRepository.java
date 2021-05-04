package sic.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Sucursal;
import sic.modelo.Recibo;

public interface ReciboRepository
    extends PagingAndSortingRepository<Recibo, Long>,
        QuerydslPredicateExecutor<Recibo>,
        ReciboRepositoryCustom {

  Optional<Recibo> findReciboByIdPagoMercadoPagoAndEliminado(
      String idPagoMercadoPago, boolean eliminado);

  Recibo findTopBySucursalAndNumSerieOrderByNumReciboDesc(Sucursal sucursal, long serie);

  @Query(
      "SELECT r FROM Recibo r "
          + "WHERE r.sucursal.idSucursal = :idSucursal "
          + "AND r.formaDePago.idFormaDePago = :idFormaDePago "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false "
          + "AND r.estado  = sic.modelo.EstadoRecibo.APROBADO")
  List<Recibo> getRecibosEntreFechasPorFormaDePago(
      @Param("idSucursal") long idSucursal,
      @Param("idFormaDePago") long idFormaDePago,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.sucursal.idSucursal = :idSucursal "
          + "AND (r.proveedor is null) "
          + "AND r.formaDePago.idFormaDePago = :idFormaDePago "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      @Param("idSucursal") long idSucursal,
      @Param("idFormaDePago") long idFormaDePago,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.sucursal.idSucursal = :idSucursal "
          + "AND (r.cliente is null) "
          + "AND r.formaDePago.idFormaDePago = :idFormaDePago "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      @Param("idSucursal") long idSucursal,
      @Param("idFormaDePago") long idFormaDePago,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.sucursal.idSucursal = :idSucursal "
          + "AND (r.proveedor is null) "
          + "AND r.formaDePago.afectaCaja = true "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      @Param("idSucursal") long idSucursal,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.sucursal.idSucursal = :idSucursal "
          + "AND (r.cliente is null) "
          + "AND r.formaDePago.afectaCaja = true "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      @Param("idSucursal") long idSucursal,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.sucursal.idSucursal = :idSucursal "
          + "AND (r.proveedor is null) "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosClientesEntreFechas(
      @Param("idSucursal") long idSucursal,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query(
      "SELECT SUM(r.monto) FROM Recibo r "
          + "WHERE r.sucursal.idSucursal = :idSucursal "
          + "AND (r.cliente is null) "
          + "AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
  BigDecimal getTotalRecibosProveedoresEntreFechas(
      @Param("idSucursal") long idSucursal,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Modifying
  @Query("UPDATE Recibo r SET r.urlImagen = :urlImagen WHERE r.idRecibo = :idRecibo")
  int actualizarUrlImagen(@Param("idRecibo") long idRecibo, @Param("urlImagen") String urlImagen);

}
