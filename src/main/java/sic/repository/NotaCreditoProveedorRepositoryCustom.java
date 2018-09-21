package sic.repository;

import com.querydsl.core.BooleanBuilder;
import sic.modelo.TipoDeComprobante;
import java.math.BigDecimal;

public interface NotaCreditoProveedorRepositoryCustom {

  BigDecimal calcularTotalNotaCreditoProveedor(BooleanBuilder builder);

  BigDecimal calcularIVANotaCreditoProveedor(
      BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);
}
