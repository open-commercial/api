package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import sic.modelo.QNotaCreditoProveedor;
import sic.modelo.TipoDeComprobante;
import sic.repository.NotaCreditoProveedorRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

public class NotaCreditoProveedorRepositoryImpl implements NotaCreditoProveedorRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public BigDecimal calcularTotalNotaCreditoProveedor(BooleanBuilder builder) {
    QNotaCreditoProveedor qNotaCreditoProveedor = QNotaCreditoProveedor.notaCreditoProveedor;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qNotaCreditoProveedor.total.sum())
        .from(qNotaCreditoProveedor)
        .where(builder)
        .fetch()
        .get(0);
  }

  @Override
  public BigDecimal calcularIVANotaCreditoProveedor(
      BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
    QNotaCreditoProveedor qNotaCreditoProveedor = QNotaCreditoProveedor.notaCreditoProveedor;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
      rsPredicate.or(qNotaCreditoProveedor.tipoComprobante.eq(tipoComprobante));
    }
    builder.and(rsPredicate);
    return queryFactory
        .select(qNotaCreditoProveedor.iva105Neto.add(qNotaCreditoProveedor.iva21Neto).sum())
        .from(qNotaCreditoProveedor)
        .where(builder)
        .fetch()
        .get(0);
  }

}
