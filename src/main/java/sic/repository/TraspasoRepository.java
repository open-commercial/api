package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Traspaso;

public interface TraspasoRepository
    extends PagingAndSortingRepository<Traspaso, Long>, QuerydslPredicateExecutor<Traspaso> {

    Traspaso findByNroTraspasoAndAndEliminado(String nroTraspaso, boolean eliminado);
}
