package sic.repository;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Caja;
import sic.modelo.Empresa;

public interface CajaRepository extends PagingAndSortingRepository<Caja, Long>, QueryDslPredicateExecutor<Caja>, CajaRepositoryCustom {

    @Query("SELECT c FROM Caja c WHERE c.id_Caja = :idCaja AND c.eliminada = false")
    Caja findById(@Param("idCaja") long idCaja);

    Caja findTopByEmpresaAndEliminadaOrderByFechaAperturaDesc(Empresa empresa, boolean eliminada);

    @Query("SELECT c FROM Caja c " +
            "WHERE c.empresa.id_Empresa = :idEmpresa AND c.eliminada = false AND c.estado = sic.modelo.EstadoCaja.ABIERTA " +
            "ORDER BY c.id_Caja DESC")
    Caja isUltimaCajaAbierta(@Param("idEmpresa") long idEmpresa);

    @Modifying
    @Query("UPDATE Caja c SET c.saldoSistema = c.saldoSistema + :monto WHERE c.id_Caja = :idCaja AND c.estado = sic.modelo.EstadoCaja.ABIERTA")
    int actualizarSaldoSistema(@Param("idCaja") long idCaja, @Param("monto") BigDecimal monto);
      
}
