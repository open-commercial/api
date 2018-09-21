package sic.repository;

import com.querydsl.core.BooleanBuilder;
import sic.modelo.TipoDeComprobante;

import java.math.BigDecimal;

public interface NotaDebitoProveedorRepositoryCustom {

    BigDecimal calcularTotalNotaDebitoProveedor(BooleanBuilder builder);

    BigDecimal calcularIVANotaDebitoProveedor(
            BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);

}
