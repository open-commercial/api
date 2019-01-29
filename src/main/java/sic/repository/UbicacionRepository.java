package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Ubicacion;

public interface UbicacionRepository extends PagingAndSortingRepository<Ubicacion, Long>, QueryDslPredicateExecutor<Ubicacion> {

  @Query("SELECT u FROM Ubicacion u WHERE u.idUbicacion = :idUbicacion AND u.eliminada = false")
  Ubicacion findById(@Param("idUbicacion") long idUbicacion);

  @Modifying
  @Query("UPDATE Ubicacion u SET u.eliminada = false WHERE u.idUbicacion = :idUbicacion")
  int eliminar(@Param("idUbicacion") long idUbicacion);
}
