package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.CuentaCorrienteCliente;
import sic.modelo.Sucursal;

public interface CuentaCorrienteClienteRepository
    extends CuentaCorrienteRepository<CuentaCorrienteCliente>,
        QuerydslPredicateExecutor<CuentaCorrienteCliente> {

  CuentaCorrienteCliente findByClienteAndSucursalAndEliminada(
    Cliente cliente, Sucursal sucursal, boolean eliminada);

  @Modifying
  @Query(
      "UPDATE CuentaCorrienteCliente ccc SET ccc.eliminada = true WHERE ccc.cliente.id_Cliente = :idCliente")
  int eliminarCuentaCorrienteCliente(@Param("idCliente") long idCliente);
}
