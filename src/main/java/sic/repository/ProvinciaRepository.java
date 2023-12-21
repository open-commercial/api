package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Provincia;

public interface ProvinciaRepository extends PagingAndSortingRepository<Provincia, Long> {
    
      @Query("SELECT p FROM Provincia p WHERE p.idProvincia = :idProvincia")
      Provincia findById(@Param("idProvincia") long idProvincia);
    
      Provincia findByNombreOrderByNombreAsc(String nombre);
      
      List<Provincia> findAllByOrderByNombreAsc();
}
