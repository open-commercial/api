package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import sic.modelo.QNotaDebitoCliente;
import sic.modelo.TipoDeComprobante;
import sic.repository.NotaDebitoClienteRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

public class NotaDebitoClienteRepositoryImpl implements NotaDebitoClienteRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public BigDecimal calcularTotalNotaDebitoCliente(BooleanBuilder builder) {
    QNotaDebitoCliente qNotaDebitoCliente = QNotaDebitoCliente.notaDebitoCliente;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qNotaDebitoCliente.total.sum())
        .from(qNotaDebitoCliente)
        .where(builder)
        .fetch()
        .get(0);
  }

  @Override
  public BigDecimal calcularIVANotaDebitoCliente(
      BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
    QNotaDebitoCliente qNotaDebitoCliente = QNotaDebitoCliente.notaDebitoCliente;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
      rsPredicate.or(qNotaDebitoCliente.tipoComprobante.eq(tipoComprobante));
    }
    builder.and(rsPredicate);
    return queryFactory
        .select(qNotaDebitoCliente.iva105Neto.add(qNotaDebitoCliente.iva21Neto).sum())
        .from(qNotaDebitoCliente)
        .where(builder)
        .fetch()
        .get(0);
  }

}
