package org.opencommercial.repository;

import com.querydsl.core.BooleanBuilder;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;

public interface NotaCreditoRepositoryCustom {

  BigDecimal calcularTotalCredito(BooleanBuilder builder);

  BigDecimal calcularIVACredito(BooleanBuilder builder, TipoDeComprobante[] tipoComprobantes);
}
