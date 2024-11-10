package org.opencommercial.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.Cliente;
import org.opencommercial.model.CuentaCorrienteCliente;

import java.math.BigDecimal;

public interface CuentaCorrienteClienteRepository extends
        CuentaCorrienteRepository<CuentaCorrienteCliente>,
        QuerydslPredicateExecutor<CuentaCorrienteCliente> {

  CuentaCorrienteCliente findByClienteAndEliminada(Cliente cliente, boolean eliminada);

  @Modifying
  @Query("UPDATE CuentaCorrienteCliente ccc SET ccc.eliminada = true WHERE ccc.cliente.idCliente = :idCliente")
  int eliminarCuentaCorrienteCliente(@Param("idCliente") long idCliente);

  @Query("SELECT ccc.saldo FROM CuentaCorrienteCliente ccc WHERE ccc.cliente.idCliente = :idCliente")
  BigDecimal getSaldoCuentaCorrienteCliente(@Param("idCliente") long idCliente);
}
