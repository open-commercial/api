package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Gasto;

public interface GastoRepository extends PagingAndSortingRepository<Gasto, Long>, QuerydslPredicateExecutor<Gasto>, GastoRepositoryCustom {

    @Query("SELECT g FROM Gasto g " +
            "WHERE g.empresa.id_Empresa = :idEmpresa " +
            "AND g.formaDePago.id_FormaDePago = :idFormaDePago " +
            "AND g.fecha BETWEEN :desde AND :hasta AND g.eliminado = false")
    List<Gasto> getGastosEntreFechasPorFormaDePago(@Param("idEmpresa") long idEmpresa,
                                                   @Param("idFormaDePago") long idFormaDePago,
                                                   @Param("desde") Date desde, @Param("hasta") Date hasta);

    Gasto findTopByEmpresaAndEliminadoOrderByNroGastoDesc(Empresa empresa, boolean eliminado);

    @Query("SELECT SUM(g.monto) FROM Gasto g " +
            "WHERE g.empresa.id_Empresa = :idEmpresa " +
            "AND g.formaDePago.id_FormaDePago = :idFormaDePago " +
            "AND g.fecha BETWEEN :desde AND :hasta AND g.eliminado = false")
    BigDecimal getTotalGastosEntreFechasPorFormaDePago(@Param("idEmpresa") long idEmpresa, @Param("idFormaDePago") long idFormaDePago,
                                                       @Param("desde") Date desde, @Param("hasta") Date hasta);

    @Query("SELECT SUM(g.monto) FROM Gasto g " +
            "WHERE g.empresa.id_Empresa = :idEmpresa " +
            "AND g.formaDePago.afectaCaja = true " +
            "AND g.fecha BETWEEN :desde AND :hasta AND g.eliminado = false")
    BigDecimal getTotalGastosQueAfectanCajaEntreFechas(@Param("idEmpresa") long idEmpresa,
                                                       @Param("desde") Date desde, @Param("hasta") Date hasta);

    @Query("SELECT SUM(g.monto) FROM Gasto g " +
            "WHERE g.empresa.id_Empresa = :idEmpresa " +
            "AND g.fecha BETWEEN :desde AND :hasta AND g.eliminado = false")
    BigDecimal getTotalGastosEntreFechas(@Param("idEmpresa") long idEmpresa,
                                         @Param("desde") Date desde, @Param("hasta") Date hasta);

}
