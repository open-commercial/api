package sic.repository;

import com.querydsl.core.BooleanBuilder;

import java.math.BigDecimal;

public interface ReciboRepositoryCustom {

  BigDecimal getTotalRecibos(BooleanBuilder builder);

}
