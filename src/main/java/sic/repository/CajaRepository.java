package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Caja;
import sic.modelo.Empresa;

public interface CajaRepository extends PagingAndSortingRepository<Caja, Long>, QueryDslPredicateExecutor<Caja> {
   
      @Query("SELECT c FROM Caja c WHERE c.id_Caja = :idCaja AND c.eliminada = false")
      Caja findById(@Param("idCaja") long idCaja);
      
      Caja findTopByEmpresaAndEliminadaOrderByFechaAperturaDesc(Empresa empresa, boolean eliminada);

      @Query("SELECT (c.estado = sic.modelo.EstadoCaja.ABIERTA) FROM Caja c WHERE c.empresa.id_Empresa = :idEmpresa AND c.eliminada = false ORDER BY c.id_Caja DESC")
      boolean ultimaCajaAbierta(@Param("idEmpresa") long idEmpresa);
      
      @Query("SELECT SUM(c.saldoSistema) FROM Caja c WHERE c.empresa.id_Empresa = :idEmpresa AND c.usuarioCierraCaja.id_Usuario = :idUsuario AND c.fechaApertura BETWEEN :desde AND :hasta AND c.eliminada = false")
      BigDecimal getSaldoSistemaCajasPorUsuarioDeCierre(@Param("idEmpresa") long idEmpresa, @Param("idUsuario") long idUsuario, @Param("desde") Date desde, @Param("hasta") Date hasta);
      
      @Query("SELECT SUM(c.saldoReal) FROM Caja c WHERE c.empresa.id_Empresa = :idEmpresa AND c.usuarioCierraCaja.id_Usuario = :idUsuario AND c.fechaApertura BETWEEN :desde AND :hasta AND c.eliminada = false")
      BigDecimal getSaldoRealCajasPorUsuarioDeCierre(@Param("idEmpresa") long idEmpresa, @Param("idUsuario") long idUsuario, @Param("desde") Date desde, @Param("hasta") Date hasta);

      @Query("SELECT SUM(c.saldoSistema) FROM Caja c WHERE c.empresa.id_Empresa = :idEmpresa AND c.fechaApertura BETWEEN :desde AND :hasta AND c.eliminada = false")
      BigDecimal getSaldoSistemaCajas(@Param("idEmpresa") long idEmpresa, @Param("desde") Date desde, @Param("hasta") Date hasta);

      @Query("SELECT SUM(c.saldoReal) FROM Caja c WHERE c.empresa.id_Empresa = :idEmpresa AND c.fechaApertura BETWEEN :desde AND :hasta AND c.eliminada = false")
      BigDecimal getSaldoRealCajas(@Param("idEmpresa") long idEmpresa, @Param("desde") Date desde, @Param("hasta") Date hasta);

      @Modifying
      @Query("UPDATE Caja c SET c.saldoSistema = c.saldoSistema + :monto WHERE c.id_Caja = :idCaja AND c.estado = sic.modelo.EstadoCaja.ABIERTA")
      int actualizarSaldoSistema(@Param("idCaja") long idCaja, @Param("monto") BigDecimal monto);
      
}
