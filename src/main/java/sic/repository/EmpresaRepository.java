package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;

public interface EmpresaRepository extends PagingAndSortingRepository<Empresa, Long>{
    
      @Query("SELECT e FROM Empresa e WHERE e.id_Empresa = :idEmpresa AND e.eliminada = false")
      Empresa findById(@Param("idEmpresa") long idEmpresa);
    
      Empresa findByCuipAndEliminada(long cuip, boolean eliminada);

      Empresa findByNombreIsAndEliminadaOrderByNombreAsc(String nombre, boolean eliminada);

      List<Empresa> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada); 
    
}
