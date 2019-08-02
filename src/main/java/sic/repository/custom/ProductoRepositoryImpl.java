package sic.repository.custom;

import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import sic.modelo.QProducto;
import sic.repository.ProductoRepositoryCustom;

public class ProductoRepositoryImpl implements ProductoRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public BigDecimal calcularValorStock(BooleanBuilder builder) {
    QProducto qProducto = QProducto.producto;
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    return queryFactory.select(qProducto.cantidadSucursales.any().cantidad.multiply(qProducto.precioCosto).sum())
      .from(qProducto)
      .where(builder)
      .fetch().get(0);
  }

}
