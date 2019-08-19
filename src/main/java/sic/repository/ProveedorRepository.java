package sic.repository;

import java.util.List;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Sucursal;
import sic.modelo.Proveedor;

public interface ProveedorRepository
    extends PagingAndSortingRepository<Proveedor, Long>, QuerydslPredicateExecutor<Proveedor> {

  Proveedor findByNroProveedorAndSucursalAndEliminado(String nroProveedor, Sucursal sucursal, boolean eliminado);

  Proveedor findByIdFiscalAndSucursalAndEliminado(Long idFiscal, Sucursal sucursal, boolean eliminado);

  Proveedor findByRazonSocialAndSucursalAndEliminado(
    String razonSocial, Sucursal sucursal, boolean eliminado);

  List<Proveedor> findAllByAndSucursalAndEliminadoOrderByRazonSocialAsc(
    Sucursal sucursal, boolean eliminado);
}
