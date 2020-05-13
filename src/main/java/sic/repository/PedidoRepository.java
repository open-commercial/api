package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Sucursal;
import sic.modelo.Pedido;

public interface PedidoRepository
    extends PagingAndSortingRepository<Pedido, Long>, QuerydslPredicateExecutor<Pedido> {

  Pedido findByNroPedidoAndSucursalAndEliminado(
      long nroPedido, Sucursal sucursal, boolean eliminado);

  Pedido findByIdPaymentAndEliminado(String idPayment, boolean eliminado);

  @Modifying
  @Query("UPDATE Pedido p SET p.idPayment = :idPayment WHERE p.idPedido = :idPedido")
  int actualizarIdPaymentDePedido(
      @Param("idPedido") long idPedido, @Param("idPayment") String idPayment);
}
