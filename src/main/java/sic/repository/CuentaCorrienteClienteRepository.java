package sic.repository;

import sic.modelo.Cliente;
import sic.modelo.CuentaCorrienteCliente;
import sic.modelo.Empresa;


public interface CuentaCorrienteClienteRepository extends CuentaCorrienteRepository<CuentaCorrienteCliente> {
    
    CuentaCorrienteCliente findByClienteAndEmpresaAndEliminada(Cliente cliente, Empresa empresa, boolean eliminada);
    
}
