package sic.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sic.entity.FacturaCompra;

public interface FacturaCompraRepository
    extends FacturaRepository<FacturaCompra>,
        FacturaCompraRepositoryCustom,
        QuerydslPredicateExecutor<FacturaCompra> {}
