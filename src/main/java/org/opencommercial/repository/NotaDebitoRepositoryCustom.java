package org.opencommercial.repository;

import com.querydsl.core.BooleanBuilder;
import org.opencommercial.model.TipoDeComprobante;

import java.math.BigDecimal;

public interface NotaDebitoRepositoryCustom {

  BigDecimal calcularTotalDebito(BooleanBuilder builder);

  BigDecimal calcularIVADebito(BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);
}
