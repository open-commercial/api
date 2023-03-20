package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.entity.Traspaso;

import java.util.List;

public interface TraspasoRepository
    extends PagingAndSortingRepository<Traspaso, Long>, QuerydslPredicateExecutor<Traspaso> {

    boolean existsByNroTraspaso(String nroTraspaso);

    List<Traspaso> findByNroPedido(Long nroPedido);
}
