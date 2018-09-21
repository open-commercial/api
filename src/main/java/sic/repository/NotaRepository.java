package sic.repository;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Nota;

public interface NotaRepository<T extends Nota>
    extends PagingAndSortingRepository<T, Long>, QueryDslPredicateExecutor<T> {

  @Query("SELECT n FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
  Nota findById(@Param("idNota") long idNota);

  @Query("SELECT n.total FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
  BigDecimal getTotalById(@Param("idNota") long idNota);

  @Query("SELECT n.CAE FROM Nota n WHERE n.idNota= :idNota AND n.eliminada = false")
  Long getCAEById(@Param("idNota") long idNota);
}
