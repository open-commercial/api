package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Pais;
import sic.modelo.Provincia;

public interface ProvinciaRepository extends PagingAndSortingRepository<Provincia, Long> {
    
      @Query("SELECT p FROM Provincia p WHERE p.id_Provincia = :idProvincia AND p.eliminada = false")
      Provincia findById(@Param("idProvincia") long idProvincia);
    
      Provincia findByNombreAndPaisAndEliminadaOrderByNombreAsc(String nombre, Pais pais, boolean eliminada);
      
      List<Provincia> findAllByAndPaisAndEliminadaOrderByNombreAsc(Pais pais, boolean eliminada);

}
