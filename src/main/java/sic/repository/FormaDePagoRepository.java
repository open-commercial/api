package sic.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;

public interface FormaDePagoRepository extends PagingAndSortingRepository<FormaDePago, Long> {

      FormaDePago findByNombreAndEmpresaAndEliminada(String nombre, Empresa empresa, boolean eliminada);

      FormaDePago findByAndEmpresaAndPredeterminadoAndEliminada(Empresa empresa, boolean predeterminado, boolean eliminada);

      List<FormaDePago> findAllByAndEmpresaAndEliminadaOrderByNombreAsc(Empresa empresa, boolean eliminada);
  
}
