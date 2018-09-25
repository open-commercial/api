package sic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.repository.NotaCreditoRepository;
import sic.repository.NotaDebitoRepository;
import sic.service.*;
import sic.repository.NotaRepository;
import sic.util.FormatterFechaHora;

@Service
public class NotaServiceImpl implements INotaService {

  private final NotaRepository notaRepository;
  private final NotaCreditoRepository notaCreditoRepository;
  private final NotaDebitoRepository notaDebitoRepository;
  private final IFacturaService facturaService;
  private final IClienteService clienteService;
  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;
  private final IProductoService productoService;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IRenglonCuentaCorrienteService renglonCuentaCorrienteService;
  private final IReciboService reciboService;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final IAfipService afipService;
  private static final BigDecimal IVA_21 = new BigDecimal("21");
  private static final BigDecimal IVA_105 = new BigDecimal("10.5");
  private static final BigDecimal CIEN = new BigDecimal("100");
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  @Lazy
  public NotaServiceImpl(
      NotaRepository notaRepository,
      NotaCreditoRepository notaCreditoRepository,
      NotaDebitoRepository notaDebitoRepository,
      IFacturaService facturaService,
      IClienteService clienteService,
      IUsuarioService usuarioService,
      IProductoService productoService,
      IEmpresaService empresaService,
      ICuentaCorrienteService cuentaCorrienteService,
      IRenglonCuentaCorrienteService renglonCuentaCorrienteService,
      IReciboService reciboService,
      IConfiguracionDelSistemaService cds,
      IAfipService afipService) {
    this.notaRepository = notaRepository;
    this.notaCreditoRepository = notaCreditoRepository;
    this.notaDebitoRepository = notaDebitoRepository;
    this.facturaService = facturaService;
    this.clienteService = clienteService;
    this.usuarioService = usuarioService;
    this.empresaService = empresaService;
    this.productoService = productoService;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.renglonCuentaCorrienteService = renglonCuentaCorrienteService;
    this.reciboService = reciboService;
    this.configuracionDelSistemaService = cds;
    this.afipService = afipService;
  }

  @Override
  public Nota getNotaPorId(Long idNota) {
    return this.notaRepository.findById(idNota);
  }

  @Override
  public Page<Nota> buscarNotas(BusquedaNotaCriteria busquedaNotaCriteria, long idUsuarioLoggedIn) {
    // Fecha de Nota
    if (busquedaNotaCriteria.isBuscaPorFecha()
        && (busquedaNotaCriteria.getFechaDesde() == null
            || busquedaNotaCriteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_fechas_busqueda_invalidas"));
    }
    if (busquedaNotaCriteria.isBuscaPorFecha()) {
      Calendar cal = new GregorianCalendar();
      cal.setTime(busquedaNotaCriteria.getFechaDesde());
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      busquedaNotaCriteria.setFechaDesde(cal.getTime());
      cal.setTime(busquedaNotaCriteria.getFechaHasta());
      cal.set(Calendar.HOUR_OF_DAY, 23);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 59);
      busquedaNotaCriteria.setFechaHasta(cal.getTime());
    }
    return notaRepository.findAll(
        this.getBuilderNota(busquedaNotaCriteria, idUsuarioLoggedIn),
        busquedaNotaCriteria.getPageable());
  }

