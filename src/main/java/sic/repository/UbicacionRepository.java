package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.entity.Ubicacion;

public interface UbicacionRepository
    extends PagingAndSortingRepository<Ubicacion, Long>, QuerydslPredicateExecutor<Ubicacion> {}
