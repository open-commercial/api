package sic.repository;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Empresa;
import sic.modelo.Medida;

public interface MedidaRepository extends PagingAndSortingRepository<Medida, Long> {

  Medida findByNombreAndEmpresaAndEliminada(String medida, Empresa empresa, boolean eliminada);

  List<Medida> findAllByAndEmpresaAndEliminadaOrderByNombreAsc(Empresa empresa, boolean eliminada);
}
