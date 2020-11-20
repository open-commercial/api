package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Traspaso;

import java.util.List;

public interface TraspasoRepository
    extends PagingAndSortingRepository<Traspaso, Long>, QuerydslPredicateExecutor<Traspaso> {

    Traspaso findByNroTraspaso(String nroTraspaso);

    List<Traspaso> findByNroPedido(Long nroPedido);
}
