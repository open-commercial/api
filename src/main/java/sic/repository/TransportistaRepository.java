package sic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.modelo.Transportista;

public interface TransportistaRepository extends
        JpaRepository<Transportista, Long>,
        QuerydslPredicateExecutor<Transportista> {

  Transportista findByNombreAndEliminado(String nombre, boolean eliminado);

  List<Transportista> findAllByAndEliminadoOrderByNombreAsc(boolean eliminado);
}
