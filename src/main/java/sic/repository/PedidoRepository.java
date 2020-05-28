package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Sucursal;
import sic.modelo.Pedido;

public interface PedidoRepository
    extends PagingAndSortingRepository<Pedido, Long>, QuerydslPredicateExecutor<Pedido> {

  Pedido findByNroPedidoAndSucursalAndEliminado(
      long nroPedido, Sucursal sucursal, boolean eliminado);
}
