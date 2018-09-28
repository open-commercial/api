package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.FacturaCompra;

public interface FacturaCompraRepository
    extends FacturaRepository<FacturaCompra>,
        FacturaCompraRepositoryCustom,
        QueryDslPredicateExecutor<FacturaCompra> {

  @Override
  @Query("SELECT f FROM FacturaCompra f WHERE f.id_Factura = :idFactura AND f.eliminada = false")
  FacturaCompra findById(@Param("idFactura") long idFactura);
}
