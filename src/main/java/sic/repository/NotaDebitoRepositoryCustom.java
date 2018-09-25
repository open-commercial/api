package sic.repository;

import com.querydsl.core.BooleanBuilder;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;

public interface NotaDebitoRepositoryCustom {

  BigDecimal calcularTotalDebito(BooleanBuilder builder);

  BigDecimal calcularIVADebito(BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);
}
