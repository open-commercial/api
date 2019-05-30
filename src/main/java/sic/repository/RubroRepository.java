package sic.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Empresa;
import sic.modelo.Rubro;

public interface RubroRepository extends PagingAndSortingRepository<Rubro, Long> {

  Rubro findByNombreAndEmpresaAndEliminado(String nombre, Empresa empresa, boolean eliminado);

  List<Rubro> findAllByAndEmpresaAndEliminadoOrderByNombreAsc(Empresa empresa, boolean eliminado);
}
