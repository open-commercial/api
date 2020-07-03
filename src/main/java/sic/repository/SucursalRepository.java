package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Sucursal;

public interface SucursalRepository extends PagingAndSortingRepository<Sucursal, Long> {

  Sucursal findByIdFiscalAndEliminada(Long idFiscal, boolean eliminada);

  Sucursal findByNombreIsAndEliminadaOrderByNombreAsc(String nombre, boolean eliminada);

  List<Sucursal> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);

  @Query(
      "SELECT s FROM Sucursal s INNER JOIN ConfiguracionSucursal cs on s.idSucursal = cs.sucursal.idSucursal"
          + " WHERE cs.predeterminada = true and s.eliminada = false")
  Sucursal getSucursalPredeterminada();
}
