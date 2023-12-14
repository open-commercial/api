package sic.repository;

import java.util.List;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Proveedor;

public interface ProveedorRepository extends
        PagingAndSortingRepository<Proveedor, Long>,
        QuerydslPredicateExecutor<Proveedor> {

  Proveedor findByNroProveedorAndEliminado(String nroProveedor, boolean eliminado);

  List<Proveedor> findByIdFiscalAndEliminado(Long idFiscal, boolean eliminado);

  Proveedor findByRazonSocialAndEliminado(String razonSocial, boolean eliminado);

  List<Proveedor> findAllByAndEliminadoOrderByRazonSocialAsc(boolean eliminado);
}
