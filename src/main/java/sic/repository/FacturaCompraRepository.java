package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.modelo.FacturaCompra;

public interface FacturaCompraRepository
    extends FacturaRepository<FacturaCompra>,
        FacturaCompraRepositoryCustom,
        QuerydslPredicateExecutor<FacturaCompra> {}
