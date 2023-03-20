package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.entity.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends PagingAndSortingRepository<Producto, Long>,
  QuerydslPredicateExecutor<Producto>, ProductoRepositoryCustom {

  Optional<Producto> findByCodigoAndEliminado(String codigo, boolean eliminado);

  Producto findByDescripcionAndEliminado(String descripcion, boolean eliminado);

  List<Producto> findAllByEliminado(boolean eliminado);

  @Modifying
  @Query("UPDATE Producto p SET p.urlImagen = :urlImagen WHERE p.idProducto = :idProducto")
  int actualizarUrlImagen(@Param("idProducto") long idProducto, @Param("urlImagen") String urlImagen);

  @Modifying
  @Query(
      "UPDATE Producto p SET p.cantidadProducto.cantidadReservada = p.cantidadProducto.cantidadReservada + :cantidad "
          + "WHERE p.idProducto = :idProducto")
  int actualizarCantidadReservada(
      @Param("idProducto") long idProducto, @Param("cantidad") BigDecimal cantidad);

  @Query(
      "SELECT p from Producto p WHERE p.rubro.idRubro = :idRubro AND p.publico = true AND p.idProducto <> :idProducto "
          + "AND p.eliminado = false order by p.precioProducto.oferta desc")
  Page<Producto> buscarProductosRelacionadosPorRubro(
      @Param("idRubro") long idRubro, @Param("idProducto") long idProducto, Pageable page);
}
