package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Caja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CajaRepository extends
        PagingAndSortingRepository<Caja, Long>,
        QuerydslPredicateExecutor<Caja>,
        CajaRepositoryCustom {

  @Query("SELECT c FROM Caja c WHERE c.idCaja = :idCaja AND c.eliminada = false")
  Caja findById(@Param("idCaja") long idCaja);

  @Query("SELECT c FROM Caja c WHERE c.sucursal.idSucursal = :idSucursal AND c.eliminada = false ORDER BY c.idCaja DESC")
  Page<Caja> findTopBySucursalAndEliminadaOrderByIdCajaDesc(@Param("idSucursal") long idSucursal, Pageable page);

  @Query("SELECT c FROM Caja c "
          + "WHERE c.sucursal.idSucursal = :idSucursal AND c.eliminada = false "
          + "AND c.estado = sic.modelo.EstadoCaja.ABIERTA "
          + "ORDER BY c.idCaja DESC")
  Caja isUltimaCajaAbierta(@Param("idSucursal") long idSucursal);

  @Query("SELECT c FROM Caja c "
          + "WHERE c.sucursal.idSucursal = :idSucursal AND c.eliminada = false "
          + "AND c.estado = sic.modelo.EstadoCaja.CERRADA "
          + "AND :fecha BETWEEN c.fechaApertura AND c.fechaCierre")
  Caja encontrarCajaCerradaQueContengaFechaEntreFechaAperturaYFechaCierre(
          @Param("idSucursal") long idSucursal, @Param("fecha") LocalDateTime fecha);

  @Modifying
  @Query("UPDATE Caja c "
          + "SET c.saldoSistema = c.saldoSistema + :monto WHERE c.idCaja = :idCaja "
          + "AND c.estado = sic.modelo.EstadoCaja.CERRADA")
  int actualizarSaldoSistema(@Param("idCaja") long idCaja, @Param("monto") BigDecimal monto);
}
