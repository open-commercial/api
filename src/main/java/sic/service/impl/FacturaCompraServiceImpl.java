package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaCompraCriteria;
import sic.repository.FacturaCompraRepository;
import sic.service.ICuentaCorrienteService;
import sic.service.IFacturaCompraService;
import sic.service.IFacturaService;
import sic.service.IProductoService;
import sic.util.CustomValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaCompraServiceImpl implements IFacturaCompraService {

  private final IFacturaService facturaService;
  private final FacturaCompraRepository facturaCompraRepository;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IProductoService productoService;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  @Lazy
  public FacturaCompraServiceImpl(
    IFacturaService facturaService,
    FacturaCompraRepository facturaCompraRepository,
    ICuentaCorrienteService cuentaCorrienteService,
    IProductoService productoService,
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
    if (criteria.getFechaDesdeAlta() != null || criteria.getFechaHastaAlta() != null) {
      if (criteria.getFechaDesdeAlta() != null && criteria.getFechaHastaAlta() != null) {
        criteria.setFechaDesdeAlta(criteria.getFechaDesdeAlta().withHour(0).withMinute(0).withSecond(0));
        criteria.setFechaHastaAlta(
                criteria
                        .getFechaHastaAlta()
                        .withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(999999999));
        builder.and(
                qFacturaCompra.fechaAlta.between(criteria.getFechaDesdeAlta(), criteria.getFechaHastaAlta()));
      } else if (criteria.getFechaDesdeAlta() != null) {
        criteria.setFechaDesdeAlta(criteria.getFechaDesdeAlta().withHour(0).withMinute(0).withSecond(0));
        builder.and(qFacturaCompra.fechaAlta.after(criteria.getFechaDesdeAlta()));
      } else if (criteria.getFechaHastaAlta() != null) {
        criteria.setFechaHastaAlta(
                criteria
                        .getFechaHastaAlta()
                        .withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(999999999));
        builder.and(qFacturaCompra.fechaAlta.before(criteria.getFechaHastaAlta()));
      }
    }
    if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
        criteria.setFechaHasta(
            criteria
                .getFechaHasta()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999));
        builder.and(
            qFacturaCompra.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
        builder.and(qFacturaCompra.fecha.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        criteria.setFechaHasta(
            criteria
                .getFechaHasta()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999));
        builder.and(qFacturaCompra.fecha.before(criteria.getFechaHasta()));
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
