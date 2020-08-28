package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Cliente;
import sic.modelo.ProductoFavorito;

import java.util.List;

public interface ProductoFavoritoRepository
    extends PagingAndSortingRepository<ProductoFavorito, Long>,
        QuerydslPredicateExecutor<ProductoFavorito> {

  List<ProductoFavorito> findByCliente(Cliente cliente);
}
