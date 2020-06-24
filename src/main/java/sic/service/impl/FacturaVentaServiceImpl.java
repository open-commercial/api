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
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.dto.NuevaFacturaVentaDTO;
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.repository.FacturaVentaRepository;
import sic.service.*;
import sic.util.CalculosComprobante;
import sic.util.CustomValidator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
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
  private final ITransportistaService transportistaService;
  private final ISucursalService sucursalService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;
  private static final BigDecimal IVA_21 = new BigDecimal("21");
  private static final BigDecimal IVA_105 = new BigDecimal("10.5");
  private static final String NRO_SERIE = "nroSerie";
  private static final String NRO_FACTURA = "nroFactura";
  private final CustomValidator customValidator;

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
      ITransportistaService transportistaService,
      ISucursalService sucursalService,
      MessageSource messageSource,
      CustomValidator customValidator) {
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
    this.transportistaService = transportistaService;
    this.sucursalService = sucursalService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public FacturaVenta construirFacuraVenta(
      NuevaFacturaVentaDTO nuevaFacturaVentaDTO, Long idPedido, Long idUsuario) {
    FacturaVenta fv = new FacturaVenta();
    Sucursal sucursal;
    Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(idPedido);
    fv.setPedido(pedido);
    sucursal = pedido.getSucursal();
    fv.setSucursal(sucursal);
    fv.setTipoComprobante(nuevaFacturaVentaDTO.getTipoDeComprobante());
    fv.setDescuentoPorcentaje(
        nuevaFacturaVentaDTO.getDescuentoPorcentaje() != null
            ? nuevaFacturaVentaDTO.getDescuentoPorcentaje()
            : BigDecimal.ZERO);
    fv.setRecargoPorcentaje(
        nuevaFacturaVentaDTO.getRecargoPorcentaje() != null
            ? nuevaFacturaVentaDTO.getRecargoPorcentaje()
            : BigDecimal.ZERO);
    Cliente cliente =
        clienteService.getClienteNoEliminadoPorId(nuevaFacturaVentaDTO.getIdCliente());
    if (cliente.getUbicacionFacturacion() == null
        && (fv.getTipoComprobante() == TipoDeComprobante.FACTURA_A
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_C)) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault()));
    }
    fv.setCliente(cliente);
    fv.setClienteEmbedded(clienteService.crearClienteEmbedded(cliente));
    if (nuevaFacturaVentaDTO.getIdTransportista() != null) {
      fv.setTransportista(
          transportistaService.getTransportistaNoEliminadoPorId(
              nuevaFacturaVentaDTO.getIdTransportista()));
    }
    fv.setFecha(LocalDateTime.now());
    fv.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    List<RenglonPedido> renglonesPedido =
        pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(idPedido);
    List<NuevoRenglonFacturaDTO> nuevosRenglonesDeFactura = new ArrayList<>();
    if (nuevaFacturaVentaDTO.getRenglonMarcado() != null) {
      if (nuevaFacturaVentaDTO.getRenglonMarcado().length != renglonesPedido.size()) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_factura_renglones_marcados_incorrectos", null, Locale.getDefault()));
      }
      for (int indice = 0;
          indice < Objects.requireNonNull(nuevaFacturaVentaDTO.getRenglonMarcado()).length;
          indice++) {
        int finalIndice = indice;
        renglonesPedido.forEach(
            renglonPedido -> {
              NuevoRenglonFacturaDTO nuevoRenglonFactura =
                  NuevoRenglonFacturaDTO.builder()
                      .idProducto(renglonPedido.getIdProductoItem())
                      .cantidad(renglonPedido.getCantidad())
                      .renglonMarcado(nuevaFacturaVentaDTO.getRenglonMarcado()[finalIndice])
                      .build();
              nuevosRenglonesDeFactura.add(nuevoRenglonFactura);
            });
      }
    } else {
      renglonesPedido.forEach(
          renglonPedido -> {
            NuevoRenglonFacturaDTO nuevoRenglonFactura =
                NuevoRenglonFacturaDTO.builder()
                    .idProducto(renglonPedido.getIdProductoItem())
                    .cantidad(renglonPedido.getCantidad())
                    .build();
            nuevosRenglonesDeFactura.add(nuevoRenglonFactura);
          });
    }
    fv.setRenglones(
        facturaService.calcularRenglones(
            nuevaFacturaVentaDTO.getTipoDeComprobante(),
            Movimiento.VENTA,
            nuevosRenglonesDeFactura));
    fv.setObservaciones(
        nuevaFacturaVentaDTO.getObservaciones() != null
            ? nuevaFacturaVentaDTO.getObservaciones()
            : "");
    return fv;
  }

  @Override
  public TipoDeComprobante[] getTiposDeComprobanteVenta(
      Long idSucursal, Long idCliente, Long idUsuario) {
    List<Rol> rolesDeUsuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario).getRoles();
    if (rolesDeUsuario.contains(Rol.ADMINISTRADOR)
        || rolesDeUsuario.contains(Rol.ENCARGADO)
        || rolesDeUsuario.contains(Rol.VENDEDOR)) {
      Sucursal sucursal = sucursalService.getSucursalPorId(idSucursal);
      Cliente cliente = clienteService.getClienteNoEliminadoPorId(idCliente);
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
    } else if (rolesDeUsuario.contains(Rol.VIAJANTE) || rolesDeUsuario.contains(Rol.COMPRADOR)) {
      return new TipoDeComprobante[] {TipoDeComprobante.PEDIDO};
    }
    return new TipoDeComprobante[0];
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
    Map<Long, BigDecimal> renglonesDeFacturas =
        pedidoService.getRenglonesFacturadosDelPedido(idPedido);
    if (renglonesDeFacturas != null) {
      renglonesPedido.forEach(
          r -> {
            if (renglonesDeFacturas.containsKey(r.getIdProductoItem())) {
              if (r.getCantidad().compareTo(renglonesDeFacturas.get(r.getIdProductoItem())) > 0) {
                NuevoRenglonFacturaDTO nuevoRenglonFacturaDTO =
                    NuevoRenglonFacturaDTO.builder()
                        .cantidad(
                            r.getCantidad()
                                .subtract(renglonesDeFacturas.get(r.getIdProductoItem())))
                        .idProducto(r.getIdProductoItem())
                        .renglonMarcado(
                            facturaService.marcarRenglonParaAplicarBonificacion(
                                r.getIdProductoItem(), r.getCantidad()))
                        .build();
                renglonesRestantes.add(
                    facturaService.calcularRenglon(
                        tipoDeComprobante, Movimiento.VENTA, nuevoRenglonFacturaDTO));
              }
            } else {
              NuevoRenglonFacturaDTO nuevoRenglonFacturaDTO =
                  NuevoRenglonFacturaDTO.builder()
                      .cantidad(r.getCantidad())
                      .idProducto(r.getIdProductoItem())
                      .renglonMarcado(
                          facturaService.marcarRenglonParaAplicarBonificacion(
                              r.getIdProductoItem(), r.getCantidad()))
                      .build();
              renglonesRestantes.add(
                  facturaService.calcularRenglon(
                      tipoDeComprobante, Movimiento.VENTA, nuevoRenglonFacturaDTO));
            }
          });
    } else {
      renglonesPedido.forEach(
          r -> {
            NuevoRenglonFacturaDTO nuevoRenglonFacturaDTO =
                NuevoRenglonFacturaDTO.builder()
                    .cantidad(r.getCantidad())
                    .idProducto(r.getIdProductoItem())
                    .renglonMarcado(
                        facturaService.marcarRenglonParaAplicarBonificacion(
                            r.getIdProductoItem(), r.getCantidad()))
                    .build();
            renglonesRestantes.add(
                facturaService.calcularRenglon(
                    tipoDeComprobante, Movimiento.VENTA, nuevoRenglonFacturaDTO));
          });
    }
    return renglonesRestantes;
  }

  @Override
  public Page<FacturaVenta> buscarFacturaVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    return facturaVentaRepository.findAll(
        this.getBuilderVenta(criteria),
        facturaService.getPageable(
            (criteria.getPagina() == null || criteria.getPagina() < 0) ? 0 : criteria.getPagina(),
            criteria.getOrdenarPor(),
            criteria.getSentido()));
  }

  @Override
  public BooleanBuilder getBuilderVenta(BusquedaFacturaVentaCriteria criteria) {
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
    return builder;
  }

  @Override
  @Transactional
  public List<FacturaVenta> guardar(
      List<FacturaVenta> facturas, Long idPedido, List<Recibo> recibos) {
    facturas.forEach(customValidator::validar);
    List<FacturaVenta> facturasProcesadas = new ArrayList<>();
    if (idPedido != null) {
      Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(idPedido);
      pedido.setEstado(EstadoPedido.CERRADO);
      this.calcularValoresFacturasVentaAndActualizarStock(facturas);
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
    } else {
      this.calcularValoresFacturasVentaAndActualizarStock(facturas);
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
    facturas.forEach(facturaService::calcularValoresFactura);
  }

  @Override
  @Transactional
  public FacturaVenta autorizarFacturaVenta(FacturaVenta fv) {
    List<TipoDeComprobante> tiposAutorizables =
        Arrays.asList(
            TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B, TipoDeComprobante.FACTURA_C);
    if (!tiposAutorizables.contains(fv.getTipoComprobante())) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_comprobanteAFIP_invalido", null, Locale.getDefault()));
    }
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
        facturaVentaRepository.calcularTotalFacturadoVenta(this.getBuilderVenta(criteria));
    return (totalFacturado != null ? totalFacturado : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularIvaVenta(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
    BigDecimal ivaVenta =
        facturaVentaRepository.calcularIVAVenta(this.getBuilderVenta(criteria), tipoFactura);
    return (ivaVenta != null ? ivaVenta : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularGananciaTotal(
      BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    BigDecimal gananciaTotal =
        facturaVentaRepository.calcularGananciaTotal(this.getBuilderVenta(criteria));
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
        params.put(NRO_SERIE, factura.getNumSerieAfip());
        params.put(NRO_FACTURA, factura.getNumFacturaAfip());
      } else {
        params.put(NRO_SERIE, null);
        params.put(NRO_FACTURA, null);
      }
    } else {
      params.put(NRO_SERIE, factura.getNumSerie());
      params.put(NRO_FACTURA, factura.getNumFactura());
    }
    if (factura.getSucursal().getLogo() != null && !factura.getSucursal().getLogo().isEmpty()) {
      try {
        params.put(
            "logo",
            new ImageIcon(ImageIO.read(new URL(factura.getSucursal().getLogo()))).getImage());
      } catch (IOException ex) {
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
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
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
            builder, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "fecha")));
    if (facturaAnterior.getTotalElements() > 0L) {
      return facturaAnterior.getContent().get(0).getCae() == 0L;
    } else {
      return false;
    }
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
          "Reporte.pdf");
      logger.warn(
          "El mail de la factura serie {} nro {} se envió.",
          factura.getNumSerie(),
          factura.getNumFactura());
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
    FacturaVenta facturaConIVA = new FacturaVenta();
    facturaConIVA.setCliente(facturaADividir.getCliente());
    facturaConIVA.setClienteEmbedded(facturaADividir.getClienteEmbedded());
    facturaConIVA.setUsuario(facturaADividir.getUsuario());
    facturaConIVA.setPedido(facturaADividir.getPedido());
    facturaConIVA.setTipoComprobante(facturaADividir.getTipoComprobante());
    facturaConIVA.setDescuentoPorcentaje(facturaADividir.getDescuentoPorcentaje());
    facturaConIVA.setRecargoPorcentaje(facturaADividir.getRecargoPorcentaje());
    List<FacturaVenta> facturas = new ArrayList<>();
    this.agregarRenglonesAFacturaSinIVA(facturaSinIVA, indices, facturaADividir.getRenglones());
    this.agregarRenglonesAFacturaConIVA(facturaConIVA, indices, facturaADividir.getRenglones());
    if (!facturaSinIVA.getRenglones().isEmpty()) {
      this.procesarFacturaSinIVA(facturaADividir, facturaSinIVA);
      facturas.add(facturaSinIVA);
    }
    this.procesarFacturaConIVA(facturaADividir, facturaConIVA);
    facturas.add(facturaConIVA);
    return facturas;
  }

  private void procesarFacturaSinIVA(FacturaVenta facturaADividir, FacturaVenta facturaSinIVA) {
    int size = facturaSinIVA.getRenglones().size();
    BigDecimal[] importes = new BigDecimal[size];
    BigDecimal[] cantidades = new BigDecimal[size];
    BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
    BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
    int indice = 0;
    List<RenglonFactura> listRenglonesSinIVA = new ArrayList<>(facturaSinIVA.getRenglones());
    facturaSinIVA.setFecha(facturaADividir.getFecha());
    facturaSinIVA.setTipoComprobante(TipoDeComprobante.FACTURA_X);
    facturaSinIVA.setFechaVencimiento(facturaADividir.getFechaVencimiento());
    facturaSinIVA.setTransportista(facturaADividir.getTransportista());
    facturaSinIVA.setRenglones(listRenglonesSinIVA);
    for (RenglonFactura renglon : facturaSinIVA.getRenglones()) {
      importes[indice] = renglon.getImporte();
      cantidades[indice] = renglon.getCantidad();
      ivaPorcentajeRenglones[indice] = renglon.getIvaPorcentaje();
      ivaNetoRenglones[indice] = renglon.getIvaNeto();
      indice++;
    }
    facturaSinIVA.setSubTotal(CalculosComprobante.calcularSubTotal(importes));
    facturaSinIVA.setDescuentoNeto(
        CalculosComprobante.calcularProporcion(
            facturaSinIVA.getSubTotal(), facturaSinIVA.getDescuentoPorcentaje()));
    facturaSinIVA.setRecargoNeto(
        CalculosComprobante.calcularProporcion(
            facturaSinIVA.getSubTotal(), facturaSinIVA.getRecargoPorcentaje()));
    facturaSinIVA.setIva105Neto(
        facturaService.calcularIvaNetoFactura(
            facturaSinIVA.getTipoComprobante(),
            cantidades,
            ivaPorcentajeRenglones,
            ivaNetoRenglones,
            IVA_105,
            facturaADividir.getDescuentoPorcentaje(),
            facturaADividir.getRecargoPorcentaje()));
    facturaSinIVA.setIva21Neto(
        facturaService.calcularIvaNetoFactura(
            facturaSinIVA.getTipoComprobante(),
            cantidades,
            ivaPorcentajeRenglones,
            ivaNetoRenglones,
            IVA_21,
            facturaADividir.getDescuentoPorcentaje(),
            facturaADividir.getRecargoPorcentaje()));
    facturaSinIVA.setSubTotalBruto(
        CalculosComprobante.calcularSubTotalBruto(
            (facturaSinIVA.getTipoComprobante() == TipoDeComprobante.FACTURA_B
                || facturaSinIVA.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO),
            facturaSinIVA.getSubTotal(),
            facturaSinIVA.getRecargoNeto(),
            facturaSinIVA.getDescuentoNeto(),
            facturaSinIVA.getIva105Neto(),
            facturaSinIVA.getIva21Neto()));
    facturaSinIVA.setTotal(
        CalculosComprobante.calcularTotal(
            facturaSinIVA.getSubTotalBruto(),
            facturaSinIVA.getIva105Neto(),
            facturaSinIVA.getIva21Neto()));
    facturaSinIVA.setObservaciones(facturaADividir.getObservaciones());
    facturaSinIVA.setSucursal(facturaADividir.getSucursal());
    facturaSinIVA.setEliminada(facturaADividir.isEliminada());
  }

  private void procesarFacturaConIVA(FacturaVenta facturaADividir, FacturaVenta facturaConIVA) {
    int size = facturaConIVA.getRenglones().size();
    BigDecimal[] importes = new BigDecimal[size];
    BigDecimal[] cantidades = new BigDecimal[size];
    BigDecimal[] ivaPorcentajeRenglones = new BigDecimal[size];
    BigDecimal[] ivaNetoRenglones = new BigDecimal[size];
    int indice = 0;
    List<RenglonFactura> listRenglonesConIVA = new ArrayList<>(facturaConIVA.getRenglones());
    facturaConIVA.setFecha(facturaADividir.getFecha());
    facturaConIVA.setTipoComprobante(facturaADividir.getTipoComprobante());
    facturaConIVA.setFechaVencimiento(facturaADividir.getFechaVencimiento());
    facturaConIVA.setTransportista(facturaADividir.getTransportista());
    facturaConIVA.setRenglones(listRenglonesConIVA);
    for (RenglonFactura renglon : facturaConIVA.getRenglones()) {
      importes[indice] = renglon.getImporte();
      cantidades[indice] = renglon.getCantidad();
      ivaPorcentajeRenglones[indice] = renglon.getIvaPorcentaje();
      ivaNetoRenglones[indice] = renglon.getIvaNeto();
      indice++;
    }
    facturaConIVA.setSubTotal(CalculosComprobante.calcularSubTotal(importes));
    facturaConIVA.setDescuentoNeto(
        CalculosComprobante.calcularProporcion(
            facturaConIVA.getSubTotal(), facturaConIVA.getDescuentoPorcentaje()));
    facturaConIVA.setRecargoNeto(
        CalculosComprobante.calcularProporcion(
            facturaConIVA.getSubTotal(), facturaConIVA.getRecargoPorcentaje()));
    facturaConIVA.setIva105Neto(
        facturaService.calcularIvaNetoFactura(
            facturaConIVA.getTipoComprobante(),
            cantidades,
            ivaPorcentajeRenglones,
            ivaNetoRenglones,
            IVA_105,
            facturaADividir.getDescuentoPorcentaje(),
            facturaADividir.getRecargoPorcentaje()));
    facturaConIVA.setIva21Neto(
        facturaService.calcularIvaNetoFactura(
            facturaConIVA.getTipoComprobante(),
            cantidades,
            ivaPorcentajeRenglones,
            ivaNetoRenglones,
            IVA_21,
            facturaADividir.getDescuentoPorcentaje(),
            facturaADividir.getRecargoPorcentaje()));
    facturaConIVA.setSubTotalBruto(
        CalculosComprobante.calcularSubTotalBruto(
            (facturaConIVA.getTipoComprobante() == TipoDeComprobante.FACTURA_B
                || facturaConIVA.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO),
            facturaConIVA.getSubTotal(),
            facturaConIVA.getRecargoNeto(),
            facturaConIVA.getDescuentoNeto(),
            facturaConIVA.getIva105Neto(),
            facturaConIVA.getIva21Neto()));
    facturaConIVA.setTotal(
        CalculosComprobante.calcularTotal(
            facturaConIVA.getSubTotalBruto(),
            facturaConIVA.getIva105Neto(),
            facturaConIVA.getIva21Neto()));
    facturaConIVA.setObservaciones(facturaADividir.getObservaciones());
    facturaConIVA.setSucursal(facturaADividir.getSucursal());
    facturaConIVA.setEliminada(facturaADividir.isEliminada());
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
        if (cantidadProductosRenglonFacturaSinIVA.compareTo(BigDecimal.ZERO) > 0) {
          RenglonFactura nuevoRenglonSinIVA =
              facturaService.calcularRenglon(
                  TipoDeComprobante.FACTURA_X,
                  Movimiento.VENTA,
                  NuevoRenglonFacturaDTO.builder()
                      .cantidad(cantidadProductosRenglonFacturaSinIVA)
                      .idProducto(renglon.getIdProductoItem())
                      .renglonMarcado(
                          facturaService.marcarRenglonParaAplicarBonificacion(
                              renglon.getIdProductoItem(), cantidad))
                      .build());
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
                  NuevoRenglonFacturaDTO.builder()
                      .cantidad(cantidadProductosRenglonFacturaConIVA)
                      .idProducto(renglon.getIdProductoItem())
                      .renglonMarcado(
                          facturaService.marcarRenglonParaAplicarBonificacion(
                              renglon.getIdProductoItem(), cantidad))
                      .build()));
          renglonMarcado++;
          numeroDeRenglon++;
        } else {
          numeroDeRenglon++;
          renglonesConIVA.add(
              facturaService.calcularRenglon(
                  facturaConIVA.getTipoComprobante(),
                  Movimiento.VENTA,
                  NuevoRenglonFacturaDTO.builder()
                      .cantidad(renglon.getCantidad())
                      .idProducto(renglon.getIdProductoItem())
                      .renglonMarcado(
                          facturaService.marcarRenglonParaAplicarBonificacion(
                              renglon.getIdProductoItem(), renglon.getCantidad()))
                      .build()));
        }
      } else {
        numeroDeRenglon++;
        renglonesConIVA.add(
            facturaService.calcularRenglon(
                facturaConIVA.getTipoComprobante(),
                Movimiento.VENTA,
                NuevoRenglonFacturaDTO.builder()
                    .cantidad(renglon.getCantidad())
                    .idProducto(renglon.getIdProductoItem())
                    .renglonMarcado(
                        facturaService.marcarRenglonParaAplicarBonificacion(
                            renglon.getIdProductoItem(), renglon.getCantidad()))
                    .build()));
      }
    }
    facturaConIVA.setRenglones(renglonesConIVA);
  }
}
