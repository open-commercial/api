package org.opencommercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.Cliente;
import org.opencommercial.model.Producto;
import org.opencommercial.model.ProductoFavorito;

import java.util.List;

public interface ProductoFavoritoRepository extends
        JpaRepository<ProductoFavorito, Long>,
        QuerydslPredicateExecutor<ProductoFavorito> {

  List<ProductoFavorito> findAllByCliente(Cliente cliente);

  boolean existsByClienteAndProducto(Cliente cliente, Producto producto);

  void deleteByClienteAndProducto(Cliente cliente, Producto producto);

  void deleteAllByCliente(Cliente cliente);

  void deleteAllByProducto(Producto producto);

  @Query("SELECT COUNT(pf) FROM ProductoFavorito pf WHERE pf.cliente = :cliente")
  Long getCantidadDeArticulosEnFavoritos(@Param("cliente") Cliente cliente);
}
