package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Cliente;
import sic.modelo.Producto;
import sic.modelo.ProductoFavorito;

import java.util.List;

public interface ProductoFavoritoRepository
    extends PagingAndSortingRepository<ProductoFavorito, Long>,
        QuerydslPredicateExecutor<ProductoFavorito> {

  List<ProductoFavorito> findAllByCliente(Cliente cliente);

  void deleteByClienteAndProducto(Cliente cliente, Producto producto);
}
