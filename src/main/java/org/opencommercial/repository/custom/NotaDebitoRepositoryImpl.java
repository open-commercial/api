package org.opencommercial.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.opencommercial.model.QNotaDebito;
import org.opencommercial.model.TipoDeComprobante;
import org.opencommercial.repository.NotaDebitoRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;

public class NotaDebitoRepositoryImpl implements NotaDebitoRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public BigDecimal calcularTotalDebito(BooleanBuilder builder) {
        QNotaDebito qNotaDebito = QNotaDebito.notaDebito;
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        return queryFactory
                .select(qNotaDebito.total.sum())
                .from(qNotaDebito)
                .where(builder)
                .fetch()
                .get(0);
    }

    @Override
    public BigDecimal calcularIVADebito(
            BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
        QNotaDebito qNotaDebito = QNotaDebito.notaDebito;
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        BooleanBuilder rsPredicate = new BooleanBuilder();
        for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
            rsPredicate.or(qNotaDebito.tipoComprobante.eq(tipoComprobante));
        }
        builder.and(rsPredicate);
        return queryFactory
                .select(qNotaDebito.iva105Neto.add(qNotaDebito.iva21Neto).sum())
                .from(qNotaDebito)
                .where(builder)
                .fetch()
                .get(0);
    }
}
