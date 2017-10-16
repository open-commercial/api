package sic.repository;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaFacturaVentaCriteria;
import sic.modelo.FacturaVenta;
import sic.modelo.TipoDeComprobante;

public interface FacturaVentaRepositoryCustom {

    double calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria);

    double calcularIVA_Venta(BusquedaFacturaVentaCriteria criteria, TipoDeComprobante[] tipoComprobante);

    double calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria);

    Page<FacturaVenta> buscarFacturasVenta(BusquedaFacturaVentaCriteria criteria);

}
