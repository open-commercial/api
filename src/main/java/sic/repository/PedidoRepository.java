package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Pedido;

public interface PedidoRepository extends PagingAndSortingRepository<Pedido, Long>, QueryDslPredicateExecutor<Pedido> {
    
    @Query("SELECT p FROM Pedido p WHERE p.id_Pedido = :idPedido AND p.eliminado = false")
    Pedido findById(@Param("idPedido") long idPedido);

    Pedido findByNroPedidoAndEmpresaAndEliminado(long nroPedido, Empresa empresa, boolean eliminado);

    Pedido findTopByEmpresaAndEliminadoOrderByNroPedidoDesc(Empresa empresa, boolean eliminado);

}
