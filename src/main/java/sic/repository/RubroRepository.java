package sic.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.entity.Rubro;

public interface RubroRepository extends PagingAndSortingRepository<Rubro, Long> {

  Rubro findByNombreAndEliminado(String nombre, boolean eliminado);

  List<Rubro> findAllByAndEliminadoOrderByNombreAsc(boolean eliminado);
}
