package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Sucursal;

public interface SucursalRepository extends PagingAndSortingRepository<Sucursal, Long> {

  @Query("SELECT e FROM Sucursal e WHERE e.idSucursal = :idSucursal AND e.eliminada = false")
  Sucursal findById(@Param("idSucursal") long idSucursal);

  Sucursal findByIdFiscalAndEliminada(Long idFiscal, boolean eliminada);

  Sucursal findByNombreIsAndEliminadaOrderByNombreAsc(String nombre, boolean eliminada);

  List<Sucursal> findAllByAndEliminadaOrderByNombreAsc(boolean eliminada);
}
