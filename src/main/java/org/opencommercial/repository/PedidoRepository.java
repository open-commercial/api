package org.opencommercial.repository;

import org.opencommercial.model.EstadoPedido;
import org.opencommercial.model.Pedido;
import org.opencommercial.model.Sucursal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends
        JpaRepository<Pedido, Long>,
        QuerydslPredicateExecutor<Pedido> {

  Pedido findByNroPedidoAndSucursalAndEliminado(long nroPedido, Sucursal sucursal, boolean eliminado);

  boolean existsByNroPedidoAndSucursal(long nroPedido, Sucursal sucursal);

  @Query("SELECT p FROM Pedido p " + "WHERE p.estado = :estado " + "AND p.eliminado = false")
  Page<Pedido> findAllByEstadoAndEliminado(@Param("estado") EstadoPedido estado, Pageable page);
}
