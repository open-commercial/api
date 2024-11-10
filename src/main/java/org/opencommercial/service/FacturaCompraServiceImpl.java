package org.opencommercial.service;

import com.querydsl.core.BooleanBuilder;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaFacturaCompraCriteria;
import org.opencommercial.repository.FacturaCompraRepository;
import org.opencommercial.util.CustomValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaCompraServiceImpl implements FacturaCompraService {

  private final FacturaService facturaService;
  private final FacturaCompraRepository facturaCompraRepository;
  private final CuentaCorrienteService cuentaCorrienteService;
  private final ProductoService productoService;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  @Lazy
  public FacturaCompraServiceImpl(
    FacturaService facturaService,
    FacturaCompraRepository facturaCompraRepository,
    CuentaCorrienteService cuentaCorrienteService,
    ProductoService productoService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.facturaService = facturaService;
    this.facturaCompraRepository = facturaCompraRepository;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.productoService = productoService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public TipoDeComprobante[] getTiposDeComprobanteCompra(Sucursal sucursal, Proveedor proveedor) {
    if (CategoriaIVA.discriminaIVA(sucursal.getCategoriaIVA())) {
      if (CategoriaIVA.discriminaIVA(proveedor.getCategoriaIVA())) {
        TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
        tiposPermitidos[0] = TipoDeComprobante.FACTURA_A;
        tiposPermitidos[1] = TipoDeComprobante.FACTURA_B;
        tiposPermitidos[2] = TipoDeComprobante.FACTURA_X;
        tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
        return tiposPermitidos;
      } else {
        TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
        tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
        tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
        tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
        return tiposPermitidos;
      }
    } else {
      if (CategoriaIVA.discriminaIVA(proveedor.getCategoriaIVA())) {
        TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
        tiposPermitidos[0] = TipoDeComprobante.FACTURA_B;
        tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
        tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
        return tiposPermitidos;
      } else {
        TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
        tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
        tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
        tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
        return tiposPermitidos;
      }
    }
  }

  @Override
  public Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria) {
    return facturaCompraRepository.findAll(
        this.getBuilderCompra(criteria),
        facturaService.getPageable(
            (criteria.getPagina() == null || criteria.getPagina() < 0) ? 0 : criteria.getPagina(),
            criteria.getOrdenarPor(),
            criteria.getSentido()));
  }

  @Override
  public BooleanBuilder getBuilderCompra(BusquedaFacturaCompraCriteria criteria) {
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getIdSucursal() == null) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_busqueda_sin_sucursal", null, Locale.getDefault()));
    }
    builder.and(
        qFacturaCompra
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturaCompra.eliminada.eq(false)));
    if (criteria.getFechaAltaDesde() != null || criteria.getFechaAltaHasta() != null) {
      if (criteria.getFechaAltaDesde() != null && criteria.getFechaAltaHasta() != null) {
        criteria.setFechaAltaDesde(criteria.getFechaAltaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        criteria.setFechaAltaHasta(
                criteria
                        .getFechaAltaHasta()
                        .withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(999999999));
        builder.and(
                qFacturaCompra.fechaAlta.between(criteria.getFechaAltaDesde(), criteria.getFechaAltaHasta()));
      } else if (criteria.getFechaAltaDesde() != null) {
        criteria.setFechaAltaDesde(criteria.getFechaAltaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        builder.and(qFacturaCompra.fechaAlta.after(criteria.getFechaAltaDesde()));
      } else if (criteria.getFechaAltaHasta() != null) {
        criteria.setFechaAltaHasta(
                criteria
                        .getFechaAltaHasta()
                        .withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(999999999));
        builder.and(qFacturaCompra.fechaAlta.before(criteria.getFechaAltaHasta()));
      }
    }
    if (criteria.getFechaFacturaDesde() != null || criteria.getFechaFacturaHasta() != null) {
      if (criteria.getFechaFacturaDesde() != null && criteria.getFechaFacturaHasta() != null) {
        criteria.setFechaFacturaDesde(
            criteria.getFechaFacturaDesde()
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0));
        criteria.setFechaFacturaHasta(
            criteria
                .getFechaFacturaHasta()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999));
        builder.and(
            qFacturaCompra.fecha.between(
                criteria.getFechaFacturaDesde(), criteria.getFechaFacturaHasta()));
      } else if (criteria.getFechaFacturaDesde() != null) {
        criteria.setFechaFacturaDesde(
            criteria.getFechaFacturaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        builder.and(qFacturaCompra.fecha.after(criteria.getFechaFacturaDesde()));
      } else if (criteria.getFechaFacturaHasta() != null) {
        criteria.setFechaFacturaHasta(
            criteria
                .getFechaFacturaHasta()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999));
        builder.and(qFacturaCompra.fecha.before(criteria.getFechaFacturaHasta()));
      }
    }
    if (criteria.getIdProveedor() != null)
      builder.and(qFacturaCompra.proveedor.idProveedor.eq(criteria.getIdProveedor()));
    if (criteria.getTipoComprobante() != null)
      builder.and(qFacturaCompra.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.getIdProducto() != null)
      builder.and(qFacturaCompra.renglones.any().idProductoItem.eq(criteria.getIdProducto()));
    if (criteria.getNumSerie() != null && criteria.getNumFactura() != null)
      builder
          .and(qFacturaCompra.numSerie.eq(criteria.getNumSerie()))
          .and(qFacturaCompra.numFactura.eq(criteria.getNumFactura()));
    return builder;
  }

  @Override
  @Transactional
  public List<FacturaCompra> guardar(List<FacturaCompra> facturas) {
    facturas.forEach(customValidator::validar);
    this.calcularValoresFacturasCompraAndActualizarStock(facturas);
    List<FacturaCompra> facturasProcesadas = new ArrayList<>();
    for (Factura f : facturas) {
      FacturaCompra facturaGuardada = null;
      if (f instanceof FacturaCompra) {
        facturaGuardada =
            facturaCompraRepository.save((FacturaCompra) facturaService.procesarFactura(f));
        this.cuentaCorrienteService.asentarEnCuentaCorriente(facturaGuardada);
      }
      facturasProcesadas.add(facturaGuardada);
    }
    return facturasProcesadas;
  }

  @Override
  public BigDecimal calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria) {
    BigDecimal totalFacturado =
        facturaCompraRepository.calcularTotalFacturadoCompra(this.getBuilderCompra(criteria));
    return (totalFacturado != null ? totalFacturado : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria) {
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A};
    BigDecimal ivaCompra =
        facturaCompraRepository.calcularIVACompra(this.getBuilderCompra(criteria), tipoFactura);
    return (ivaCompra != null ? ivaCompra : BigDecimal.ZERO);
  }

  private void calcularValoresFacturasCompraAndActualizarStock(List<FacturaCompra> facturas) {
    facturas.forEach(
        facturaCompra -> {
          facturaService.calcularValoresFactura(facturaCompra);
          productoService.actualizarStockFacturaCompra(
              facturaService.getIdsProductosYCantidades(facturaCompra),
              facturaCompra.getIdSucursal(),
              TipoDeOperacion.ALTA,
              Movimiento.COMPRA);
        });
  }
}
