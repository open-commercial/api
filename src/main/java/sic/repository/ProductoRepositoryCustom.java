package sic.repository;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;

public interface ProductoRepositoryCustom {

    BigDecimal calcularValorStock(BooleanBuilder builder);

}
