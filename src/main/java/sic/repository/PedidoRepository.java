package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

public interface PedidoRepository
    extends PagingAndSortingRepository<Pedido, Long>, QuerydslPredicateExecutor<Pedido>, RevisionRepository<Pedido, Long, Long> {

  Pedido findByNroPedidoAndSucursalAndEliminado(
      long nroPedido, Sucursal sucursal, boolean eliminado);

  boolean existsByNroPedidoAndSucursal(long nroPedido, Sucursal sucursal);

  @Query("SELECT p FROM Pedido p " + "WHERE p.estado = :estado " + "AND p.eliminado = false")
  Page<Pedido> findAllByEstadoAndEliminado(@Param("estado") EstadoPedido estado, Pageable page);
}
