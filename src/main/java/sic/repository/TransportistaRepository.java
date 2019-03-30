package sic.repository;

import java.util.List;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Empresa;
import sic.modelo.Transportista;

public interface TransportistaRepository
    extends PagingAndSortingRepository<Transportista, Long>,
        QuerydslPredicateExecutor<Transportista> {

  Transportista findByNombreAndEmpresaAndEliminado(
      String nombre, Empresa empresa, boolean eliminado);

  List<Transportista> findAllByAndEmpresaAndEliminadoOrderByNombreAsc(
      Empresa empresa, boolean eliminado);
}
