package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.Producto;
import sic.modelo.ProductoFavorito;

public interface IProductoFavoritoService {

    ProductoFavorito getProductoFavoritoPorId(Long idProductoFavorito);

    ProductoFavorito guardarProductoFavorito(long idProducto, long idCliente);

    Page<Producto> getProductosFavoritosDelCliente(long idCliente);

    void quitarProductoDeFavoritos(long idProductoFavorito);
}
