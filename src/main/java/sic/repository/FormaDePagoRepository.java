package sic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.FormaDePago;

public interface FormaDePagoRepository extends PagingAndSortingRepository<FormaDePago, Long> {

  Optional<FormaDePago> findByAndPredeterminadoAndEliminada(boolean predeterminado, boolean eliminada);

  List<FormaDePago> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);

  List<FormaDePago> findAllByOrderByNombreAsc();

  Optional<FormaDePago> findByNombreAndEliminada(String nombre, boolean eliminada);
}
