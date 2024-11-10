package org.opencommercial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.opencommercial.model.Medida;

public interface MedidaRepository extends JpaRepository<Medida, Long> {

  Medida findByNombreAndEliminada(String medida, boolean eliminada);

  List<Medida> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);
}
