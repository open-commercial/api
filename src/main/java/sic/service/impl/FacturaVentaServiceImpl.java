package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.repository.FacturaVentaRepository;
import sic.service.*;
import sic.util.CalculosComprobante;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Validated
public class FacturaVentaServiceImpl implements IFacturaVentaService {

  private final FacturaVentaRepository facturaVentaRepository;
  private final IAfipService afipService;
  private final IReciboService reciboService;
  private final ICorreoElectronicoService correoElectronicoService;
  private final IPedidoService pedidoService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IConfiguracionSucursalService configuracionSucursalService;
  private final IFacturaService facturaService;
  private final IProductoService productoService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  @Lazy
  public FacturaVentaServiceImpl(
      FacturaVentaRepository facturaVentaRepository,
      IAfipService afipService,
      IReciboService reciboService,
      ICorreoElectronicoService correoElectronicoService,
      IPedidoService pedidoService,
      IUsuarioService usuarioService,
      IClienteService clienteService,
      ICuentaCorrienteService cuentaCorrienteService,
      IConfiguracionSucursalService configuracionSucursalService,
      IFacturaService facturaService,
      IProductoService productoService,
      MessageSource messageSource) {
    this.facturaVentaRepository = facturaVentaRepository;
    this.reciboService = reciboService;
    this.afipService = afipService;
    this.correoElectronicoService = correoElectronicoService;
    this.pedidoService = pedidoService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.configuracionSucursalService = configuracionSucursalService;
    this.facturaService = facturaService;
    this.productoService = productoService;
    this.messageSource = messageSource;
  }

  @Override
  public TipoDeComprobante[] getTiposDeComprobanteVenta(Sucursal sucursal, Cliente cliente) {
    if (CategoriaIVA.discriminaIVA(sucursal.getCategoriaIVA())) {
      if (CategoriaIVA.discriminaIVA(cliente.getCategoriaIVA())) {
        TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
        tiposPermitidos[0] = TipoDeComprobante.FACTURA_A;
        tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
        tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
        return tiposPermitidos;
      } else {
        TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
        tiposPermitidos[0] = TipoDeComprobante.FACTURA_B;
        tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
        tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
        return tiposPermitidos;
      }
    } else {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[3];
      tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
      tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
      tiposPermitidos[2] = TipoDeComprobante.PRESUPUESTO;
      return tiposPermitidos;
    }
  }

  @Override
  public List<Factura> getFacturasDelPedido(Long idPedido) {
    return facturaVentaRepository.findAllByPedidoAndEliminada(
        pedidoService.getPedidoNoEliminadoPorId(idPedido), false);
  }

  @Override
  public List<RenglonFactura> getRenglonesPedidoParaFacturar(
      long idPedido, TipoDeComprobante tipoDeComprobante) {
    List<RenglonFactura> renglonesRestantes = new ArrayList<>();
    List<RenglonPedido> renglonesPedido =
        pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(idPedido);
    Map<Long, RenglonFactura> renglonesDeFacturas =
        pedidoService.getRenglonesFacturadosDelPedido(idPedido);
    if (renglonesDeFacturas != null) {
      renglonesPedido.forEach(
          r -> {
            if (renglonesDeFacturas.containsKey(r.getIdProductoItem())) {
              if (r.getCantidad()
                      .compareTo(renglonesDeFacturas.get(r.getIdProductoItem()).getCantidad())
                  > 0) {
                renglonesRestantes.add(
                    facturaService.calcularRenglon(
                        tipoDeComprobante,
                        Movimiento.VENTA,
                        r.getCantidad()
                            .subtract(renglonesDeFacturas.get(r.getIdProductoItem()).getCantidad()),
                        r.getIdProductoItem(),
                        null));
              }
            } else {
              renglonesRestantes.add(
                  facturaService.calcularRenglon(
                      tipoDeComprobante,
                      Movimiento.VENTA,
                      r.getCantidad(),
                      r.getIdProductoItem(),
                      null));
            }
          });
    } else {
      renglonesPedido.forEach(
          r ->
              renglonesRestantes.add(
                  facturaService.calcularRenglon(
                      tipoDeComprobante,
                      Movimiento.VENTA,
                      r.getCantidad(),
                      r.getIdProductoItem(),
                      null)));
    }
    return renglonesRestantes;
  }

