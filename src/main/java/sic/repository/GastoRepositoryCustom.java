package sic.repository;

import com.querydsl.core.BooleanBuilder;

import java.math.BigDecimal;

public interface GastoRepositoryCustom {

  BigDecimal getTotalGastos(BooleanBuilder builder);
}
