package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Producto;

import java.util.List;

public interface ProductoRepository extends PagingAndSortingRepository<Producto, Long>,
        QueryDslPredicateExecutor<Producto>, ProductoRepositoryCustom {

    @Query("SELECT p FROM Producto p WHERE p.idProducto = :idProducto AND p.eliminado = false")
    Producto findById(@Param("idProducto") long idProducto);
    
    Producto findByCodigoAndEmpresaAndEliminado(String codigo, Empresa empresa, boolean eliminado);

    Producto findByDescripcionAndEmpresaAndEliminado(String descripcion, Empresa empresa, boolean eliminado);

    List<Producto> findByIdProductoIn(List<Long> idsProductos);
    
}
