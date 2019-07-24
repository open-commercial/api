package sic.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.FormaDePago;

public interface FormaDePagoRepository extends PagingAndSortingRepository<FormaDePago, Long> {

  FormaDePago findByAndPredeterminadoAndEliminada(boolean predeterminado, boolean eliminada);

  List<FormaDePago> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);

  List<FormaDePago> findAllByOrderByNombreAsc();

  FormaDePago findByPaymentMethodIdAndEliminada(String nombre, boolean eliminada);
}
