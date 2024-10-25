package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import sic.modelo.QGasto;
import sic.repository.GastoRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;

public class GastoRepositoryImpl implements GastoRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public BigDecimal getTotalGastos(BooleanBuilder builder) {
    QGasto qGasto = QGasto.gasto;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory
      .select(qGasto.monto.sum())
      .from(qGasto)
      .where(builder)
      .fetch()
      .get(0);
  }
}
