package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Medida;

public interface MedidaRepository extends PagingAndSortingRepository<Medida, Long> {
    
      @Query("SELECT m FROM Medida m WHERE m.id_Medida = :idMedida AND m.eliminada = false")
      Medida findById(@Param("idMedida") long idMedida);
    
      Medida findByNombreAndEmpresaAndEliminada(String medida, Empresa empresa, boolean eliminada);
      
      List<Medida> findAllByAndEmpresaAndEliminadaOrderByNombreAsc(Empresa empresa, boolean eliminada);
    
}
