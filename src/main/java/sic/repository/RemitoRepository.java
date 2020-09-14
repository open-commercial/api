package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Remito;
import sic.modelo.TipoDeComprobante;

public interface RemitoRepository extends PagingAndSortingRepository<Remito, Long>,
        QuerydslPredicateExecutor<Remito> {

    @Query(
            "SELECT max(r.nroRemito) FROM Remito r "
                    + "WHERE r.tipoComprobante = :tipoComprobante "
                    + "AND r.serie = :serie")
    Long buscarMayorNumRemitoSegunTipo(
            @Param("tipoComprobante") TipoDeComprobante tipoComprobante,
            @Param("serie") long serie);
}
