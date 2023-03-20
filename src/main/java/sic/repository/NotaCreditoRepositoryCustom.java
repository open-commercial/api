package sic.repository;

import com.querydsl.core.BooleanBuilder;
import sic.domain.TipoDeComprobante;

import java.math.BigDecimal;

public interface NotaCreditoRepositoryCustom {

  BigDecimal calcularTotalCredito(BooleanBuilder builder);

  BigDecimal calcularIVACredito(BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes);
}
