package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Nota;

public interface NotaRepository<T extends Nota>
    extends PagingAndSortingRepository<T, Long>, QueryDslPredicateExecutor<T> {

  @Query("SELECT n FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
  Nota findById(@Param("idNota") long idNota);
}
