package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import sic.modelo.QCaja;
import sic.repository.CajaRepositoryCustom;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import java.math.BigDecimal;

@Repository
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
