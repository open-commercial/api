package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.opencommercial.model.Traspaso;

import java.util.List;

public interface TraspasoRepository extends
        JpaRepository<Traspaso, Long>,
        QuerydslPredicateExecutor<Traspaso> {

    boolean existsByNroTraspaso(String nroTraspaso);

    List<Traspaso> findByNroPedido(Long nroPedido);
}
