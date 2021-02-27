package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Producto;
import sic.modelo.ProductoFavorito;

import java.util.List;

public interface ProductoFavoritoRepository
    extends PagingAndSortingRepository<ProductoFavorito, Long>,
        QuerydslPredicateExecutor<ProductoFavorito> {

  List<ProductoFavorito> findAllByCliente(Cliente cliente);

  boolean existsByClienteAndProducto(Cliente cliente, Producto producto);

  void deleteByClienteAndProducto(Cliente cliente, Producto producto);

  void deleteAllByCliente(Cliente cliente);

  @Query("SELECT COUNT(pf) FROM ProductoFavorito pf WHERE pf.cliente = :cliente")
  Long getCantidadDeArticulosEnFavoritos(@Param("cliente") Cliente cliente);

  @Query(
      "SELECT p from ProductoFavorito pf LEFT JOIN pf.producto p WHERE p.rubro.idRubro = :idRubro order by p.precioProducto.oferta desc, pf.idProductoFavorito desc")
  Page<Producto> buscarProductosRelacionadosPorRubro(@Param("idRubro") long idRubro, Pageable page);
}
