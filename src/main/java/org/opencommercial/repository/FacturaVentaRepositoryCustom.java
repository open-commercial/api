package org.opencommercial.repository;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import org.opencommercial.model.TipoDeComprobante;

public interface FacturaVentaRepositoryCustom {

    BigDecimal calcularTotalFacturadoVenta(BooleanBuilder builder);

    BigDecimal calcularIVAVenta(BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);

    BigDecimal calcularGananciaTotal(BooleanBuilder builder);

}
