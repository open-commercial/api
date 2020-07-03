package sic.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import sic.modelo.FacturaCompra;
import sic.modelo.Proveedor;
import sic.modelo.Sucursal;
import sic.modelo.TipoDeComprobante;
import sic.modelo.criteria.BusquedaFacturaCompraCriteria;

import java.math.BigDecimal;
import java.util.List;

public interface IFacturaCompraService {

  TipoDeComprobante[] getTiposDeComprobanteCompra(Sucursal sucursal, Proveedor proveedor);

  Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria);

  List<FacturaCompra> guardar(List<FacturaCompra> facturas);

  BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

  BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria);

  BooleanBuilder getBuilderCompra(BusquedaFacturaCompraCriteria criteria);
}
