package org.opencommercial.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.opencommercial.model.FormaDePago;

public interface FormaDePagoRepository extends JpaRepository<FormaDePago, Long> {

  Optional<FormaDePago> findByAndPredeterminadoAndEliminada(boolean predeterminado, boolean eliminada);

  List<FormaDePago> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);

  List<FormaDePago> findAllByOrderByNombreAsc();

  Optional<FormaDePago> findByNombreAndEliminada(String nombre, boolean eliminada);
}
