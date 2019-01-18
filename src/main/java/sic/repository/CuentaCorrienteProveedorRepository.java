package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.Empresa;
import sic.modelo.Proveedor;

public interface CuentaCorrienteProveedorRepository
    extends CuentaCorrienteRepository<CuentaCorrienteProveedor>,
        QueryDslPredicateExecutor<CuentaCorrienteProveedor> {

  CuentaCorrienteProveedor findByProveedorAndEmpresaAndEliminada(
      Proveedor proveedor, Empresa empresa, boolean eliminada);

  @Modifying
  @Query(
      "UPDATE CuentaCorrienteProveedor ccp SET ccp.eliminada = true WHERE ccp.proveedor.id_Proveedor = :idProveedor")
  int eliminarCuentaCorrienteProveedor(@Param("idProveedor") long idProveedor);
}
