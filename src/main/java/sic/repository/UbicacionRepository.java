package sic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.modelo.Ubicacion;

public interface UbicacionRepository extends
        JpaRepository<Ubicacion, Long>,
        QuerydslPredicateExecutor<Ubicacion> {}
