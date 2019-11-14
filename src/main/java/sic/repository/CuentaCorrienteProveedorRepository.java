package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.Sucursal;
import sic.modelo.Proveedor;

public interface CuentaCorrienteProveedorRepository
    extends CuentaCorrienteRepository<CuentaCorrienteProveedor>,
        QuerydslPredicateExecutor<CuentaCorrienteProveedor> {

  CuentaCorrienteProveedor findByProveedorAndEliminada(Proveedor proveedor, boolean eliminada);

  @Modifying
  @Query(
      "UPDATE CuentaCorrienteProveedor ccp SET ccp.eliminada = true WHERE ccp.proveedor.idProveedor = :idProveedor")
  int eliminarCuentaCorrienteProveedor(@Param("idProveedor") long idProveedor);
}
