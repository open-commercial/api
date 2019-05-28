package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.Empresa;
import sic.modelo.Pedido;

public interface PedidoRepository
    extends PagingAndSortingRepository<Pedido, Long>, QuerydslPredicateExecutor<Pedido> {

  Pedido findByNroPedidoAndEmpresaAndEliminado(long nroPedido, Empresa empresa, boolean eliminado);
}
