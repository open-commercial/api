package org.opencommercial.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.opencommercial.model.QFacturaVenta;
import org.opencommercial.model.QRenglonFactura;
import org.opencommercial.model.TipoDeComprobante;
import org.opencommercial.repository.FacturaVentaRepositoryCustom;

import java.math.BigDecimal;

public class FacturaVentaRepositoryImpl implements FacturaVentaRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public BigDecimal calcularTotalFacturadoVenta(BooleanBuilder builder) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qFacturaVenta.total.sum())
        .from(qFacturaVenta)
        .where(builder)
        .fetch()
        .getFirst();
  }

  @Override
  public BigDecimal calcularIVAVenta(BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    for (TipoDeComprobante tipoComprobante : tipoComprobantes) {
      rsPredicate.or(qFacturaVenta.tipoComprobante.eq(tipoComprobante));
    }
    builder.and(rsPredicate);
    return queryFactory
        .select(qFacturaVenta.iva105Neto.add(qFacturaVenta.iva21Neto).sum())
        .from(qFacturaVenta)
        .where(builder)
        .fetch()
        .getFirst();
  }

  @Override
  public BigDecimal calcularGananciaTotal(BooleanBuilder builder) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    QRenglonFactura qRenglonFactura = QRenglonFactura.renglonFactura;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qRenglonFactura.gananciaNeto.multiply(qRenglonFactura.cantidad).sum())
        .from(qFacturaVenta)
        .leftJoin(qFacturaVenta.renglones, qRenglonFactura)
        .where(builder)
        .fetch()
        .getFirst();
  }
}
