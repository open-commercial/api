package sic.repository;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import sic.modelo.Cliente;
import sic.modelo.CuentaCorrienteCliente;
import sic.modelo.Empresa;


public interface CuentaCorrienteClienteRepository extends CuentaCorrienteRepository<CuentaCorrienteCliente>, QueryDslPredicateExecutor<CuentaCorrienteCliente> {
    
    CuentaCorrienteCliente findByClienteAndEmpresaAndEliminada(Cliente cliente, Empresa empresa, boolean eliminada);
    
}
