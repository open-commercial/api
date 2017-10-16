package sic.repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.FormaDePago;
import sic.modelo.Gasto;

public interface GastoRepository extends PagingAndSortingRepository<Gasto, Long> {

      @Query("SELECT g FROM Gasto g WHERE g.id_Gasto = :idGasto AND g.eliminado = false")
      Gasto findById(@Param("idGasto") long idGasto);
      
      Gasto findByNroGastoAndEmpresaAndEliminado(Long nroPago, Empresa empresa, boolean eliminado);

      List<Gasto> findAllByFechaBetweenAndEmpresaAndEliminado(Date desde, Date hasta, Empresa empresa, boolean eliminado);

      List<Gasto> findAllByFechaBetweenAndEmpresaAndFormaDePagoAndEliminado(Date desde, Date hasta, Empresa empresa, FormaDePago formaDePago, boolean eliminado);

      Gasto findTopByEmpresaAndEliminadoOrderByNroGastoDesc(Empresa empresa, boolean eliminado);

}
