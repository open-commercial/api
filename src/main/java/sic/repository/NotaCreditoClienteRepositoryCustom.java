package sic.repository;

import com.querydsl.core.BooleanBuilder;
import sic.modelo.TipoDeComprobante;
import java.math.BigDecimal;

public interface NotaCreditoClienteRepositoryCustom {

    BigDecimal calcularTotalNotaCreditoCliente(BooleanBuilder builder);

    BigDecimal calcularIVANotaCreditoCliente(BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);

}
