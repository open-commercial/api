package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Localidad;
import sic.modelo.Provincia;

public interface LocalidadRepository extends PagingAndSortingRepository<Localidad, Long>, QueryDslPredicateExecutor<Localidad> {
    
      @Query("SELECT l FROM Localidad l WHERE l.idLocalidad = :idLocalidad")
      Localidad findById(@Param("idLocalidad") long idLocalidad);
      
      Localidad findByNombreAndProvinciaOrderByNombreAsc(String nombre, Provincia provincia);
      
      List<Localidad> findAllByAndProvinciaOrderByNombreAsc(Provincia provincia);
    
}
