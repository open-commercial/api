package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Sucursal;

public interface SucursalRepository extends PagingAndSortingRepository<Sucursal, Long> {

  Sucursal findByIdFiscalAndEliminada(Long idFiscal, boolean eliminada);

  Sucursal findByNombreIsAndEliminadaOrderByNombreAsc(String nombre, boolean eliminada);

  List<Sucursal> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);

  @Query(
      "SELECT s FROM Sucursal s WHERE s.configuracionSucursal.predeterminada = true and s.eliminada = false")
  Sucursal getSucursalPredeterminada();
}
