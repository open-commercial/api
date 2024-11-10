package org.opencommercial.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.opencommercial.model.QRecibo;
import org.opencommercial.repository.ReciboRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;

public class ReciboRepositoryImpl implements ReciboRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public BigDecimal getTotalRecibos(BooleanBuilder builder) {
    QRecibo qRecibo = QRecibo.recibo;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
      .select(qRecibo.monto.sum())
      .from(qRecibo)
      .where(builder)
      .fetch()
      .get(0);
  }
}
