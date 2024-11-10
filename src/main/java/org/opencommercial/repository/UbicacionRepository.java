package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.opencommercial.model.Ubicacion;

public interface UbicacionRepository extends
        JpaRepository<Ubicacion, Long>,
        QuerydslPredicateExecutor<Ubicacion> {}
