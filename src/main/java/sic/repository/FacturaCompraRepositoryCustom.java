package sic.repository;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaFacturaCompraCriteria;
import sic.modelo.FacturaCompra;
import sic.modelo.TipoDeComprobante;

public interface FacturaCompraRepositoryCustom {
    
    double calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

    double calcularIVA_Compra(BusquedaFacturaCompraCriteria criteria, TipoDeComprobante[] tipoComprobante);

    Page<FacturaCompra> buscarFacturasCompra(BusquedaFacturaCompraCriteria criteria);


}
