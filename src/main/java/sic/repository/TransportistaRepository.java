package sic.repository;

import java.util.List;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Transportista;

public interface TransportistaRepository extends
        PagingAndSortingRepository<Transportista, Long>,
        QuerydslPredicateExecutor<Transportista> {

  Transportista findByNombreAndEliminado(String nombre, boolean eliminado);

  List<Transportista> findAllByAndEliminadoOrderByNombreAsc(boolean eliminado);
}
