package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Ubicacion;

public interface UbicacionRepository extends PagingAndSortingRepository<Ubicacion, Long>, QueryDslPredicateExecutor<Ubicacion> {

  @Query("SELECT u FROM Ubicacion u WHERE u.idUbicacion = :idUbicacion")
  Ubicacion findById(@Param("idUbicacion") long idUbicacion);
}
