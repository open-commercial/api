package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import sic.modelo.QNotaCredito;
import sic.modelo.TipoDeComprobante;
import sic.repository.NotaCreditoRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

@Repository
public class NotaCreditoRepositoryImpl implements NotaCreditoRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public BigDecimal calcularTotalCredito(BooleanBuilder builder) {
    QNotaCredito qNotaCredito = QNotaCredito.notaCredito;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qNotaCredito.total.sum())
        .from(qNotaCredito)
        .where(builder)
        .fetch()
        .get(0);
  }

  @Override
  public BigDecimal calcularIVACredito(
      BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
    QNotaCredito qNotaCredito = QNotaCredito.notaCredito;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
      rsPredicate.or(qNotaCredito.tipoComprobante.eq(tipoComprobante));
    }
    builder.and(rsPredicate);
    return queryFactory
        .select(qNotaCredito.iva105Neto.add(qNotaCredito.iva21Neto).sum())
        .from(qNotaCredito)
        .where(builder)
        .fetch()
        .get(0);
  }
}
