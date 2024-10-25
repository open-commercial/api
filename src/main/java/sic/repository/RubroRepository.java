package sic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import sic.modelo.Rubro;

public interface RubroRepository extends JpaRepository<Rubro, Long> {

  Rubro findByNombreAndEliminado(String nombre, boolean eliminado);

  List<Rubro> findAllByAndEliminadoOrderByNombreAsc(boolean eliminado);
}
