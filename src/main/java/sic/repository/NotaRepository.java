package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Empresa;
import sic.modelo.Nota;
import sic.modelo.TipoDeComprobante;

import java.util.List;

public interface NotaRepository<T extends Nota>
    extends PagingAndSortingRepository<T, Long>, QuerydslPredicateExecutor<T> {}
