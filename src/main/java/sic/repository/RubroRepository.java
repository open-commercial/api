package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Rubro;

public interface RubroRepository extends PagingAndSortingRepository<Rubro, Long> {
    
      @Query("SELECT r FROM Rubro r WHERE r.id_Rubro = :idRubro AND r.eliminado = false")
      Rubro findById(@Param("idRubro") long idRubro);

      Rubro findByNombreAndEmpresaAndEliminado(String nombre, Empresa empresa, boolean eliminado); 

      List<Rubro> findAllByAndEmpresaAndEliminadoOrderByNombreAsc(Empresa empresa, boolean eliminado);
    
}
