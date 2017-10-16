package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Pais;

public interface PaisRepository extends PagingAndSortingRepository<Pais, Long> {
    
      @Query("SELECT p FROM Pais p WHERE p.id_Pais = :idPais AND p.eliminado = false")
      Pais findById(@Param("idPais") long idPais);
    
      Pais findByNombreIsAndEliminadoOrderByNombreAsc(String nombre, boolean eliminado);

      List<Pais> findAllByAndEliminadoOrderByNombreAsc(boolean eliminado);
    
}
