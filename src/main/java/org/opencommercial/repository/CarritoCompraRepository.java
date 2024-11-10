package org.opencommercial.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.opencommercial.model.ItemCarritoCompra;
import org.opencommercial.model.Usuario;

public interface CarritoCompraRepository extends JpaRepository<ItemCarritoCompra, Long> {

  Page<ItemCarritoCompra> findAllByUsuario(Usuario usuario, Pageable pageable);

  List<ItemCarritoCompra> findAllByUsuarioOrderByIdItemCarritoCompraDesc(Usuario usuario);

  @Query("SELECT SUM(icc.cantidad) FROM ItemCarritoCompra icc WHERE icc.usuario.idUsuario = :idUsuario")
  BigDecimal getCantArticulos(@Param("idUsuario") long idUsuario);

  @Query("SELECT COUNT(icc) FROM ItemCarritoCompra icc WHERE icc.usuario.idUsuario = :idUsuario")
  Long getCantRenglones(@Param("idUsuario") long idUsuario);

  @Modifying
  @Query("DELETE FROM ItemCarritoCompra icc WHERE icc.producto.idProducto = :idProducto")
  void eliminarItem(@Param("idProducto") long idProducto);

  @Modifying
  @Query("DELETE FROM ItemCarritoCompra icc "
          + "WHERE icc.usuario.idUsuario = :idUsuario AND icc.producto.idProducto = :idProducto")
  void eliminarItemDelUsuario(@Param("idUsuario") long idUsuario, @Param("idProducto") long idProducto);

  @Modifying
  @Query("DELETE FROM ItemCarritoCompra icc WHERE icc.usuario.idUsuario = :idUsuario")
  void eliminarTodosLosItemsDelUsuario(@Param("idUsuario") long idUsuario);

  @Query("SELECT icc FROM ItemCarritoCompra icc WHERE icc.usuario.idUsuario = :idUsuario AND icc.producto.idProducto = :idProducto")
  ItemCarritoCompra findByUsuarioAndProducto(@Param("idUsuario") long idUsuario, @Param("idProducto") long idProducto);
}
