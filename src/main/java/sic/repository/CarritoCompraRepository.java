package sic.repository;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.ItemCarritoCompra;
import sic.modelo.Producto;
import sic.modelo.Usuario;

public interface CarritoCompraRepository
    extends PagingAndSortingRepository<ItemCarritoCompra, Long> {

  Page<ItemCarritoCompra> findAllByUsuario(Usuario usuario, Pageable pageable);

  @Query(
      "SELECT SUM(icc.cantidad * p.precioLista) "
          + "FROM ItemCarritoCompra icc INNER JOIN icc.producto p "
          + "WHERE icc.usuario.id_Usuario = :idUsuario")
  BigDecimal calcularSubtotal(@Param("idUsuario") long idUsuario);

  @Query(
      "SELECT SUM(icc.cantidad) FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario")
  BigDecimal getCantArticulos(@Param("idUsuario") long idUsuario);

  @Query("SELECT COUNT(icc) FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario")
  Long getCantRenglones(@Param("idUsuario") long idUsuario);

  @Modifying
  @Query(
    "DELETE FROM ItemCarritoCompra icc WHERE icc.producto.idProducto = :idProducto")
  void eliminarItem(@Param("idProducto") long idProducto);

  @Modifying
  @Query(
      "DELETE FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario AND icc.producto.idProducto = :idProducto")
  void eliminarItemDelUsuario(@Param("idUsuario") long idUsuario, @Param("idProducto") long idProducto);

  @Modifying
  @Query("DELETE FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario")
  void eliminarTodosLosItemsDelUsuario(@Param("idUsuario") long idUsuario);

  @Modifying
  @Query("DELETE FROM ItemCarritoCompra icc WHERE icc.producto.idProducto = :idProducto")
  void eliminarTodosLosItemsDelProducto(@Param("idProducto") long idProducto);

  ItemCarritoCompra findByUsuarioAndProducto(Usuario usuario, Producto producto);
}
