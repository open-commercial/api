package sic.repository;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import sic.domain.TipoDeComprobante;

public interface FacturaVentaRepositoryCustom {

    BigDecimal calcularTotalFacturadoVenta(BooleanBuilder builder);

    BigDecimal calcularIVAVenta(BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);

    BigDecimal calcularGananciaTotal(BooleanBuilder builder);

}
