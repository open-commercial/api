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

public interface CarritoCompraRepository extends PagingAndSortingRepository<ItemCarritoCompra, Long> {

    Page<ItemCarritoCompra> findAllByUsuario(Usuario usuario, Pageable pageable);

    @Query("SELECT SUM(icc.importe) FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario")
    BigDecimal calcularTotal(@Param("idUsuario") long idUsuario);

    @Query("SELECT SUM(icc.cantidad) FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario")
    BigDecimal getCantArticulos(@Param("idUsuario") long idUsuario);
    
    @Query("SELECT COUNT(icc) FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario")
    Long getCantRenglones(@Param("idUsuario") long idUsuario);

    @Modifying
    @Query("DELETE FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario AND icc.producto.id_Producto = :idProducto")
    void eliminarItem(@Param("idUsuario") long idUsuario, @Param("idProducto") long idProducto);
    
    @Modifying
    @Query("DELETE FROM ItemCarritoCompra icc WHERE icc.usuario.id_Usuario = :idUsuario")
    void eliminarTodosLosItems(@Param("idUsuario") long idUsuario);
    
    ItemCarritoCompra findByUsuarioAndProducto(Usuario usuario, Producto producto);
}
