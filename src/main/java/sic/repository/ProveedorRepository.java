package sic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.modelo.Proveedor;

public interface ProveedorRepository extends
        JpaRepository<Proveedor, Long>,
        QuerydslPredicateExecutor<Proveedor> {

  Proveedor findByNroProveedorAndEliminado(String nroProveedor, boolean eliminado);

  List<Proveedor> findByIdFiscalAndEliminado(Long idFiscal, boolean eliminado);

  Proveedor findByRazonSocialAndEliminado(String razonSocial, boolean eliminado);

  List<Proveedor> findAllByAndEliminadoOrderByRazonSocialAsc(boolean eliminado);
}
