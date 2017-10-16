package sic.repository;

import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.CuentaCorriente;
import sic.modelo.RenglonCuentaCorriente;

public interface CuentaCorrienteRepository extends PagingAndSortingRepository<CuentaCorriente, Long> {
    
      @Query("SELECT c FROM CuentaCorriente c WHERE c.idCuentaCorriente = :idCuentaCorriente AND c.eliminada = false")
      CuentaCorriente findById(@Param("idCuentaCorriente") long idCuentaCorriente);
      
      CuentaCorriente findByClienteAndEliminada(Cliente cliente, boolean eliminada);
      
      @Query("SELECT r FROM CuentaCorriente cc INNER JOIN cc.renglones r WHERE cc.cliente.id_Cliente = :idCliente AND cc.eliminada = false AND r.eliminado = false AND r.fecha BETWEEN :desde AND :hasta ORDER BY r.fecha ASC")
      Page<RenglonCuentaCorriente> getRenglonesCuentaCorrientePorClienteEntreFechas(@Param("idCliente") long idCliente, @Param("desde") Date desde, @Param("hasta") Date hasta, Pageable page);
    
      @Query("SELECT SUM(r.monto) FROM CuentaCorriente cc INNER JOIN cc.renglones r WHERE cc.cliente.id_Cliente = :idCliente AND cc.eliminada = false AND r.eliminado = false AND r.fecha <= :hasta")
      Double getSaldoCuentaCorriente(@Param("idCliente") long idCliente, @Param("hasta") Date hasta);
      
}
