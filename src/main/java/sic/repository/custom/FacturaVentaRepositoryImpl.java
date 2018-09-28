package sic.repository.custom;

import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import sic.modelo.*;
import sic.repository.FacturaVentaRepositoryCustom;

@Repository
public class FacturaVentaRepositoryImpl implements FacturaVentaRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public BigDecimal calcularTotalFacturadoVenta(BooleanBuilder builder) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qFacturaVenta.total.sum())
        .from(qFacturaVenta)
        .where(builder)
        .fetch()
        .get(0);
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
        .select(qFacturaVenta.iva_105_neto.add(qFacturaVenta.iva_21_neto).sum())
        .from(qFacturaVenta)
        .where(builder)
        .fetch()
        .get(0);
  }

  @Override
  public BigDecimal calcularGananciaTotal(BooleanBuilder builder) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    QRenglonFactura qRenglonFactura = QRenglonFactura.renglonFactura;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
        .select(qRenglonFactura.ganancia_neto.multiply(qRenglonFactura.cantidad).sum())
        .from(qFacturaVenta)
        .leftJoin(qFacturaVenta.renglones, qRenglonFactura)
        .where(builder)
        .fetch()
        .get(0);
  }
}
