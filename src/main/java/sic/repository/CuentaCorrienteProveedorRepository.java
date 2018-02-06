package sic.repository;

import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.Empresa;
import sic.modelo.Proveedor;

public interface CuentaCorrienteProveedorRepository extends CuentaCorrienteRepository<CuentaCorrienteProveedor>  {
    
    CuentaCorrienteProveedor findByProveedorAndEmpresaAndEliminada(Proveedor proveedor, Empresa empresa, boolean eliminada);
    
}
