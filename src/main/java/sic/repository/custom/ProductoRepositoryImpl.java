package sic.repository.custom;

import java.math.BigDecimal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
    return queryFactory
        .select(qProducto.cantidadProducto.cantidadTotalEnSucursales.multiply(qProducto.precioProducto.precioCosto).sum())
        .from(qProducto)
        .where(builder)
        .fetch()
        .get(0);
  }
}
