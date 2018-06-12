package sic.repository;

import java.math.BigDecimal;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaFacturaVentaCriteria;
import sic.modelo.FacturaVenta;
import sic.modelo.TipoDeComprobante;
import sic.modelo.Usuario;

public interface FacturaVentaRepositoryCustom {

    BigDecimal calcularTotalFacturadoVenta(BooleanBuilder builder);

    BigDecimal calcularIVAVenta(BooleanBuilder builder, TipoDeComprobante[] tipoComprobante);

    BigDecimal calcularGananciaTotal(BooleanBuilder builder);

}
