package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.FacturaCompra;
import sic.modelo.Proveedor;
import sic.modelo.Sucursal;
import sic.modelo.TipoDeComprobante;
import sic.modelo.criteria.BusquedaFacturaCompraCriteria;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

public interface IFacturaCompraService {

    TipoDeComprobante[] getTipoFacturaCompra(Sucursal sucursal, Proveedor proveedor);

    Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria);

    List<FacturaCompra> guardar(@Valid List<FacturaCompra> facturas);

    BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

    BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria);
}
