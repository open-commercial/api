package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import sic.modelo.Nota;
import sic.modelo.QNota;
import sic.modelo.TipoDeComprobante;
import sic.repository.NotaRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.math.BigDecimal;

public class NotaRepositoryCustomImpl<T extends Nota, ID extends Serializable> implements NotaRepositoryCustom<T, ID> {

  @PersistenceContext private EntityManager em;

  @Override
  public BigDecimal calcularTotalNotas(BooleanBuilder builder) {
    QNota qNota = QNota.nota;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory.select(qNota.total.sum()).from(qNota).where(builder).fetch().get(0);
  }

  @Override
  public BigDecimal calcularIVANotas(BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
    QNota qNota = QNota.nota;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
      rsPredicate.or(qNota.tipoComprobante.eq(tipoComprobante));
    }
    builder.and(rsPredicate);
    return queryFactory
        .select(qNota.iva105Neto.add(qNota.iva21Neto).sum())
        .from(qNota)
        .where(builder)
        .fetch()
        .get(0);
  }
}
