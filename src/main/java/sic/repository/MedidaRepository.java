package sic.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.entity.Medida;

public interface MedidaRepository extends PagingAndSortingRepository<Medida, Long> {

  Medida findByNombreAndEliminada(String medida, boolean eliminada);

  List<Medida> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);
}
