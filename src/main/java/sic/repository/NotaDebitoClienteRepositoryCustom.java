package sic.repository;

import com.querydsl.core.BooleanBuilder;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;

public interface NotaDebitoClienteRepositoryCustom {

  BigDecimal calcularTotalNotaDebitoCliente(BooleanBuilder builder);

  BigDecimal calcularIVANotaDebitoCliente(
      BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);

}
