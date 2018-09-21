package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import sic.modelo.QNotaCreditoCliente;
import sic.modelo.TipoDeComprobante;
import sic.repository.NotaCreditoClienteRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

@Repository
public class NotaCreditoClienteRepositoryImpl implements NotaCreditoClienteRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public BigDecimal calcularTotalNotaCreditoCliente(BooleanBuilder builder) {
    QNotaCreditoCliente qNotaCreditoCliente = QNotaCreditoCliente.notaCreditoCliente;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qNotaCreditoCliente.total.sum())
        .from(qNotaCreditoCliente)
        .where(builder)
        .fetch()
        .get(0);
  }

  @Override
  public BigDecimal calcularIVANotaCreditoCliente(
      BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
    QNotaCreditoCliente qNotaCreditoCliente = QNotaCreditoCliente.notaCreditoCliente;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
      rsPredicate.or(qNotaCreditoCliente.tipoComprobante.eq(tipoComprobante));
    }
    builder.and(rsPredicate);
    return queryFactory
        .select(qNotaCreditoCliente.iva105Neto.add(qNotaCreditoCliente.iva21Neto).sum())
        .from(qNotaCreditoCliente)
        .where(builder)
        .fetch()
        .get(0);
  }
}
