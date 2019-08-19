package sic.repository;

import java.util.List;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Sucursal;
import sic.modelo.Transportista;

public interface TransportistaRepository
    extends PagingAndSortingRepository<Transportista, Long>,
        QuerydslPredicateExecutor<Transportista> {

  Transportista findByNombreAndSucursalAndEliminado(
    String nombre, Sucursal sucursal, boolean eliminado);

  List<Transportista> findAllByAndSucursalAndEliminadoOrderByNombreAsc(
    Sucursal sucursal, boolean eliminado);
}
