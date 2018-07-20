package sic.repository;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import sic.modelo.BusquedaFacturaCompraCriteria;
import sic.modelo.FacturaCompra;
import sic.modelo.TipoDeComprobante;

public interface FacturaCompraRepositoryCustom {

  BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria);

  BigDecimal calcularIVACompra(
      BusquedaFacturaCompraCriteria criteria, TipoDeComprobante[] tipoComprobante);

  Page<FacturaCompra> buscarFacturasCompra(BusquedaFacturaCompraCriteria criteria);
}
