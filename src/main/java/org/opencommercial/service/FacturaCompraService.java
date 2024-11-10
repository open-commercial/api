package org.opencommercial.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.opencommercial.model.FacturaCompra;
import org.opencommercial.model.Proveedor;
import org.opencommercial.model.Sucursal;
import org.opencommercial.model.TipoDeComprobante;
import org.opencommercial.model.criteria.BusquedaFacturaCompraCriteria;

import java.math.BigDecimal;
import java.util.List;

public interface FacturaCompraService {

  TipoDeComprobante[] getTiposDeComprobanteCompra(Sucursal sucursal, Proveedor proveedor);

  Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria);

  List<FacturaCompra> guardar(List<FacturaCompra> facturas);

  BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

  BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria);

  BooleanBuilder getBuilderCompra(BusquedaFacturaCompraCriteria criteria);
}
