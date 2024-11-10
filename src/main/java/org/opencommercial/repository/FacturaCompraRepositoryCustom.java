package org.opencommercial.repository;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import org.opencommercial.model.TipoDeComprobante;

public interface FacturaCompraRepositoryCustom {

  BigDecimal calcularTotalFacturadoCompra(BooleanBuilder builder);

  BigDecimal calcularIVACompra(BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);
}
