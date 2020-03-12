package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.RenglonPedido;

import java.util.List;

public interface RenglonPedidoRepository extends PagingAndSortingRepository<RenglonPedido, Long> {

  @Query(
      "SELECT rp FROM Pedido p INNER JOIN p.renglones rp INNER JOIN Producto producto on rp.idProductoItem = producto.idProducto"
          + " WHERE p.idPedido = :idPedido AND p.eliminado = false AND producto.eliminado = false order by rp.idRenglonPedido asc")
  List<RenglonPedido> findByIdPedidoOrderByIdRenglonPedido(@Param("idPedido") long idPedido);

    @Query("SELECT rp FROM Pedido p INNER JOIN p.renglones rp"
      + " WHERE p.idPedido = :idPedido AND p.eliminado = false order by rp.idProductoItem asc")
    List<RenglonPedido> findByIdPedidoOrderByIdProductoItem(@Param("idPedido") long idPedido);
    
}
