package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import sic.modelo.QNotaDebitoProveedor;
import sic.modelo.TipoDeComprobante;
import sic.repository.NotaDebitoProveedorRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

public class NotaDebitoProveedorRepositoryImpl implements NotaDebitoProveedorRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public BigDecimal calcularTotalNotaDebitoProveedor(BooleanBuilder builder) {
        QNotaDebitoProveedor qNotaDebitoProveedor = QNotaDebitoProveedor.notaDebitoProveedor;
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        return queryFactory
                .select(qNotaDebitoProveedor.total.sum())
                .from(qNotaDebitoProveedor)
                .where(builder)
                .fetch()
                .get(0);
    }

    @Override
    public BigDecimal calcularIVANotaDebitoProveedor(
            BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
        QNotaDebitoProveedor qNotaDebitoProveedor = QNotaDebitoProveedor.notaDebitoProveedor;
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        BooleanBuilder rsPredicate = new BooleanBuilder();
        for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
            rsPredicate.or(qNotaDebitoProveedor.tipoComprobante.eq(tipoComprobante));
        }
        builder.and(rsPredicate);
        return queryFactory
                .select(qNotaDebitoProveedor.iva105Neto.add(qNotaDebitoProveedor.iva21Neto).sum())
                .from(qNotaDebitoProveedor)
                .where(builder)
                .fetch()
                .get(0);
    }

}
