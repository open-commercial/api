package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.entity.Localidad;
import sic.entity.Provincia;

public interface LocalidadRepository
    extends PagingAndSortingRepository<Localidad, Long>, QuerydslPredicateExecutor<Localidad> {

  @Query("SELECT l FROM Localidad l WHERE l.idLocalidad = :idLocalidad")
  Localidad findById(@Param("idLocalidad") long idLocalidad);

  Localidad findByNombreAndProvinciaOrderByNombreAsc(String nombre, Provincia provincia);

  Localidad findByCodigoPostal(String codigoPostal);

  List<Localidad> findAllByAndProvinciaOrderByNombreAsc(Provincia provincia);
}
