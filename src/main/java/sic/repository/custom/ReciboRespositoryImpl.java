package sic.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import sic.modelo.QRecibo;
import sic.repository.ReciboRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

@Repository
public class ReciboRespositoryImpl implements ReciboRepositoryCustom {

//  @PersistenceContext private EntityManager em;
//
//  @Override
//  public BigDecimal calcularMontosRecibos(BooleanBuilder builder) {
//    QRecibo qRecibo = QRecibo.recibo;
//    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
//    return queryFactory.select(qRecibo.monto.sum()).from(qRecibo).where(builder).fetch().get(0);
//  }
}
