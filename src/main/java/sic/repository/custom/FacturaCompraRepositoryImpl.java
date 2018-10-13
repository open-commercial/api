package sic.repository.custom;

import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import sic.modelo.QFacturaCompra;
import sic.modelo.TipoDeComprobante;
import sic.repository.FacturaCompraRepositoryCustom;

@Repository
public class FacturaCompraRepositoryImpl implements FacturaCompraRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public BigDecimal calcularTotalFacturadoCompra(BooleanBuilder builder) {
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qFacturaCompra.total.sum())
        .from(qFacturaCompra)
        .where(builder)
        .fetch()
        .get(0);
  }

  @Override
  public BigDecimal calcularIVACompra(
      BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
      rsPredicate.or(qFacturaCompra.tipoComprobante.eq(tipoComprobante));
    }
    builder.and(rsPredicate);
    return queryFactory
        .select(qFacturaCompra.iva105Neto.add(qFacturaCompra.iva21Neto).sum())
        .from(qFacturaCompra)
        .where(builder)
        .fetch()
        .get(0);
  }
}
