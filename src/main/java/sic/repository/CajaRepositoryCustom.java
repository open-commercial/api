package sic.repository;

import com.querydsl.core.BooleanBuilder;

import java.math.BigDecimal;

public interface CajaRepositoryCustom {

    BigDecimal getSaldoSistemaCajas(BooleanBuilder builder);

    BigDecimal getSaldoRealCajas(BooleanBuilder builder);

}