  @Override
  public boolean pedidoTotalmenteFacturado(Pedido pedido) {
    boolean facturado = false;
    Map<Long, RenglonFactura> renglonesDeFacturas =
        pedidoService.getRenglonesFacturadosDelPedido(pedido.getIdPedido());
    if (!renglonesDeFacturas.isEmpty()) {
      for (RenglonPedido r : pedido.getRenglones()) {
        if (renglonesDeFacturas.containsKey(r.getIdProductoItem())) {
          facturado =
              (r.getCantidad()
                      .compareTo(renglonesDeFacturas.get(r.getIdProductoItem()).getCantidad())
                  < 1);
        } else {
          return false;
        }
      }
    }
    return facturado;
  }

  @Override
  public Page<FacturaVenta> buscarFacturaVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    return facturaVentaRepository.findAll(
        this.getBuilderVenta(criteria, idUsuarioLoggedIn),
        facturaService.getPageable(
            (criteria.getPagina() == null || criteria.getPagina() < 0) ? 0 : criteria.getPagina(),
            criteria.getOrdenarPor(),
            criteria.getSentido()));
  }

  private BooleanBuilder getBuilderVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getIdSucursal() == null) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_busqueda_sin_sucursal", null, Locale.getDefault()));
    }
    builder.and(
        qFacturaVenta
            .sucursal
            .idSucursal
            .eq(criteria.getIdSucursal())
            .and(qFacturaVenta.eliminada.eq(false)));
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
            qFacturaVenta.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
        builder.and(qFacturaVenta.fecha.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        criteria.setFechaHasta(
            criteria
                .getFechaHasta()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999));
        builder.and(qFacturaVenta.fecha.before(criteria.getFechaHasta()));
      }
    }
    if (criteria.getIdCliente() != null)
      builder.and(qFacturaVenta.cliente.idCliente.eq(criteria.getIdCliente()));
    if (criteria.getTipoComprobante() != null)
      builder.and(qFacturaVenta.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.getIdUsuario() != null)
      builder.and(qFacturaVenta.usuario.idUsuario.eq(criteria.getIdUsuario()));
    if (criteria.getIdViajante() != null)
      builder.and(qFacturaVenta.cliente.viajante.idUsuario.eq(criteria.getIdViajante()));
    if (criteria.getNumSerie() != null && criteria.getNumFactura() != null)
      builder
          .and(qFacturaVenta.numSerie.eq(criteria.getNumSerie()))
          .and(qFacturaVenta.numFactura.eq(criteria.getNumFactura()));
    if (criteria.getNroPedido() != null)
      builder.and(qFacturaVenta.pedido.nroPedido.eq(criteria.getNroPedido()));
    if (criteria.getIdProducto() != null)
      builder.and(qFacturaVenta.renglones.any().idProductoItem.eq(criteria.getIdProducto()));
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    if (!usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLogueado.getRoles().contains(Rol.VENDEDOR)
        && !usuarioLogueado.getRoles().contains(Rol.ENCARGADO)) {
      usuarioLogueado
          .getRoles()
          .forEach(
              rol -> {
                if (rol == Rol.VIAJANTE) {
                  rsPredicate.or(
                      qFacturaVenta.cliente.viajante.idUsuario.eq(usuarioLogueado.getIdUsuario()));
                }
                if (rol == Rol.COMPRADOR) {
                  Cliente clienteRelacionado =
                      clienteService.getClientePorIdUsuario(idUsuarioLoggedIn);
                  if (clienteRelacionado != null) {
                    rsPredicate.or(
                        qFacturaVenta.cliente.idCliente.eq(clienteRelacionado.getIdCliente()));
                  } else {
                    rsPredicate.or(qFacturaVenta.cliente.isNull());
                  }
                }
              });
      builder.and(rsPredicate);
    }
    return builder;
  }

  @Override
  @Transactional
  public List<FacturaVenta> guardar(
      @Valid List<FacturaVenta> facturas, Long idPedido, List<Recibo> recibos) {
    this.calcularValoresFacturasVentaAndActualizarStock(facturas);
    List<FacturaVenta> facturasProcesadas = new ArrayList<>();
    if (idPedido != null) {
      Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(idPedido);
      facturas.forEach(f -> f.setPedido(pedido));
      for (FacturaVenta f : facturas) {
        FacturaVenta facturaGuardada =
            facturaVentaRepository.save((FacturaVenta) this.procesarFacturaVenta(f));
        this.cuentaCorrienteService.asentarEnCuentaCorriente(facturaGuardada, TipoDeOperacion.ALTA);
        facturasProcesadas.add(facturaGuardada);
        if (recibos != null) {
          recibos.forEach(reciboService::guardar);
        }
      }
      List<Factura> facturasParaRelacionarAlPedido = new ArrayList<>(facturasProcesadas);
      pedidoService.actualizarFacturasDelPedido(pedido, facturasParaRelacionarAlPedido);
      facturasProcesadas.forEach(f -> logger.warn("La Factura {} se guardó correctamente.", f));
      pedidoService.actualizarEstadoPedido(pedido);
    } else {
      facturasProcesadas = new ArrayList<>();
      for (FacturaVenta f : facturas) {
        FacturaVenta facturaGuardada;
        facturaGuardada = facturaVentaRepository.save((FacturaVenta) this.procesarFacturaVenta(f));
        this.cuentaCorrienteService.asentarEnCuentaCorriente(facturaGuardada, TipoDeOperacion.ALTA);
        facturasProcesadas.add(facturaGuardada);
        logger.warn("La Factura {} se guardó correctamente.", facturaGuardada);
        if (recibos != null) {
          recibos.forEach(reciboService::guardar);
        }
      }
    }
    return facturasProcesadas;
  }

  public Factura procesarFacturaVenta(FacturaVenta factura) {
    factura.setEliminada(false);
    factura.setFecha(LocalDateTime.now());
    factura.setNumSerie(
        configuracionSucursalService
            .getConfiguracionSucursal(factura.getSucursal())
            .getNroPuntoDeVentaAfip());
    factura.setNumFactura(
        this.calcularNumeroFacturaVenta(
            factura.getTipoComprobante(),
            factura.getNumSerie(),
            factura.getSucursal().getIdSucursal()));
    return facturaService.procesarFactura(factura);
  }

  private void calcularValoresFacturasVentaAndActualizarStock(List<FacturaVenta> facturas) {
    facturas.forEach(
        facturaVenta -> {
          facturaService.calcularValoresFactura(facturaVenta);
          productoService.actualizarStock(
              facturaService.getIdsProductosYCantidades(facturaVenta),
              facturaVenta.getIdSucursal(),
              TipoDeOperacion.ALTA,
              Movimiento.VENTA,
              facturaVenta.getTipoComprobante());
        });
  }

  @Override
  @Transactional
  public FacturaVenta autorizarFacturaVenta(FacturaVenta fv) {
    ComprobanteAFIP comprobante =
        ComprobanteAFIP.builder()
            .idComprobante(fv.getIdFactura())
            .fecha(fv.getFecha())
            .tipoComprobante(fv.getTipoComprobante())
            .cae(fv.getCae())
            .vencimientoCAE(fv.getVencimientoCae())
            .numSerieAfip(fv.getNumSerieAfip())
            .numFacturaAfip(fv.getNumFacturaAfip())
            .sucursal(fv.getSucursal())
            .cliente(fv.getClienteEmbedded())
            .subtotalBruto(fv.getSubTotalBruto())
            .iva105neto(fv.getIva105Neto())
            .iva21neto(fv.getIva21Neto())
            .montoNoGravado(BigDecimal.ZERO)
            .total(fv.getTotal())
            .build();
    afipService.autorizar(comprobante);
    fv.setCae(comprobante.getCae());
    fv.setVencimientoCae(comprobante.getVencimientoCAE());
    fv.setNumSerieAfip(comprobante.getNumSerieAfip());
    fv.setNumFacturaAfip(comprobante.getNumFacturaAfip());
    return fv;
  }

  @Override
  public BigDecimal calcularTotalFacturadoVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    BigDecimal totalFacturado =
        facturaVentaRepository.calcularTotalFacturadoVenta(
            this.getBuilderVenta(criteria, idUsuarioLoggedIn));
    return (totalFacturado != null ? totalFacturado : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularIvaVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
    BigDecimal ivaVenta =
        facturaVentaRepository.calcularIVAVenta(
            this.getBuilderVenta(criteria, idUsuarioLoggedIn), tipoFactura);
    return (ivaVenta != null ? ivaVenta : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularGananciaTotal(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    BigDecimal gananciaTotal =
        facturaVentaRepository.calcularGananciaTotal(
            this.getBuilderVenta(criteria, idUsuarioLoggedIn));
    return (gananciaTotal != null ? gananciaTotal : BigDecimal.ZERO);
  }

  @Override
  public long calcularNumeroFacturaVenta(
      TipoDeComprobante tipoDeComprobante, long serie, long idSucursal) {
    Long numeroFactura =
        facturaVentaRepository.buscarMayorNumFacturaSegunTipo(tipoDeComprobante, serie, idSucursal);
    if (numeroFactura == null) {
      return 1; // No existe ninguna Factura anterior
    } else {
      return 1 + numeroFactura;
    }
  }

  @Override
  public byte[] getReporteFacturaVenta(Factura factura) {
    ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
    InputStream isFileReport =
        classLoader.getResourceAsStream("sic/vista/reportes/FacturaVenta.jasper");
    Map<String, Object> params = new HashMap<>();
    ConfiguracionSucursal configuracionSucursal =
        this.configuracionSucursalService.getConfiguracionSucursal(factura.getSucursal());
    params.put("preImpresa", configuracionSucursal.isUsarFacturaVentaPreImpresa());
    if (factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_B)
        || factura.getTipoComprobante().equals(TipoDeComprobante.PRESUPUESTO)) {
      factura.setSubTotalBruto(factura.getTotal());
      factura.setIva105Neto(BigDecimal.ZERO);
      factura.setIva21Neto(BigDecimal.ZERO);
    }
    params.put("facturaVenta", factura);
    if (factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_A)
        || factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_B)
        || factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_C)) {
      if (factura.getNumSerieAfip() != 0 && factura.getNumFacturaAfip() != 0) {
        params.put("nroSerie", factura.getNumSerieAfip());
        params.put("nroFactura", factura.getNumFacturaAfip());
      } else {
        params.put("nroSerie", null);
        params.put("nroFactura", null);
      }
    } else {
      params.put("nroSerie", factura.getNumSerie());
      params.put("nroFactura", factura.getNumFactura());
    }
    if (factura.getSucursal().getLogo() != null && !factura.getSucursal().getLogo().isEmpty()) {
      try {
        params.put(
            "logo",
            new ImageIcon(ImageIO.read(new URL(factura.getSucursal().getLogo()))).getImage());
      } catch (IOException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(
            messageSource.getMessage("mensaje_sucursal_404_logo", null, Locale.getDefault()), ex);
      }
    }
    List<RenglonFactura> renglones = facturaService.getRenglonesDeLaFactura(factura.getIdFactura());
    JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones);
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params, ds));
    } catch (JRException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
  }

  @Override
  public List<FacturaVenta> dividirFactura(FacturaVenta facturaADividir, int[] indices) {
    FacturaVenta facturaSinIVA = new FacturaVenta();
    facturaSinIVA.setCliente(facturaADividir.getCliente());
    facturaSinIVA.setClienteEmbedded(facturaADividir.getClienteEmbedded());
    facturaSinIVA.setUsuario(facturaADividir.getUsuario());
    facturaSinIVA.setPedido(facturaADividir.getPedido());
    facturaSinIVA.setDescuentoPorcentaje(facturaADividir.getDescuentoPorcentaje());
    facturaSinIVA.setRecargoPorcentaje(facturaADividir.getRecargoPorcentaje());
    facturaSinIVA.setFecha(facturaADividir.getFecha());
    facturaSinIVA.setTipoComprobante(TipoDeComprobante.FACTURA_X);
    facturaSinIVA.setFechaVencimiento(facturaADividir.getFechaVencimiento());
    facturaSinIVA.setTransportista(facturaADividir.getTransportista());
    facturaSinIVA.setObservaciones(facturaADividir.getObservaciones());
    facturaSinIVA.setSucursal(facturaADividir.getSucursal());
    facturaSinIVA.setEliminada(facturaADividir.isEliminada());
    FacturaVenta facturaConIVA = new FacturaVenta();
    facturaConIVA.setCliente(facturaADividir.getCliente());
    facturaConIVA.setClienteEmbedded(facturaADividir.getClienteEmbedded());
    facturaConIVA.setUsuario(facturaADividir.getUsuario());
    facturaConIVA.setPedido(facturaADividir.getPedido());
    facturaConIVA.setTipoComprobante(facturaADividir.getTipoComprobante());
    facturaConIVA.setFecha(facturaADividir.getFecha());
    facturaConIVA.setFechaVencimiento(facturaADividir.getFechaVencimiento());
    facturaConIVA.setTransportista(facturaADividir.getTransportista());
    facturaConIVA.setObservaciones(facturaADividir.getObservaciones());
    facturaConIVA.setSucursal(facturaADividir.getSucursal());
    facturaConIVA.setEliminada(facturaADividir.isEliminada());
    facturaConIVA.setDescuentoPorcentaje(facturaADividir.getDescuentoPorcentaje());
    facturaConIVA.setRecargoPorcentaje(facturaADividir.getRecargoPorcentaje());
    List<FacturaVenta> facturas = new ArrayList<>();
    this.agregarRenglonesAFacturaSinIVA(facturaSinIVA, indices, facturaADividir.getRenglones());
    this.agregarRenglonesAFacturaConIVA(facturaConIVA, indices, facturaADividir.getRenglones());
    if (!facturaSinIVA.getRenglones().isEmpty()) {
      facturas.add(facturaSinIVA);
    }
    facturas.add(facturaConIVA);
    return facturas;
  }

  @Override
  public boolean existeFacturaVentaAnteriorSinAutorizar(ComprobanteAFIP comprobante) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qFacturaVenta
            .idFactura
            .lt(comprobante.getIdComprobante())
            .and(qFacturaVenta.eliminada.eq(false))
            .and(qFacturaVenta.sucursal.idSucursal.eq(comprobante.getSucursal().getIdSucursal()))
            .and(qFacturaVenta.tipoComprobante.eq(comprobante.getTipoComprobante())));
    Page<FacturaVenta> facturaAnterior =
        facturaVentaRepository.findAll(
            builder, PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "fecha")));
    return facturaAnterior.getContent().get(0).getCae() == 0L;
  }

  @Override
  public void enviarFacturaVentaPorEmail(long idFactura) {
    Factura factura = facturaService.getFacturaNoEliminadaPorId(idFactura);
    List<TipoDeComprobante> tiposPermitidosParaEnviar =
        Arrays.asList(
            TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B, TipoDeComprobante.FACTURA_C);
    if (factura instanceof FacturaVenta) {
      if (tiposPermitidosParaEnviar.contains(factura.getTipoComprobante())
          && factura.getCae() == 0L) {
        throw new BusinessServiceException(
            messageSource.getMessage("mensaje_correo_factura_sin_cae", null, Locale.getDefault()));
      }
      FacturaVenta facturaVenta = (FacturaVenta) factura;
      if (facturaVenta.getCliente().getEmail() == null
          || facturaVenta.getCliente().getEmail().isEmpty()) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_correo_cliente_sin_email", null, Locale.getDefault()));
      }
      String bodyEmail = "";
      if (facturaVenta.getPedido() != null) {
        if (facturaVenta.getPedido().getTipoDeEnvio().equals(TipoDeEnvio.RETIRO_EN_SUCURSAL)) {
          bodyEmail =
              messageSource.getMessage(
                  "mensaje_correo_factura_retiro_sucursal",
                  new Object[] {
                    facturaVenta.getPedido().getSucursal().getNombre(),
                    "(" + facturaVenta.getPedido().getDetalleEnvio().toString() + ")"
                  },
                  Locale.getDefault());
        } else {
          bodyEmail =
              messageSource.getMessage(
                  "mensaje_correo_factura_direccion_envio",
                  new Object[] {facturaVenta.getPedido().getDetalleEnvio().toString()},
                  Locale.getDefault());
        }
      } else {
        bodyEmail =
            messageSource.getMessage(
                "mensaje_correo_factura_sin_pedido", null, Locale.getDefault());
      }
      correoElectronicoService.enviarEmail(
          facturaVenta.getCliente().getEmail(),
          "",
          "Su Factura de Compra",
          bodyEmail,
          this.getReporteFacturaVenta(factura),
          "Reporte");
      logger.warn(
          "El mail de la factura serie {} nro {} se envió.",
          factura.getNumSerie(),
          factura.getNumFactura());
    }
  }

  @Override
  public void agregarRenglonesAFacturaSinIVA(
      FacturaVenta facturaSinIVA, int[] indices, List<RenglonFactura> renglones) {
    List<RenglonFactura> renglonesSinIVA = new ArrayList<>();
    BigDecimal cantidadProductosRenglonFacturaSinIVA = BigDecimal.ZERO;
    int renglonMarcado = 0;
    int numeroDeRenglon = 0;
    for (RenglonFactura renglon : renglones) {
      if (numeroDeRenglon == indices[renglonMarcado]) {
        BigDecimal cantidad = renglon.getCantidad();
        if (cantidad.compareTo(BigDecimal.ONE) >= 0) {
          if ((cantidad.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0)
              || cantidad.remainder(new BigDecimal("2")).compareTo(BigDecimal.ZERO) == 0) {
            cantidadProductosRenglonFacturaSinIVA =
                cantidad.divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP);
          } else if (cantidad.remainder(new BigDecimal(2)).compareTo(BigDecimal.ZERO) != 0) {
            cantidadProductosRenglonFacturaSinIVA =
                cantidad.subtract(
                    cantidad
                        .divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP)
                        .setScale(0, RoundingMode.CEILING));
          }
        } else {
          cantidadProductosRenglonFacturaSinIVA = BigDecimal.ZERO;
        }
        RenglonFactura nuevoRenglonSinIVA =
             facturaService.calcularRenglon(
                TipoDeComprobante.FACTURA_X,
                Movimiento.VENTA,
                cantidadProductosRenglonFacturaSinIVA,
                renglon.getIdProductoItem(),
                null);
        if (nuevoRenglonSinIVA.getCantidad().compareTo(BigDecimal.ZERO) != 0) {
          renglonesSinIVA.add(nuevoRenglonSinIVA);
        }
        numeroDeRenglon++;
        renglonMarcado++;
        if (renglonMarcado == indices.length) {
          break;
        }
      } else {
        numeroDeRenglon++;
      }
    }
    facturaSinIVA.setRenglones(renglonesSinIVA);
  }

  @Override
  public void agregarRenglonesAFacturaConIVA(
      FacturaVenta facturaConIVA, int[] indices, List<RenglonFactura> renglones) {
    List<RenglonFactura> renglonesConIVA = new ArrayList<>();
    BigDecimal cantidadProductosRenglonFacturaConIVA = BigDecimal.ZERO;
    int renglonMarcado = 0;
    int numeroDeRenglon = 0;
    for (RenglonFactura renglon : renglones) {
      if (renglonMarcado < indices.length) {
        if (numeroDeRenglon == indices[renglonMarcado]) {
          BigDecimal cantidad = renglon.getCantidad();
          if (cantidad.compareTo(BigDecimal.ONE) < 0 || cantidad.compareTo(BigDecimal.ONE) == 0) {
            cantidadProductosRenglonFacturaConIVA = cantidad;
          } else if ((cantidad.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0)
              || renglon.getCantidad().remainder(new BigDecimal(2)).compareTo(BigDecimal.ZERO)
                  == 0) {
            cantidadProductosRenglonFacturaConIVA =
                renglon.getCantidad().divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP);
          } else if (renglon.getCantidad().remainder(new BigDecimal("2")).compareTo(BigDecimal.ZERO)
              != 0) {
            cantidadProductosRenglonFacturaConIVA =
                renglon
                    .getCantidad()
                    .divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP)
                    .setScale(0, RoundingMode.CEILING);
          }
          renglonesConIVA.add(
              facturaService.calcularRenglon(
                  facturaConIVA.getTipoComprobante(),
                  Movimiento.VENTA,
                  cantidadProductosRenglonFacturaConIVA,
                  renglon.getIdProductoItem(),
                  null));
          renglonMarcado++;
          numeroDeRenglon++;
        } else {
          numeroDeRenglon++;
          renglonesConIVA.add(
              facturaService.calcularRenglon(
                  facturaConIVA.getTipoComprobante(),
                  Movimiento.VENTA,
                  renglon.getCantidad(),
                  renglon.getIdProductoItem(),
                  null));
        }
      } else {
        numeroDeRenglon++;
        renglonesConIVA.add(
            facturaService.calcularRenglon(
                facturaConIVA.getTipoComprobante(),
                Movimiento.VENTA,
                renglon.getCantidad(),
                renglon.getIdProductoItem(),
                null));
      }
    }
    facturaConIVA.setRenglones(renglonesConIVA);
  }
}
