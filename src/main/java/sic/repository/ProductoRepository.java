package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Producto;

import java.util.List;

public interface ProductoRepository extends PagingAndSortingRepository<Producto, Long>,
  QuerydslPredicateExecutor<Producto>, ProductoRepositoryCustom {

  Producto findByCodigoAndEliminado(String codigo, boolean eliminado);

  Producto findByDescripcionAndEliminado(String descripcion, boolean eliminado);

  List<Producto> findByIdProductoInOrderByIdProductoAsc(List<Long> idsProductos);

  @Modifying
  @Query("UPDATE Producto p SET p.urlImagen = :urlImagen WHERE p.idProducto = :idProducto")
  int actualizarUrlImagen(@Param("idProducto") long idProducto, @Param("urlImagen") String urlImagen);
}