  private BooleanBuilder getBuilderNota(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn) {
    QNota qNota = QNota.nota;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qNota.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qNota.eliminada.eq(false)));
    if (criteria.getMovimiento() == Movimiento.VENTA)
      builder.and(qNota.movimiento.eq(Movimiento.VENTA));
    if (criteria.getMovimiento() == Movimiento.COMPRA)
      builder.and(qNota.movimiento.eq(Movimiento.COMPRA));
    // Fecha
    if (criteria.isBuscaPorFecha()) {
      FormatterFechaHora formateadorFecha =
          new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
      DateExpression<Date> fDesde =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaDesde()));
      DateExpression<Date> fHasta =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaHasta()));
      builder.and(qNota.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscaUsuario())
      builder.and(qNota.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.isBuscaCliente())
      builder.and(qNota.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.isBuscaProveedor())
      builder.and(qNota.proveedor.id_Proveedor.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qNota.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaPorNumeroNota())
      builder
          .and(qNota.serie.eq(criteria.getNumSerie()))
          .and(qNota.nroNota.eq(criteria.getNumNota()));
    Usuario usuarioLogueado = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    if (!usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLogueado.getRoles().contains(Rol.VENDEDOR)
        && !usuarioLogueado.getRoles().contains(Rol.ENCARGADO)) {
      for (Rol rol : usuarioLogueado.getRoles()) {
        switch (rol) {
          case VIAJANTE:
            rsPredicate.or(qNota.cliente.viajante.eq(usuarioLogueado));
            break;
          case COMPRADOR:
            Cliente clienteRelacionado =
                clienteService.getClientePorIdUsuarioYidEmpresa(
                    idUsuarioLoggedIn, criteria.getIdEmpresa());
            if (clienteRelacionado != null) {
              rsPredicate.or(qNota.cliente.eq(clienteRelacionado));
            } else {
              rsPredicate.or(qNota.cliente.isNull());
            }
            break;
        }
      }
      builder.and(rsPredicate);
    }
    return builder;
  }

  private BooleanBuilder getBuilderNotaCredito(
      BusquedaNotaCriteria criteria, long idUsuarioLoggedIn) {
    QNotaCredito qNotaCredito = QNotaCredito.notaCredito;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qNotaCredito
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qNotaCredito.eliminada.eq(false)));
    if (criteria.getMovimiento() == Movimiento.VENTA)
      builder.and(qNotaCredito.movimiento.eq(Movimiento.VENTA));
    if (criteria.getMovimiento() == Movimiento.COMPRA)
      builder.and(qNotaCredito.movimiento.eq(Movimiento.COMPRA));
    // Fecha
    if (criteria.isBuscaPorFecha()) {
      FormatterFechaHora formateadorFecha =
          new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
      DateExpression<Date> fDesde =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaDesde()));
      DateExpression<Date> fHasta =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaHasta()));
      builder.and(qNotaCredito.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscaUsuario())
      builder.and(qNotaCredito.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.isBuscaCliente())
      builder.and(qNotaCredito.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.isBuscaProveedor())
      builder.and(qNotaCredito.proveedor.id_Proveedor.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qNotaCredito.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaPorNumeroNota())
      builder
          .and(qNotaCredito.serie.eq(criteria.getNumSerie()))
          .and(qNotaCredito.nroNota.eq(criteria.getNumNota()));
    Usuario usuarioLogueado = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    if (!usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLogueado.getRoles().contains(Rol.VENDEDOR)
        && !usuarioLogueado.getRoles().contains(Rol.ENCARGADO)) {
      for (Rol rol : usuarioLogueado.getRoles()) {
        switch (rol) {
          case VIAJANTE:
            rsPredicate.or(qNotaCredito.cliente.viajante.eq(usuarioLogueado));
            break;
          case COMPRADOR:
            Cliente clienteRelacionado =
                clienteService.getClientePorIdUsuarioYidEmpresa(
                    idUsuarioLoggedIn, criteria.getIdEmpresa());
            if (clienteRelacionado != null) {
              rsPredicate.or(qNotaCredito.cliente.eq(clienteRelacionado));
            } else {
              rsPredicate.or(qNotaCredito.cliente.isNull());
            }
            break;
        }
      }
      builder.and(rsPredicate);
    }
    return builder;
  }

  private BooleanBuilder getBuilderNotaDebito(
      BusquedaNotaCriteria criteria, long idUsuarioLoggedIn) {
    QNotaDebito qNotDebito = QNotaDebito.notaDebito;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qNotDebito
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qNotDebito.eliminada.eq(false)));
    if (criteria.getMovimiento() == Movimiento.VENTA)
      builder.and(qNotDebito.movimiento.eq(Movimiento.VENTA));
    if (criteria.getMovimiento() == Movimiento.COMPRA)
      builder.and(qNotDebito.movimiento.eq(Movimiento.COMPRA));
    // Fecha
    if (criteria.isBuscaPorFecha()) {
      FormatterFechaHora formateadorFecha =
          new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL);
      DateExpression<Date> fDesde =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaDesde()));
      DateExpression<Date> fHasta =
          Expressions.dateTemplate(
              Date.class,
              "convert({0}, datetime)",
              formateadorFecha.format(criteria.getFechaHasta()));
      builder.and(qNotDebito.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscaUsuario())
      builder.and(qNotDebito.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.isBuscaCliente())
      builder.and(qNotDebito.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.isBuscaProveedor())
      builder.and(qNotDebito.proveedor.id_Proveedor.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qNotDebito.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaPorNumeroNota())
      builder
          .and(qNotDebito.serie.eq(criteria.getNumSerie()))
          .and(qNotDebito.nroNota.eq(criteria.getNumNota()));
    Usuario usuarioLogueado = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    if (!usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLogueado.getRoles().contains(Rol.VENDEDOR)
        && !usuarioLogueado.getRoles().contains(Rol.ENCARGADO)) {
      for (Rol rol : usuarioLogueado.getRoles()) {
        switch (rol) {
          case VIAJANTE:
            rsPredicate.or(qNotDebito.cliente.viajante.eq(usuarioLogueado));
            break;
          case COMPRADOR:
            Cliente clienteRelacionado =
                clienteService.getClientePorIdUsuarioYidEmpresa(
                    idUsuarioLoggedIn, criteria.getIdEmpresa());
            if (clienteRelacionado != null) {
              rsPredicate.or(qNotDebito.cliente.eq(clienteRelacionado));
            } else {
              rsPredicate.or(qNotDebito.cliente.isNull());
            }
            break;
        }
      }
      builder.and(rsPredicate);
    }
    return builder;
  }

  @Override
  public Factura getFacturaDeLaNotaCredito(Long idNota) {
    NotaCredito nota = this.notaCreditoRepository.getById(idNota);
    return (nota.getFacturaVenta() != null ? nota.getFacturaVenta() : nota.getFacturaCompra());
  }

  @Override
  public boolean existsNotaDebitoPorRecibo(Recibo recibo) {
    return notaDebitoRepository.existsByReciboAndEliminada(recibo, false);
  }

  @Override
  public boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta) {
    return notaCreditoRepository.existsByFacturaVentaAndEliminada(facturaVenta, false);
  }

  @Override
  public List<NotaCredito> getNotasCreditoPorFactura(Long idFactura) {
    List<NotaCredito> notasCredito = new ArrayList<>();
    Factura factura = facturaService.getFacturaPorId(idFactura);
    if (factura instanceof FacturaVenta) {
      notasCredito =
          notaCreditoRepository.findAllByFacturaVentaAndEliminada((FacturaVenta) factura, false);
    } else if (factura instanceof FacturaCompra) {
      notasCredito =
          notaCreditoRepository.findAllByFacturaCompraAndEliminada((FacturaCompra) factura, false);
    }
    return notasCredito;
  }

  @Override
  public List<RenglonFactura> getRenglonesFacturaModificadosParaNotaCredito(long idFactura) {
    HashMap<Long, BigDecimal> listaCantidadesProductosUnificados = new HashMap<>();
    this.getNotasCreditoPorFactura(idFactura)
        .forEach(
            n ->
                n.getRenglonesNotaCredito()
                    .forEach(
                        rnc -> {
                          if (listaCantidadesProductosUnificados.containsKey(
                              rnc.getIdProductoItem())) {
                            listaCantidadesProductosUnificados.put(
                                rnc.getIdProductoItem(),
                                listaCantidadesProductosUnificados
                                    .get(rnc.getIdProductoItem())
                                    .add(rnc.getCantidad()));
                          } else {
                            listaCantidadesProductosUnificados.put(
                                rnc.getIdProductoItem(), rnc.getCantidad());
                          }
                        }));
    List<RenglonFactura> renglonesFactura = facturaService.getRenglonesDeLaFactura(idFactura);
    if (!listaCantidadesProductosUnificados.isEmpty()) {
      renglonesFactura.forEach(
          rf -> {
            if (listaCantidadesProductosUnificados.containsKey(rf.getId_ProductoItem())) {
              rf.setCantidad(
                  rf.getCantidad()
                      .subtract(listaCantidadesProductosUnificados.get(rf.getId_ProductoItem())));
            }
          });
    }
    return renglonesFactura;
  }

  @Override
  public long getSiguienteNumeroNotaDebitoCliente(
      Long idEmpresa, TipoDeComprobante tipoDeComprobante) {
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Long numeroNota =
        notaDebitoRepository.buscarMayorNumNotaDebitoClienteSegunTipo(
            tipoDeComprobante,
            configuracionDelSistemaService
                .getConfiguracionDelSistemaPorEmpresa(empresa)
                .getNroPuntoDeVentaAfip(),
            idEmpresa);
    return (numeroNota == null) ? 1 : numeroNota + 1;
  }

  @Override
  public long getSiguienteNumeroNotaCreditoCliente(
      Long idEmpresa, TipoDeComprobante tipoDeComprobante) {
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Long numeroNota =
        notaCreditoRepository.buscarMayorNumNotaCreditoClienteSegunTipo(
            tipoDeComprobante,
            configuracionDelSistemaService
                .getConfiguracionDelSistemaPorEmpresa(empresa)
                .getNroPuntoDeVentaAfip(),
            idEmpresa);
    return (numeroNota == null) ? 1 : numeroNota + 1;
  }

  @Override
  public TipoDeComprobante[] getTipoNotaCliente(Long idCliente, Long idEmpresa) {
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Cliente cliente = clienteService.getClientePorId(idCliente);
    if (empresa.getCondicionIVA().isDiscriminaIVA()
        && cliente.getCondicionIVA().isDiscriminaIVA()) {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[6];
      tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_A;
      tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_X;
      tiposPermitidos[2] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
      tiposPermitidos[3] = TipoDeComprobante.NOTA_DEBITO_A;
      tiposPermitidos[4] = TipoDeComprobante.NOTA_DEBITO_X;
      tiposPermitidos[5] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
      return tiposPermitidos;
    } else if (empresa.getCondicionIVA().isDiscriminaIVA()
        && !cliente.getCondicionIVA().isDiscriminaIVA()) {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[6];
      tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_B;
      tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_X;
      tiposPermitidos[2] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
      tiposPermitidos[3] = TipoDeComprobante.NOTA_DEBITO_B;
      tiposPermitidos[4] = TipoDeComprobante.NOTA_DEBITO_X;
      tiposPermitidos[5] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
      return tiposPermitidos;
    } else {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
      tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_X;
      tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
      tiposPermitidos[2] = TipoDeComprobante.NOTA_DEBITO_X;
      tiposPermitidos[3] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
      return tiposPermitidos;
    }
  }

  @Override
  public TipoDeComprobante[] getTiposNota(Empresa empresa) {
    // cuando la Empresa discrimina IVA
    if (empresa.getCondicionIVA().isDiscriminaIVA()) {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[10];
      tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_A;
      tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_B;
      tiposPermitidos[2] = TipoDeComprobante.NOTA_CREDITO_X;
      tiposPermitidos[3] = TipoDeComprobante.NOTA_CREDITO_Y;
      tiposPermitidos[4] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
      tiposPermitidos[5] = TipoDeComprobante.NOTA_DEBITO_A;
      tiposPermitidos[6] = TipoDeComprobante.NOTA_DEBITO_B;
      tiposPermitidos[7] = TipoDeComprobante.NOTA_DEBITO_X;
      tiposPermitidos[8] = TipoDeComprobante.NOTA_DEBITO_Y;
      tiposPermitidos[9] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
      return tiposPermitidos;
    } else {
      // cuando la Empresa NO discrimina IVA
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[8];
      tiposPermitidos[0] = TipoDeComprobante.NOTA_CREDITO_B;
      tiposPermitidos[1] = TipoDeComprobante.NOTA_CREDITO_X;
      tiposPermitidos[2] = TipoDeComprobante.NOTA_CREDITO_Y;
      tiposPermitidos[3] = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
      tiposPermitidos[4] = TipoDeComprobante.NOTA_DEBITO_B;
      tiposPermitidos[5] = TipoDeComprobante.NOTA_DEBITO_X;
      tiposPermitidos[6] = TipoDeComprobante.NOTA_DEBITO_Y;
      tiposPermitidos[7] = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
      return tiposPermitidos;
    }
  }

  @Override
  public List<RenglonNotaCredito> getRenglonesDeNotaCredito(Long idNota) {
    return this.notaCreditoRepository.getById(idNota).getRenglonesNotaCredito();
  }

  @Override
  public List<RenglonNotaDebito> getRenglonesDeNotaDebito(Long idNota) {
    return this.notaDebitoRepository.getById(idNota).getRenglonesNotaDebito();
  }

  private void validarNota(Nota nota) {
    if (nota instanceof NotaCredito && nota.getMovimiento().equals(Movimiento.VENTA)) {
      if (nota.getFecha().compareTo(nota.getFacturaVenta().getFecha()) <= 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_fecha_incorrecta"));
      }
      if (nota.getCAE() != 0L) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_cliente_CAE"));
      }
    } else if (nota instanceof NotaCredito && nota.getMovimiento().equals(Movimiento.COMPRA)) {
      if (nota.getFecha().compareTo(nota.getFacturaCompra().getFecha()) <= 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_fecha_incorrecta"));
      }
    }
    if (nota.getMotivo() == null || nota.getMotivo().isEmpty()) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_de_motivo_vacio"));
    }
    if (nota instanceof NotaCredito) {
      if (((NotaCredito) nota).getRenglonesNotaCredito() == null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_de_renglones_vacio"));
      }
    } else {
      if (((NotaDebito) nota).getRenglonesNotaDebito() == null) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_de_renglones_vacio"));
      }
    }
  }

  private void validarCalculosCredito(NotaCredito notaCredito) {
    TipoDeComprobante tipoDeComprobanteDeFacturaRelacionada =
        this.getTipoDeComprobanteFacturaSegunNotaCredito(notaCredito);
    List<RenglonNotaCredito> renglonesNotaCredito = notaCredito.getRenglonesNotaCredito();
    BigDecimal subTotal = BigDecimal.ZERO;
    BigDecimal[] importes = new BigDecimal[renglonesNotaCredito.size()];
    int i = 0;
    int sizeRenglonesCredito = renglonesNotaCredito.size();
    // IVA - subTotal
    BigDecimal iva21 = BigDecimal.ZERO;
    BigDecimal iva105 = BigDecimal.ZERO;
    if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_A
        || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_B
        || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_Y
        || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) {
      BigDecimal[] ivaPorcentajes = new BigDecimal[sizeRenglonesCredito];
      BigDecimal[] ivaNetos = new BigDecimal[sizeRenglonesCredito];
      BigDecimal[] cantidades = new BigDecimal[sizeRenglonesCredito];
      for (RenglonNotaCredito r : renglonesNotaCredito) {
        ivaPorcentajes[i] = r.getIvaPorcentaje();
        ivaNetos[i] = r.getIvaNeto();
        cantidades[i] = r.getCantidad();
        importes[i] = r.getImporteBruto();
        i++;
      }
      subTotal = this.calcularSubTotalCredito(importes);
      if (notaCredito.getSubTotal().compareTo(subTotal) != 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_sub_total_no_valido"));
      }
      iva21 =
          this.calcularIVANetoCredito(
              tipoDeComprobanteDeFacturaRelacionada,
              cantidades,
              ivaPorcentajes,
              ivaNetos,
              IVA_21,
              notaCredito.getDescuentoPorcentaje(),
              notaCredito.getRecargoPorcentaje());
      if (notaCredito.getIva21Neto().compareTo(iva21) != 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_iva21_no_valido"));
      }
      iva105 =
          this.calcularIVANetoCredito(
              tipoDeComprobanteDeFacturaRelacionada,
              cantidades,
              ivaPorcentajes,
              ivaNetos,
              IVA_105,
              notaCredito.getDescuentoPorcentaje(),
              notaCredito.getRecargoPorcentaje());
      if (notaCredito.getIva105Neto().compareTo(iva105) != 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_iva105_no_valido"));
      }
    } else if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_X) {
      for (RenglonNotaCredito r : renglonesNotaCredito) {
        importes[i] = r.getImporteNeto();
        i++;
      }
      subTotal = this.calcularSubTotalCredito(importes);
      if (notaCredito.getSubTotal().compareTo(subTotal) != 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_sub_total_no_valido"));
      }
      if (notaCredito.getIva21Neto().compareTo(BigDecimal.ZERO) != 0.0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_iva21_no_valido"));
      }
      if (notaCredito.getIva105Neto().compareTo(BigDecimal.ZERO) != 0.0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_iva105_no_valido"));
      }
    }
    // DescuentoNeto
    BigDecimal descuentoNeto =
        this.calcularDecuentoNetoCredito(subTotal, notaCredito.getDescuentoPorcentaje());
    if (notaCredito.getDescuentoNeto().compareTo(descuentoNeto) != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_descuento_neto_no_valido"));
    }
    // RecargoNeto
    BigDecimal recargoNeto =
        this.calcularRecargoNetoCredito(subTotal, notaCredito.getRecargoPorcentaje());
    if (notaCredito.getRecargoNeto().compareTo(recargoNeto) != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_recargo_neto_no_valido"));
    }
    // subTotalBruto
    BigDecimal subTotalBruto =
        this.calcularSubTotalBrutoCredito(
            tipoDeComprobanteDeFacturaRelacionada,
            subTotal,
            recargoNeto,
            descuentoNeto,
            iva105,
            iva21);
    if (notaCredito.getSubTotalBruto().compareTo(subTotalBruto) != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_sub_total_bruto_no_valido"));
    }
    // Total
    if (notaCredito.getTotal().compareTo(this.calcularTotalCredito(subTotalBruto, iva105, iva21))
        != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_total_no_valido"));
    }
  }

  private void validarCalculosDebito(NotaDebito notaDebito) {
    // monto no gravado
    BigDecimal montoComprobante = BigDecimal.ZERO;
    if (notaDebito.getRecibo() != null) {
      montoComprobante = notaDebito.getRecibo().getMonto();
    }
    if (notaDebito.getMontoNoGravado().compareTo(montoComprobante) != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_nota_monto_no_gravado_no_valido"));
    }
    // iva 21
    BigDecimal iva21 = BigDecimal.ZERO;
    switch (notaDebito.getTipoComprobante()) {
      case NOTA_DEBITO_X:
      case NOTA_DEBITO_A:
      case NOTA_DEBITO_B:
      case NOTA_DEBITO_Y:
      case NOTA_DEBITO_PRESUPUESTO:
      case NOTA_CREDITO_X:
      case NOTA_CREDITO_A:
      case NOTA_CREDITO_B:
      case NOTA_CREDITO_PRESUPUESTO:
        iva21 = notaDebito.getSubTotalBruto().multiply(new BigDecimal("0.21"));
        if (notaDebito.getIva21Neto().compareTo(iva21) != 0) {
          throw new BusinessServiceException(
              ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_iva21_no_valido"));
        }
        break;
    }
    // total
    if (notaDebito
            .getTotal()
            .compareTo(
                this.calcularTotalDebito(notaDebito.getSubTotalBruto(), iva21, montoComprobante))
        != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_total_no_valido"));
    }
  }

  @Override
  @Transactional
  public Nota guardarNotaCredito(NotaCredito notaCredito) {
    if (notaCredito.getFecha() == null) {
      notaCredito.setFecha(new Date());
    }
    this.validarNota(notaCredito);
    if (notaCredito.getMovimiento().equals(Movimiento.VENTA)) {
      notaCredito.setTipoComprobante(
          this.getTipoDeNotaCreditoSegunFactura(notaCredito.getFacturaVenta()));
      notaCredito.setSerie(
          configuracionDelSistemaService
              .getConfiguracionDelSistemaPorEmpresa(notaCredito.getEmpresa())
              .getNroPuntoDeVentaAfip());
      notaCredito.setNroNota(
          this.getSiguienteNumeroNotaCreditoCliente(
              notaCredito.getIdEmpresa(), notaCredito.getTipoComprobante()));
    } else if (notaCredito.getMovimiento().equals(Movimiento.COMPRA)) {
      notaCredito.setTipoComprobante(
          this.getTipoDeNotaCreditoSegunFactura(notaCredito.getFacturaCompra()));
    }
    if (notaCredito.isModificaStock()) {
      this.actualizarStock(notaCredito.getRenglonesNotaCredito(), TipoDeOperacion.ACTUALIZACION);
    }
    this.validarCalculosCredito(notaCredito);
    notaCredito = notaCreditoRepository.save(notaCredito);
    this.cuentaCorrienteService.asentarEnCuentaCorriente(notaCredito, TipoDeOperacion.ALTA);
    logger.warn("La Nota {} se guardó correctamente.", notaCredito);
    return notaCredito;
  }

  @Override
  @Transactional
  public NotaDebito guardarNotaDebito(NotaDebito notaDebito) {
    if (notaDebito.getFecha() == null) {
      notaDebito.setFecha(new Date());
    }
    this.validarNota(notaDebito);
    if (notaDebito.getMovimiento().equals(Movimiento.VENTA)) {
      notaDebito.setTipoComprobante(
          this.getTipoDeNotaDebito(
              this.facturaService
                  .getTipoFacturaVenta(notaDebito.getEmpresa(), notaDebito.getCliente())[0]));
      notaDebito.setSerie(
          configuracionDelSistemaService
              .getConfiguracionDelSistemaPorEmpresa(notaDebito.getEmpresa())
              .getNroPuntoDeVentaAfip());
      notaDebito.setNroNota(
          this.getSiguienteNumeroNotaDebitoCliente(
              notaDebito.getIdEmpresa(), notaDebito.getTipoComprobante()));
    } else if (notaDebito.getMovimiento().equals(Movimiento.COMPRA)) {
      notaDebito.setTipoComprobante(
          this.getTipoDeNotaDebito(
              this.facturaService
                  .getTipoFacturaCompra(notaDebito.getEmpresa(), notaDebito.getProveedor())[0]));
    }
    this.validarCalculosDebito(notaDebito);
    notaDebito = notaDebitoRepository.save(notaDebito);
    cuentaCorrienteService.asentarEnCuentaCorriente(notaDebito, TipoDeOperacion.ALTA);
    logger.warn("La Nota {} se guardó correctamente.", notaDebito);
    return notaDebito;
  }

  @Override
  @Transactional
  public Nota autorizarNota(Nota nota) {
    if (nota.getMovimiento().equals(Movimiento.VENTA)) {
      BigDecimal montoNoGravado =
          (nota instanceof NotaDebito) ? ((NotaDebito) nota).getMontoNoGravado() : BigDecimal.ZERO;
      Cliente cliente;
      if (nota instanceof NotaCredito) {
        if (nota.getFacturaVenta().getCAE() == 0L) {
          throw new BusinessServiceException(
              ResourceBundle.getBundle("Mensajes")
                  .getString("mensaje_nota_factura_relacionada_sin_CAE"));
        }
        cliente = nota.getCliente();
      } else {
        cliente = nota.getCliente();
      }
      ComprobanteAFIP comprobante =
          ComprobanteAFIP.builder()
              .fecha(nota.getFecha())
              .tipoComprobante(nota.getTipoComprobante())
              .CAE(nota.getCAE())
              .vencimientoCAE(nota.getVencimientoCAE())
              .numSerieAfip(nota.getNumSerieAfip())
              .numFacturaAfip(nota.getNumNotaAfip())
              .empresa(nota.getEmpresa())
              .cliente(cliente)
              .subtotalBruto(nota.getSubTotalBruto())
              .iva105neto(nota.getIva105Neto())
              .iva21neto(nota.getIva21Neto())
              .montoNoGravado(montoNoGravado)
              .total(nota.getTotal())
              .build();
      afipService.autorizar(comprobante);
      nota.setCAE(comprobante.getCAE());
      nota.setVencimientoCAE(comprobante.getVencimientoCAE());
      nota.setNumSerieAfip(comprobante.getNumSerieAfip());
      nota.setNumNotaAfip(comprobante.getNumFacturaAfip());
      renglonCuentaCorrienteService.updateCAENota(nota.getIdNota(), comprobante.getCAE());
    } else {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_comprobanteAFIP_invalido"));
    }
    return nota;
  }

  private TipoDeComprobante getTipoDeNotaDebito(TipoDeComprobante tipo) {
    switch (tipo) {
      case FACTURA_A:
        tipo = TipoDeComprobante.NOTA_DEBITO_A;
        break;
      case FACTURA_B:
        tipo = TipoDeComprobante.NOTA_DEBITO_B;
        break;
      case FACTURA_X:
        tipo = TipoDeComprobante.NOTA_DEBITO_X;
        break;
      case FACTURA_Y:
        tipo = TipoDeComprobante.NOTA_DEBITO_Y;
        break;
      case PRESUPUESTO:
        tipo = TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO;
        break;
    }
    return tipo;
  }

  private TipoDeComprobante getTipoDeComprobanteFacturaSegunNotaCredito(NotaCredito notaCredito) {
    TipoDeComprobante tipo = null;
    if (notaCredito.getTipoComprobante() != null) {
      switch (notaCredito.getTipoComprobante()) {
        case NOTA_CREDITO_A:
          tipo = TipoDeComprobante.FACTURA_A;
          break;
        case NOTA_CREDITO_B:
          tipo = TipoDeComprobante.FACTURA_B;
          break;
        case NOTA_CREDITO_X:
          tipo = TipoDeComprobante.FACTURA_X;
          break;
        case NOTA_CREDITO_Y:
          tipo = TipoDeComprobante.FACTURA_X;
          break;
        case NOTA_CREDITO_PRESUPUESTO:
          tipo = TipoDeComprobante.PRESUPUESTO;
          break;
        default:
          throw new ServiceException(
              ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_tipo_no_valido"));
      }
    }
    return tipo;
  }

  private TipoDeComprobante getTipoDeNotaCreditoSegunFactura(Factura factura) {
    TipoDeComprobante tipo = null;
    switch (factura.getTipoComprobante()) {
      case FACTURA_A:
        tipo = TipoDeComprobante.NOTA_CREDITO_A;
        break;
      case FACTURA_B:
        tipo = TipoDeComprobante.NOTA_CREDITO_B;
        break;
      case FACTURA_X:
        tipo = TipoDeComprobante.NOTA_CREDITO_X;
        break;
      case FACTURA_Y:
        tipo = TipoDeComprobante.NOTA_CREDITO_Y;
        break;
      case PRESUPUESTO:
        tipo = TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
        break;
      default:
        throw new ServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_tipo_no_valido"));
    }
    return tipo;
  }

  private void actualizarStock(
      List<RenglonNotaCredito> renglonesNotaCredito, TipoDeOperacion tipoOperacion) {
    HashMap<Long, BigDecimal> idsYCantidades = new HashMap<>();
    renglonesNotaCredito.forEach(r -> idsYCantidades.put(r.getIdProductoItem(), r.getCantidad()));
    if (tipoOperacion == TipoDeOperacion.ELIMINACION) {
      tipoOperacion = TipoDeOperacion.ALTA;
    }
    productoService.actualizarStock(idsYCantidades, tipoOperacion, Movimiento.VENTA);
  }

  @Override
  public byte[] getReporteNota(Nota nota) {
    ClassLoader classLoader = NotaServiceImpl.class.getClassLoader();
    InputStream isFileReport;
    JRBeanCollectionDataSource ds;
    Map<String, Object> params = new HashMap<>();
    if (nota instanceof NotaCredito) {
      isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/NotaCredito.jasper");
      List<RenglonNotaCredito> renglones = this.getRenglonesDeNotaCredito(nota.getIdNota());
      ds = new JRBeanCollectionDataSource(renglones);
      params.put("notaCredito", nota);
    } else {
      isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/NotaDebito.jasper");
      List<RenglonNotaDebito> renglones = this.getRenglonesDeNotaDebito(nota.getIdNota());
      ds = new JRBeanCollectionDataSource(renglones);
      params.put("notaDebito", nota);
    }
    ConfiguracionDelSistema cds =
        configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(nota.getEmpresa());
    params.put("preImpresa", cds.isUsarFacturaVentaPreImpresa());
    if (nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_X)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_X)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO)) {
      nota.setSubTotalBruto(nota.getTotal());
      nota.setIva105Neto(BigDecimal.ZERO);
      nota.setIva21Neto(BigDecimal.ZERO);
    }
    if (nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_A)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_A)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B)) {
      if (nota.getNumSerieAfip() != 0 && nota.getNumNotaAfip() != 0) {
        params.put("serie", nota.getNumSerieAfip());
        params.put("nroNota", nota.getNumNotaAfip());
      } else {
        params.put("serie", null);
        params.put("nroNota", null);
      }
    } else {
      params.put("serie", nota.getSerie());
      params.put("nroNota", nota.getNroNota());
    }
    if (!nota.getEmpresa().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(nota.getEmpresa().getLogo()))).getImage());
      } catch (IOException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_empresa_404_logo"), ex);
      }
    }
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params, ds));
    } catch (JRException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_error_reporte"), ex);
    }
  }

  @Override
  @Transactional
  public void eliminarNota(long[] idsNota) {
    for (long idNota : idsNota) {
      Nota nota = this.getNotaPorId(idNota);
      if (nota != null) {
        if (nota.getMovimiento() == Movimiento.VENTA) {
          if (nota.getCAE() != 0L) {
            throw new BusinessServiceException(
                ResourceBundle.getBundle("Mensajes").getString("mensaje_eliminar_nota_aprobada"));
          }
          if (nota instanceof NotaCredito) {
            NotaCredito nc = (NotaCredito) nota;
            if (nc.isModificaStock()) {
              this.actualizarStock(nc.getRenglonesNotaCredito(), TipoDeOperacion.ALTA);
            }
          }
        } else if (nota.getMovimiento() == Movimiento.COMPRA && nota instanceof NotaCredito) {
          NotaCredito nc = (NotaCredito) nota;
          if (nc.isModificaStock()) {
            this.actualizarStock(nc.getRenglonesNotaCredito(), TipoDeOperacion.ACTUALIZACION);
          }
        }
        nota.setEliminada(true);
        this.cuentaCorrienteService.asentarEnCuentaCorriente(nota, TipoDeOperacion.ELIMINACION);
        notaRepository.save(nota);
        logger.warn("La Nota {} se eliminó correctamente.", nota);
      }
    }
  }

  @Override
  public BigDecimal calcularTotalNota(List<RenglonNotaCredito> renglonesNota) {
    BigDecimal total = BigDecimal.ZERO;
    for (RenglonNotaCredito renglon : renglonesNota) {
      total = total.add(renglon.getImporteNeto());
    }
    return total;
  }

  @Override
  public BigDecimal getIvaNetoNota(Long idNota) {
    Nota nota = this.getNotaPorId(idNota);
    BigDecimal ivaNeto = BigDecimal.ZERO;
    if (nota instanceof NotaCredito) {
      for (RenglonNotaCredito r : this.getRenglonesDeNotaCredito(nota.getIdNota())) {
        ivaNeto =
            ivaNeto.add(
                r.getIvaPorcentaje()
                    .divide(CIEN, 15, RoundingMode.HALF_UP)
                    .multiply(r.getImporte()));
      }
    } else {
      for (RenglonNotaCredito r : this.getRenglonesDeNotaCredito(nota.getIdNota())) {
        ivaNeto = ivaNeto.add(r.getIvaNeto());
      }
    }
    return ivaNeto;
  }

  @Override
  public List<RenglonNotaCredito> calcularRenglonCredito(
      TipoDeComprobante tipo, BigDecimal[] cantidad, long[] idRenglonFactura) {
    List<RenglonNotaCredito> renglonesNota = new ArrayList<>();
    RenglonNotaCredito renglonNota;
    if (cantidad.length == idRenglonFactura.length) {
      for (int i = 0; i < idRenglonFactura.length; i++) {
        RenglonFactura renglonFactura = facturaService.getRenglonFactura(idRenglonFactura[i]);
        if (renglonFactura.getCantidad().compareTo(cantidad[i]) < 0
            || cantidad[i].compareTo(BigDecimal.ZERO) < 0) {
          throw new BusinessServiceException(
              ResourceBundle.getBundle("Mensajes")
                      .getString("mensaje_nota_de_credito_cantidad_no_valida")
                  + " "
                  + renglonFactura.getDescripcionItem());
        }
        renglonNota = new RenglonNotaCredito();
        renglonNota.setIdProductoItem(renglonFactura.getId_ProductoItem());
        renglonNota.setCodigoItem(renglonFactura.getCodigoItem());
        renglonNota.setDescripcionItem(renglonFactura.getDescripcionItem());
        renglonNota.setMedidaItem(renglonFactura.getMedidaItem());
        renglonNota.setCantidad(cantidad[i]);
        renglonNota.setPrecioUnitario(renglonFactura.getPrecioUnitario());
        renglonNota.setDescuentoPorcentaje(renglonFactura.getDescuento_porcentaje());
        renglonNota.setDescuentoNeto(
            renglonFactura
                .getDescuento_porcentaje()
                .divide(CIEN, 15, RoundingMode.HALF_UP)
                .multiply(renglonNota.getPrecioUnitario()));
        renglonNota.setGananciaPorcentaje(renglonFactura.getGanancia_porcentaje());
        renglonNota.setGananciaNeto(
            renglonNota
                .getGananciaPorcentaje()
                .divide(CIEN, 15, RoundingMode.HALF_UP)
                .multiply(renglonNota.getPrecioUnitario()));
        renglonNota.setIvaPorcentaje(renglonFactura.getIva_porcentaje());
        if (tipo.equals(TipoDeComprobante.FACTURA_Y)) {
          renglonNota.setIvaPorcentaje(
              renglonFactura
                  .getIva_porcentaje()
                  .divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP));
        }
        renglonNota.setIvaNeto(
            (tipo == TipoDeComprobante.FACTURA_A
                    || tipo == TipoDeComprobante.FACTURA_B
                    || tipo == TipoDeComprobante.PRESUPUESTO)
                ? renglonFactura.getIva_neto()
                : BigDecimal.ZERO);
        renglonNota.setImporte(renglonNota.getPrecioUnitario().multiply(cantidad[i]));
        renglonNota.setImporteBruto(
            renglonNota
                .getImporte()
                .subtract(renglonNota.getDescuentoNeto().multiply(cantidad[i])));
        if (tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
          renglonNota.setImporteNeto(renglonNota.getImporteBruto());
        } else {
          renglonNota.setImporteNeto(
              renglonNota.getImporteBruto().add(renglonNota.getIvaNeto().multiply(cantidad[i])));
        }
        renglonesNota.add(renglonNota);
      }
    }
    return renglonesNota;
  }

  @Override
  public List<RenglonNotaDebito> calcularRenglonDebito(
      long idRecibo, BigDecimal monto, BigDecimal ivaPorcentaje) {
    List<RenglonNotaDebito> renglonesNota = new ArrayList<>();
    RenglonNotaDebito renglonNota;
    Recibo r = reciboService.getById(idRecibo);
    renglonNota = new RenglonNotaDebito();
    String descripcion =
        "Recibo Nº "
            + r.getNumRecibo()
            + " "
            + (new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHA_HISPANO))
                .format(r.getFecha());
    renglonNota.setDescripcion(descripcion);
    renglonNota.setMonto(r.getMonto());
    renglonNota.setImporteBruto(renglonNota.getMonto());
    renglonNota.setIvaPorcentaje(BigDecimal.ZERO);
    renglonNota.setIvaNeto(BigDecimal.ZERO);
    renglonNota.setImporteNeto(r.getMonto());
    renglonesNota.add(renglonNota);
    renglonNota = new RenglonNotaDebito();
    renglonNota.setDescripcion("Gasto Administrativo");
    renglonNota.setMonto(monto);
    renglonNota.setIvaPorcentaje(ivaPorcentaje);
    renglonNota.setIvaNeto(monto.multiply(ivaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)));
    renglonNota.setImporteBruto(monto);
    renglonNota.setImporteNeto(renglonNota.getIvaNeto().add(renglonNota.getImporteBruto()));
    renglonesNota.add(renglonNota);
    return renglonesNota;
  }

  @Override
  public BigDecimal calcularSubTotalCredito(BigDecimal[] importes) {
    BigDecimal resultado = BigDecimal.ZERO;
    for (BigDecimal importe : importes) {
      resultado = resultado.add(importe);
    }
    return resultado;
  }

  @Override
  public BigDecimal calcularDecuentoNetoCredito(
      BigDecimal subTotal, BigDecimal descuentoPorcentaje) {
    BigDecimal resultado = BigDecimal.ZERO;
    if (descuentoPorcentaje.compareTo(BigDecimal.ZERO) != 0) {
      resultado = subTotal.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    }
    return resultado;
  }

  @Override
  public BigDecimal calcularRecargoNetoCredito(BigDecimal subTotal, BigDecimal recargoPorcentaje) {
    BigDecimal resultado = BigDecimal.ZERO;
    if (recargoPorcentaje.compareTo(BigDecimal.ZERO) != 0) {
      resultado = subTotal.multiply(recargoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    }
    return resultado;
  }

  @Override
  public BigDecimal calcularIVANetoCredito(
      TipoDeComprobante tipoDeComprobante,
      BigDecimal[] cantidades,
      BigDecimal[] ivaPorcentajeRenglones,
      BigDecimal[] ivaNetoRenglones,
      BigDecimal ivaPorcentaje,
      BigDecimal descuentoPorcentaje,
      BigDecimal recargoPorcentaje) {
    return facturaService.calcularIvaNetoFactura(
        tipoDeComprobante,
        cantidades,
        ivaPorcentajeRenglones,
        ivaNetoRenglones,
        ivaPorcentaje,
        descuentoPorcentaje,
        recargoPorcentaje);
  }

  @Override
  public BigDecimal calcularSubTotalBrutoCredito(
      TipoDeComprobante tipoDeComprobante,
      BigDecimal subTotal,
      BigDecimal recargoNeto,
      BigDecimal descuentoNeto,
      BigDecimal iva105Neto,
      BigDecimal iva21Neto) {
    BigDecimal resultado = subTotal.add(recargoNeto).subtract(descuentoNeto);
    if (tipoDeComprobante == TipoDeComprobante.FACTURA_B
        || tipoDeComprobante == TipoDeComprobante.PRESUPUESTO) {
      resultado = resultado.subtract(iva105Neto.add(iva21Neto));
    }
    return resultado;
  }

  @Override
  public BigDecimal calcularTotalCredito(
      BigDecimal subTotalBruto, BigDecimal iva105Neto, BigDecimal iva21Neto) {
    return subTotalBruto.add(iva105Neto).add(iva21Neto);
  }

  @Override
  public BigDecimal calcularTotalDebito(
      BigDecimal subTotalBruto, BigDecimal iva21Neto, BigDecimal montoNoGravado) {
    return subTotalBruto.add(iva21Neto).add(montoNoGravado);
  }

  @Override
  public BigDecimal calcularTotalCredito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn) {
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_fechas_busqueda_invalidas"));
    }
    if (criteria.isBuscaPorFecha()) {
      Calendar cal = new GregorianCalendar();
      cal.setTime(criteria.getFechaDesde());
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      criteria.setFechaDesde(cal.getTime());
      cal.setTime(criteria.getFechaHasta());
      cal.set(Calendar.HOUR_OF_DAY, 23);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 59);
      criteria.setFechaHasta(cal.getTime());
    }
    BigDecimal totalNotaCredito =
        notaCreditoRepository.calcularTotalCredito(
            this.getBuilderNotaCredito(criteria, idUsuarioLoggedIn));
    return (totalNotaCredito != null ? totalNotaCredito : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularTotalDebito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn) {
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_fechas_busqueda_invalidas"));
    }
    if (criteria.isBuscaPorFecha()) {
      Calendar cal = new GregorianCalendar();
      cal.setTime(criteria.getFechaDesde());
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      criteria.setFechaDesde(cal.getTime());
      cal.setTime(criteria.getFechaHasta());
      cal.set(Calendar.HOUR_OF_DAY, 23);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 59);
      criteria.setFechaHasta(cal.getTime());
    }
    BigDecimal totalNotaDebito =
        notaDebitoRepository.calcularTotalDebito(
            this.getBuilderNotaDebito(criteria, idUsuarioLoggedIn));
    return (totalNotaDebito != null ? totalNotaDebito : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularTotalIVACredito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn) {
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_fechas_busqueda_invalidas"));
    }
    if (criteria.isBuscaPorFecha()) {
      Calendar cal = new GregorianCalendar();
      cal.setTime(criteria.getFechaDesde());
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      criteria.setFechaDesde(cal.getTime());
      cal.setTime(criteria.getFechaHasta());
      cal.set(Calendar.HOUR_OF_DAY, 23);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 59);
      criteria.setFechaHasta(cal.getTime());
    }
    BigDecimal ivaNotaCredito =
        notaCreditoRepository.calcularIVACredito(
            this.getBuilderNotaCredito(criteria, idUsuarioLoggedIn),
            new TipoDeComprobante[] {
              TipoDeComprobante.NOTA_CREDITO_A, TipoDeComprobante.NOTA_CREDITO_B
            });
    return (ivaNotaCredito != null ? ivaNotaCredito : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal calcularTotalIVADebito(BusquedaNotaCriteria criteria, long idUsuarioLoggedIn) {
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_nota_fechas_busqueda_invalidas"));
    }
    if (criteria.isBuscaPorFecha()) {
      Calendar cal = new GregorianCalendar();
      cal.setTime(criteria.getFechaDesde());
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      criteria.setFechaDesde(cal.getTime());
      cal.setTime(criteria.getFechaHasta());
      cal.set(Calendar.HOUR_OF_DAY, 23);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 59);
      criteria.setFechaHasta(cal.getTime());
    }
    BigDecimal ivaNotaDebito =
        notaDebitoRepository.calcularIVADebito(
            this.getBuilderNotaDebito(criteria, idUsuarioLoggedIn),
            new TipoDeComprobante[] {
              TipoDeComprobante.NOTA_DEBITO_A, TipoDeComprobante.NOTA_DEBITO_B
            });
    return (ivaNotaDebito != null ? ivaNotaDebito : BigDecimal.ZERO);
  }
}
