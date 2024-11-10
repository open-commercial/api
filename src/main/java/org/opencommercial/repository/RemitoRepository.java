package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.Remito;

public interface RemitoRepository extends
        JpaRepository<Remito, Long>,
        QuerydslPredicateExecutor<Remito> {

  @Query("SELECT max(r.nroRemito) FROM Remito r WHERE r.serie = :serie")
  Long buscarMayorNumRemitoSegunSerie(@Param("serie") long serie);
}
