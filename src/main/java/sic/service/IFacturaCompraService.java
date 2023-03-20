package sic.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import sic.entity.FacturaCompra;
import sic.entity.Proveedor;
import sic.entity.Sucursal;
import sic.domain.TipoDeComprobante;
import sic.entity.criteria.BusquedaFacturaCompraCriteria;

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
