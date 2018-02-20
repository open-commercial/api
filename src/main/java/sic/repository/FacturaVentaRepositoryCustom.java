package sic.repository;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaFacturaVentaCriteria;
import sic.modelo.FacturaVenta;
import sic.modelo.TipoDeComprobante;

public interface FacturaVentaRepositoryCustom {

    BigDecimal calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria);

    BigDecimal calcularIVA_Venta(BusquedaFacturaVentaCriteria criteria, TipoDeComprobante[] tipoComprobante);

    BigDecimal calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria);

    Page<FacturaVenta> buscarFacturasVenta(BusquedaFacturaVentaCriteria criteria);

}
