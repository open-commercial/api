package org.opencommercial.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.opencommercial.model.QCaja;
import org.opencommercial.repository.CajaRepositoryCustom;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;

public class CajaRepositoryImpl implements CajaRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public BigDecimal getSaldoSistemaCajas(BooleanBuilder builder) {
        QCaja qcaja = QCaja.caja;
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        return queryFactory.select(qcaja.saldoSistema.sum()).from(qcaja).where(builder).fetch().get(0);
    }

    @Override
    public BigDecimal getSaldoRealCajas(BooleanBuilder builder) {
        QCaja qcaja = QCaja.caja;
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        return queryFactory.select(qcaja.saldoReal.sum()).from(qcaja).where(builder).fetch().get(0);
    }

}
