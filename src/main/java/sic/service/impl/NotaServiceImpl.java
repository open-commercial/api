package sic.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;
import javax.validation.Valid;

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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.repository.NotaCreditoRepository;
import sic.repository.NotaDebitoRepository;
import sic.service.*;
import sic.repository.NotaRepository;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.util.FormatterFechaHora;

@Service
@Validated
public class NotaServiceImpl implements INotaService {

  private final NotaRepository notaRepository;
  private final NotaCreditoRepository notaCreditoRepository;
  private final NotaDebitoRepository notaDebitoRepository;
  private final IFacturaService facturaService;
  private final IClienteService clienteService;
  private final IProveedorService proveedorService;
  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;
  private final IProductoService productoService;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final IAfipService afipService;
  private static final BigDecimal IVA_21 = new BigDecimal("21");
  private static final BigDecimal IVA_105 = new BigDecimal("10.5");
  private static final BigDecimal CIEN = new BigDecimal("100");
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MessageSource messageSource;

  @Autowired
  @Lazy
  public NotaServiceImpl(
      NotaRepository notaRepository,
      NotaCreditoRepository notaCreditoRepository,
      NotaDebitoRepository notaDebitoRepository,
      IFacturaService facturaService,
      IClienteService clienteService,
      IProveedorService proveedorService,
      IUsuarioService usuarioService,
      IProductoService productoService,
      IEmpresaService empresaService,
      ICuentaCorrienteService cuentaCorrienteService,
      IConfiguracionDelSistemaService cds,
      IAfipService afipService,
      MessageSource messageSource) {
    this.notaRepository = notaRepository;
    this.notaCreditoRepository = notaCreditoRepository;
    this.notaDebitoRepository = notaDebitoRepository;
    this.facturaService = facturaService;
    this.clienteService = clienteService;
    this.proveedorService = proveedorService;
    this.usuarioService = usuarioService;
    this.empresaService = empresaService;
    this.productoService = productoService;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.configuracionDelSistemaService = cds;
    this.afipService = afipService;
    this.messageSource = messageSource;
  }

  @Override
  public Nota getNotaNoEliminadaPorId(long idNota) {
    Optional<Nota> nota = notaRepository.findById(idNota);
    if (nota.isPresent() && !nota.get().isEliminada()) {
      return nota.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_factura_eliminada", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public void eliminarNota(long idNota) {
    Nota nota = this.getNotaNoEliminadaPorId(idNota);
    if (nota.getMovimiento() == Movimiento.VENTA) {
      if (nota.getCAE() != 0L) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_eliminar_nota_aprobada", null, Locale.getDefault()));
      }
      this.cuentaCorrienteService.asentarEnCuentaCorriente(nota, TipoDeOperacion.ELIMINACION);
      if (nota instanceof NotaCredito) {
        NotaCredito nc = (NotaCredito) nota;
        if (nc.isModificaStock()) {
          this.actualizarStock(
              nc.getRenglonesNotaCredito(),
              TipoDeOperacion.ELIMINACION,
              nota.getMovimiento(),
              nota.getTipoComprobante());
        }
      }
      nota.setEliminada(true);
      notaRepository.save(nota);
      logger.warn("La Nota {} se eliminó correctamente.", nota);
    } else {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
    }
  }

  @Override
  public boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta) {
    return notaCreditoRepository.existsByFacturaVentaAndEliminada(facturaVenta, false);
  }

  @Override
  public Page<Nota> buscarNotas(BusquedaNotaCriteria busquedaNotaCriteria, long idUsuarioLoggedIn) {
    // Fecha de Nota
    if (busquedaNotaCriteria.isBuscaPorFecha()
        && (busquedaNotaCriteria.getFechaDesde() == null
            || busquedaNotaCriteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_fechas_busqueda_invalidas", null, Locale.getDefault()));
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
    if (criteria.isBuscaCliente() && criteria.getMovimiento().equals(Movimiento.VENTA))
      builder.and(qNota.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.isBuscaViajante() && criteria.getMovimiento().equals(Movimiento.VENTA))
      builder.and(qNota.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.isBuscaProveedor())
      builder.and(qNota.proveedor.id_Proveedor.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qNota.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaPorNumeroNota())
      builder
          .and(qNota.serie.eq(criteria.getNumSerie()))
          .and(qNota.nroNota.eq(criteria.getNumNota()));
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
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
    if (criteria.isBuscaViajante())
      builder.and(qNotaCredito.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.isBuscaProveedor())
      builder.and(qNotaCredito.proveedor.id_Proveedor.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qNotaCredito.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaPorNumeroNota())
      builder
          .and(qNotaCredito.serie.eq(criteria.getNumSerie()))
          .and(qNotaCredito.nroNota.eq(criteria.getNumNota()));
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
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
    if (criteria.isBuscaViajante())
      builder.and(qNotDebito.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.isBuscaProveedor())
      builder.and(qNotDebito.proveedor.id_Proveedor.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qNotDebito.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaPorNumeroNota())
      builder
          .and(qNotDebito.serie.eq(criteria.getNumSerie()))
          .and(qNotDebito.nroNota.eq(criteria.getNumNota()));
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
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
    Optional<NotaCredito> nc = this.notaCreditoRepository.findById(idNota);
    if (nc.isPresent()) {
      return (nc.get().getFacturaVenta() != null ? nc.get().getFacturaVenta() : nc.get().getFacturaCompra());
    } else {
     return null;
    }
  }

  @Override
  public boolean existsNotaDebitoPorRecibo(Recibo recibo) {
    return notaDebitoRepository.existsByReciboAndEliminada(recibo, false);
  }

  @Override
  public List<TipoDeComprobante> getTipoNotaCreditoCliente(Long idCliente, Long idEmpresa) {
    List<TipoDeComprobante> tiposPermitidos = new ArrayList<>();
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(idCliente);
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())
        && CategoriaIVA.discriminaIVA(cliente.getCategoriaIVA())) {
      tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_A);
    } else if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())
        && !CategoriaIVA.discriminaIVA(cliente.getCategoriaIVA())) {
      tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_B);
    } else {
      tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_C);
    }
    tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_X);
    tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO);
    return tiposPermitidos;
  }

  @Override
  public List<TipoDeComprobante>  getTipoNotaDebitoCliente(Long idCliente, Long idEmpresa) {
    List<TipoDeComprobante> tiposPermitidos = new ArrayList<>();
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(idCliente);
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())
      && CategoriaIVA.discriminaIVA(cliente.getCategoriaIVA())) {
      tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_A);
    } else if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())
      && !CategoriaIVA.discriminaIVA(cliente.getCategoriaIVA())) {
      tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_B);
    } else {
      tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_C);
    }
    tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_X);
    tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO);
    return tiposPermitidos;
  }

  @Override
  public List<TipoDeComprobante> getTipoNotaCreditoProveedor(Long idProveedor, Long idEmpresa) {
    List<TipoDeComprobante> tiposPermitidos = new ArrayList<>();
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Proveedor proveedor = proveedorService.getProveedorNoEliminadoPorId(idProveedor);
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())) {
      if (CategoriaIVA.discriminaIVA(proveedor.getCategoriaIVA())) {
        tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_A);
      } else {
        tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_C);
      }
    } else {
      if (CategoriaIVA.discriminaIVA(proveedor.getCategoriaIVA())) {
        tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_B);
      } else {
        tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_C);
      }
    }
    tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_X);
    tiposPermitidos.add(TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO);
    return tiposPermitidos;
  }

  @Override
  public List<TipoDeComprobante> getTipoNotaDebitoProveedor(Long idProveedor, Long idEmpresa) {
    List<TipoDeComprobante> tiposPermitidos = new ArrayList<>();
    Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
    Proveedor proveedor = proveedorService.getProveedorNoEliminadoPorId(idProveedor);
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())) {
      if (CategoriaIVA.discriminaIVA(proveedor.getCategoriaIVA())) {
        tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_A);
      } else {
        tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_C);
      }
    } else {
      if (CategoriaIVA.discriminaIVA(proveedor.getCategoriaIVA())) {
        tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_B);
      } else {
        tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_C);
      }
    }
    tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_X);
    tiposPermitidos.add(TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO);
    return tiposPermitidos;
  }

  @Override
  public List<NotaCredito> getNotasCreditoPorFactura(Long idFactura) {
    List<NotaCredito> notasCredito = new ArrayList<>();
    Factura factura = facturaService.getFacturaNoEliminadaPorId(idFactura);
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
            if (listaCantidadesProductosUnificados.containsKey(rf.getIdProductoItem())) {
              rf.setCantidad(
                  rf.getCantidad()
                      .subtract(listaCantidadesProductosUnificados.get(rf.getIdProductoItem())));
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
  public TipoDeComprobante[] getTiposNota(Empresa empresa) {
    // cuando la Empresa discrimina IVA
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())) {
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
    Optional<NotaCredito> nc = this.notaCreditoRepository.findById(idNota);
    if (nc.isPresent()) {
      return nc.get().getRenglonesNotaCredito();
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<RenglonNotaDebito> getRenglonesDeNotaDebito(long idNota) {
    Optional<NotaDebito> nd = this.notaDebitoRepository.findById(idNota);
    if (nd.isPresent()) {
      return nd.get().getRenglonesNotaDebito();
    } else {
      return Collections.emptyList();
    }
  }

  private void validarOperacion(Nota nota) {
    if (nota instanceof NotaCredito && nota.getMovimiento().equals(Movimiento.VENTA)) {
      if (nota.getFacturaVenta() != null
          && nota.getFecha().compareTo(nota.getFacturaVenta().getFecha()) <= 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_fecha_incorrecta", null, Locale.getDefault()));
      }
      if (nota.getCAE() != 0L) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_cliente_CAE", null, Locale.getDefault()));
      }
    } else if (nota instanceof NotaCredito && nota.getMovimiento().equals(Movimiento.COMPRA)) {
      if (nota.getFacturaCompra() != null
          && nota.getFecha().compareTo(nota.getFacturaCompra().getFecha()) < 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_fecha_incorrecta", null, Locale.getDefault()));
      }
      if (nota.getFecha().compareTo(new Date()) > 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_fecha_incorrecta", null, Locale.getDefault()));
      }
    }
    if (nota instanceof NotaCredito) {
      if (((NotaCredito) nota).getRenglonesNotaCredito() == null
          || ((NotaCredito) nota).getRenglonesNotaCredito().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_de_renglones_vacio", null, Locale.getDefault()));
      }
    } else {
      if (((NotaDebito) nota).getRenglonesNotaDebito() == null
          || ((NotaDebito) nota).getRenglonesNotaDebito().isEmpty()) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_de_renglones_vacio", null, Locale.getDefault()));
      }
    }
  }

  private void validarCalculosCredito(NotaCredito notaCredito) {
    List<RenglonNotaCredito> renglonesNotaCredito = notaCredito.getRenglonesNotaCredito();
    BigDecimal subTotal = BigDecimal.ZERO;
    BigDecimal[] importes = new BigDecimal[renglonesNotaCredito.size()];
    int i = 0;
    int sizeRenglonesCredito = renglonesNotaCredito.size();
    // IVA - importe
    BigDecimal iva21 = BigDecimal.ZERO;
    BigDecimal iva105 = BigDecimal.ZERO;
    if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_A
        || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_B
        || notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_C
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
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_sub_total_no_valido", null, Locale.getDefault()));
      }
      iva21 =
          this.calcularIVANetoCredito(
              notaCredito.getTipoComprobante(),
              cantidades,
              ivaPorcentajes,
              ivaNetos,
              IVA_21,
              notaCredito.getDescuentoPorcentaje(),
              notaCredito.getRecargoPorcentaje());
      if (notaCredito.getIva21Neto().compareTo(iva21) != 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_iva21_no_valido", null, Locale.getDefault()));
      }
      iva105 =
          this.calcularIVANetoCredito(
              notaCredito.getTipoComprobante(),
              cantidades,
              ivaPorcentajes,
              ivaNetos,
              IVA_105,
              notaCredito.getDescuentoPorcentaje(),
              notaCredito.getRecargoPorcentaje());
      if (notaCredito.getIva105Neto().compareTo(iva105) != 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_iva105_no_valido", null, Locale.getDefault()));
      }
    } else if (notaCredito.getTipoComprobante() == TipoDeComprobante.NOTA_CREDITO_X) {
      for (RenglonNotaCredito r : renglonesNotaCredito) {
        importes[i] = r.getImporteNeto();
        i++;
      }
      subTotal = this.calcularSubTotalCredito(importes);
      if (notaCredito.getSubTotal().compareTo(subTotal) != 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_sub_total_no_valido", null, Locale.getDefault()));
      }
      if (notaCredito.getIva21Neto().compareTo(BigDecimal.ZERO) != 0.0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_iva21_no_valido", null, Locale.getDefault()));
      }
      if (notaCredito.getIva105Neto().compareTo(BigDecimal.ZERO) != 0.0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_iva105_no_valido", null, Locale.getDefault()));
      }
    }
    // DescuentoNeto
    BigDecimal descuentoNeto =
        this.calcularDecuentoNetoCredito(subTotal, notaCredito.getDescuentoPorcentaje());
    if (notaCredito.getDescuentoNeto().compareTo(descuentoNeto) != 0) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_descuento_neto_no_valido", null, Locale.getDefault()));
    }
    // RecargoNeto
    BigDecimal recargoNeto =
        this.calcularRecargoNetoCredito(subTotal, notaCredito.getRecargoPorcentaje());
    if (notaCredito.getRecargoNeto().compareTo(recargoNeto) != 0) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_recargo_neto_no_valido", null, Locale.getDefault()));
    }
    // subTotalBruto
    BigDecimal subTotalBruto =
        this.calcularSubTotalBrutoCredito(
            notaCredito.getTipoComprobante(), subTotal, recargoNeto, descuentoNeto, iva105, iva21);
    if (notaCredito.getSubTotalBruto().compareTo(subTotalBruto) != 0) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_sub_total_bruto_no_valido", null, Locale.getDefault()));
    }
    // Total
    if (notaCredito.getTotal().compareTo(this.calcularTotalCredito(subTotalBruto, iva105, iva21))
        != 0) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_total_no_valido", null, Locale.getDefault()));
    }
  }

  private void validarCalculosDebito(NotaDebito notaDebito) {
    // monto no gravado
    if ((notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_C
            || notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_X)
        && notaDebito.getMontoNoGravado().compareTo(BigDecimal.ZERO) != 0) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_c_x_monto_no_gravado_no_valido", null, Locale.getDefault()));
    } else if ((notaDebito.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_C
        && notaDebito.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_X)) {
      BigDecimal montoComprobante = BigDecimal.ZERO;
      if (notaDebito.getRecibo() != null) {
        montoComprobante = notaDebito.getRecibo().getMonto();
      }
      if (notaDebito.getMontoNoGravado().compareTo(montoComprobante) != 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_monto_no_gravado_no_valido", null, Locale.getDefault()));
      }
    }
    // iva 21
    BigDecimal iva21 = BigDecimal.ZERO;
    if (notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_A
        || notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_B
        || notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_Y
        || notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO) {
      iva21 = notaDebito.getSubTotalBruto().multiply(new BigDecimal("0.21"));
      if (notaDebito.getIva21Neto().compareTo(iva21) != 0) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_iva21_no_valido", null, Locale.getDefault()));
      }
    }
    if ((notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_C
            || notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_X)
        && notaDebito.getIva21Neto().compareTo(BigDecimal.ZERO) != 0) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_iva21_no_valido", null, Locale.getDefault()));
    }
    // total
    if ((notaDebito.getTipoComprobante() == TipoDeComprobante.NOTA_DEBITO_C
        && notaDebito.getSubTotalBruto().compareTo(notaDebito.getTotal()) != 0)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_total_no_valido", null, Locale.getDefault()));
    }
    if (notaDebito.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_C
        && notaDebito.getTipoComprobante() != TipoDeComprobante.NOTA_DEBITO_X
        && notaDebito.getRecibo() != null
        && (notaDebito
                .getTotal()
                .compareTo(
                    this.calcularTotalDebito(
                        notaDebito.getSubTotalBruto(), iva21, notaDebito.getRecibo().getMonto()))
            != 0)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_total_no_valido", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public NotaCredito guardarNotaCredito(@Valid NotaCredito notaCredito) {
    if (notaCredito.getFecha() == null) {
      notaCredito.setFecha(new Date());
    }
    this.validarOperacion(notaCredito);
    if (notaCredito.getMovimiento().equals(Movimiento.VENTA)) {
      if (notaCredito.getFacturaVenta() != null) {
        notaCredito.setTipoComprobante(
            this.getTipoDeNotaCreditoSegunFactura(
                notaCredito.getFacturaVenta().getTipoComprobante()));
      } else {
        if (!this.getTipoNotaCreditoCliente(
                notaCredito.getCliente().getId_Cliente(), notaCredito.getEmpresa().getId_Empresa())
            .contains(notaCredito.getTipoComprobante())) {
          throw new BusinessServiceException(messageSource.getMessage(
            "mensaje_nota_tipo_no_valido", null, Locale.getDefault()));
        }
      }
      notaCredito.setSerie(
          configuracionDelSistemaService
              .getConfiguracionDelSistemaPorEmpresa(notaCredito.getEmpresa())
              .getNroPuntoDeVentaAfip());
      notaCredito.setNroNota(
          this.getSiguienteNumeroNotaCreditoCliente(
              notaCredito.getIdEmpresa(), notaCredito.getTipoComprobante()));
    } else if (notaCredito.getMovimiento().equals(Movimiento.COMPRA)
        && notaCredito.getFacturaCompra() != null) {
      notaCredito.setTipoComprobante(
          this.getTipoDeNotaCreditoSegunFactura(
              notaCredito.getFacturaCompra().getTipoComprobante()));
    } else {
      if (!this.getTipoNotaCreditoProveedor(
              notaCredito.getProveedor().getId_Proveedor(),
              notaCredito.getEmpresa().getId_Empresa())
          .contains(notaCredito.getTipoComprobante())) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_tipo_no_valido", null, Locale.getDefault()));
      }
    }
    if (notaCredito.isModificaStock()) {
      this.actualizarStock(
          notaCredito.getRenglonesNotaCredito(),
          TipoDeOperacion.ALTA,
          notaCredito.getMovimiento(),
          notaCredito.getTipoComprobante());
    }
    this.validarCalculosCredito(notaCredito);
    notaCredito = notaCreditoRepository.save(notaCredito);
    this.cuentaCorrienteService.asentarEnCuentaCorriente(notaCredito, TipoDeOperacion.ALTA);
    logger.warn("La Nota {} se guardó correctamente.", notaCredito);
    return notaCredito;
  }

  @Override
  @Transactional
  public NotaDebito guardarNotaDebito(@Valid NotaDebito notaDebito) {
    if (notaDebito.getFecha() == null) {
      notaDebito.setFecha(new Date());
    }
    this.validarOperacion(notaDebito);
    if (notaDebito.getMovimiento().equals(Movimiento.VENTA)) {
      if (!this.getTipoNotaDebitoCliente(
              notaDebito.getCliente().getId_Cliente(), notaDebito.getEmpresa().getId_Empresa())
          .contains(notaDebito.getTipoComprobante())) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_tipo_no_valido", null, Locale.getDefault()));
      }
      notaDebito.setSerie(
          configuracionDelSistemaService
              .getConfiguracionDelSistemaPorEmpresa(notaDebito.getEmpresa())
              .getNroPuntoDeVentaAfip());
      notaDebito.setNroNota(
          this.getSiguienteNumeroNotaDebitoCliente(
              notaDebito.getIdEmpresa(), notaDebito.getTipoComprobante()));
    } else if (notaDebito.getMovimiento().equals(Movimiento.COMPRA)) {
      if (!this.getTipoNotaDebitoProveedor(
        notaDebito.getProveedor().getId_Proveedor(), notaDebito.getEmpresa().getId_Empresa())
        .contains(notaDebito.getTipoComprobante())) {
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_nota_tipo_no_valido", null, Locale.getDefault()));
      }
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
        if (nota.getFacturaVenta() != null && nota.getFacturaVenta().getCAE() == 0L) {
          throw new BusinessServiceException(messageSource.getMessage(
            "mensaje_nota_factura_relacionada_sin_CAE", null, Locale.getDefault()));
        }
        cliente = nota.getCliente();
      } else {
        cliente = nota.getCliente();
      }
      ComprobanteAFIP comprobante =
          ComprobanteAFIP.builder()
              .idComprobante(nota.getIdNota())
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
      cuentaCorrienteService.updateCAENota(nota.getIdNota(), comprobante.getCAE());
    } else {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_comprobanteAFIP_invalido", null, Locale.getDefault()));
    }
    return nota;
  }

  @Override
  public TipoDeComprobante getTipoDeNotaCreditoSegunFactura(TipoDeComprobante tipo) {
    switch (tipo) {
      case FACTURA_A:
        return TipoDeComprobante.NOTA_CREDITO_A;
      case FACTURA_B:
        return TipoDeComprobante.NOTA_CREDITO_B;
      case FACTURA_C:
        return TipoDeComprobante.NOTA_CREDITO_C;
      case FACTURA_X:
        return TipoDeComprobante.NOTA_CREDITO_X;
      case FACTURA_Y:
        return TipoDeComprobante.NOTA_CREDITO_Y;
      case PRESUPUESTO:
        return TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO;
      default:
        throw new ServiceException(messageSource.getMessage(
          "mensaje_nota_tipo_no_valido", null, Locale.getDefault()));
    }
  }

  private void actualizarStock(
      List<RenglonNotaCredito> renglonesNotaCredito,
      TipoDeOperacion tipoOperacion,
      Movimiento movimiento,
      TipoDeComprobante tipoDeComprobante) {
    HashMap<Long, BigDecimal> idsYCantidades = new HashMap<>();
    renglonesNotaCredito.forEach(r -> idsYCantidades.put(r.getIdProductoItem(), r.getCantidad()));
    productoService.actualizarStock(idsYCantidades, tipoOperacion, movimiento, tipoDeComprobante);
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
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_C)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_X)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_C)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_X)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_PRESUPUESTO)) {
      nota.setSubTotalBruto(nota.getTotal());
      nota.setIva105Neto(BigDecimal.ZERO);
      nota.setIva21Neto(BigDecimal.ZERO);
    }
    if (nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_A)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_B)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_CREDITO_C)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_A)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_B)
        || nota.getTipoComprobante().equals(TipoDeComprobante.NOTA_DEBITO_C)) {
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
    if (nota.getEmpresa().getLogo() != null && !nota.getEmpresa().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(nota.getEmpresa().getLogo()))).getImage());
      } catch (IOException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(messageSource.getMessage(
          "mensaje_empresa_404_logo", null, Locale.getDefault()), ex);
      }
    }
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params, ds));
    } catch (JRException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(messageSource.getMessage(
        "mensaje_error_reporte", null, Locale.getDefault()), ex);
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
  public List<RenglonNotaCredito> calcularRenglonCreditoProducto(
      TipoDeComprobante tipo, BigDecimal[] cantidad, Long[] idRenglonFactura) {
    List<RenglonNotaCredito> renglonesNota = new ArrayList<>();
    RenglonNotaCredito renglonNota;
    if (cantidad.length == idRenglonFactura.length) {
      for (int i = 0; i < idRenglonFactura.length; i++) {
        RenglonFactura renglonFactura = facturaService.getRenglonFactura(idRenglonFactura[i]);
        if (renglonFactura.getCantidad().compareTo(cantidad[i]) < 0
            || cantidad[i].compareTo(BigDecimal.ZERO) < 0) {
          throw new BusinessServiceException(messageSource.getMessage(
            "mensaje_nota_de_credito_cantidad_no_valida", new Object[] {renglonFactura.getDescripcionItem()}, Locale.getDefault()));
        }
        renglonNota = new RenglonNotaCredito();
        renglonNota.setIdProductoItem(renglonFactura.getIdProductoItem());
        renglonNota.setCodigoItem(renglonFactura.getCodigoItem());
        renglonNota.setDescripcionItem(renglonFactura.getDescripcionItem());
        renglonNota.setMedidaItem(renglonFactura.getMedidaItem());
        renglonNota.setCantidad(cantidad[i]);
        renglonNota.setPrecioUnitario(renglonFactura.getPrecioUnitario());
        renglonNota.setDescuentoPorcentaje(renglonFactura.getDescuentoPorcentaje());
        renglonNota.setDescuentoNeto(
            renglonFactura
                .getDescuentoPorcentaje()
                .divide(CIEN, 15, RoundingMode.HALF_UP)
                .multiply(renglonNota.getPrecioUnitario()));
        renglonNota.setGananciaPorcentaje(renglonFactura.getGananciaPorcentaje());
        renglonNota.setGananciaNeto(
            renglonNota
                .getGananciaPorcentaje()
                .divide(CIEN, 15, RoundingMode.HALF_UP)
                .multiply(renglonNota.getPrecioUnitario()));
        renglonNota.setIvaPorcentaje(renglonFactura.getIvaPorcentaje());
        if (tipo.equals(TipoDeComprobante.FACTURA_Y)) {
          renglonNota.setIvaPorcentaje(
              renglonFactura
                  .getIvaPorcentaje()
                  .divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP));
        }
        renglonNota.setIvaNeto(
            (tipo == TipoDeComprobante.NOTA_CREDITO_A
                    || tipo == TipoDeComprobante.NOTA_CREDITO_B
                    || tipo == TipoDeComprobante.NOTA_CREDITO_C
                    || tipo == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO)
                ? renglonFactura.getIvaNeto()
                : BigDecimal.ZERO);
        renglonNota.setImporte(renglonNota.getPrecioUnitario().multiply(cantidad[i]));
        renglonNota.setImporteBruto(
            renglonNota
                .getImporte()
                .subtract(renglonNota.getDescuentoNeto().multiply(cantidad[i])));
        if (tipo == TipoDeComprobante.NOTA_CREDITO_B
            || tipo == TipoDeComprobante.NOTA_CREDITO_C
            || tipo == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) {
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
  public RenglonNotaCredito calcularRenglonCredito(
      TipoDeComprobante tipo, String detalle, BigDecimal monto) {
    this.validarTipoNotaCredito(tipo);
    RenglonNotaCredito renglonNota = new RenglonNotaCredito();
    renglonNota.setIdProductoItem(null);
    renglonNota.setCodigoItem(null);
    renglonNota.setDescripcionItem(detalle);
    renglonNota.setMedidaItem(null);
    renglonNota.setCantidad(BigDecimal.ONE);
    BigDecimal subTotal =
        monto
            .multiply(new BigDecimal("100"))
            .divide(new BigDecimal("121"), 15, RoundingMode.HALF_UP);
    renglonNota.setPrecioUnitario(
        (tipo == TipoDeComprobante.NOTA_CREDITO_B
                || tipo == TipoDeComprobante.NOTA_CREDITO_C
                || tipo == TipoDeComprobante.NOTA_CREDITO_X
                || tipo == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO)
            ? monto
            : subTotal);
    renglonNota.setDescuentoPorcentaje(BigDecimal.ZERO);
    renglonNota.setDescuentoNeto(BigDecimal.ZERO);
    renglonNota.setGananciaPorcentaje(BigDecimal.ZERO);
    renglonNota.setGananciaNeto(BigDecimal.ZERO);
    renglonNota.setIvaPorcentaje(
        (tipo == TipoDeComprobante.NOTA_CREDITO_A
                || tipo == TipoDeComprobante.NOTA_CREDITO_B
                || tipo == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO)
            ? new BigDecimal("21")
            : BigDecimal.ZERO);
    renglonNota.setIvaNeto(
        (tipo == TipoDeComprobante.NOTA_CREDITO_X || tipo == TipoDeComprobante.NOTA_CREDITO_C)
            ? BigDecimal.ZERO
            : monto.subtract(subTotal));
    renglonNota.setImporte(
        (tipo == TipoDeComprobante.NOTA_CREDITO_B
                || tipo == TipoDeComprobante.NOTA_CREDITO_C
                || tipo == TipoDeComprobante.NOTA_CREDITO_X
                || tipo == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO)
            ? monto
            : subTotal);
    renglonNota.setImporteBruto(
        (tipo == TipoDeComprobante.NOTA_CREDITO_B
                || tipo == TipoDeComprobante.NOTA_CREDITO_C
                || tipo == TipoDeComprobante.NOTA_CREDITO_X
                || tipo == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO)
            ? monto
            : subTotal);
    if (tipo == TipoDeComprobante.NOTA_CREDITO_B
        || tipo == TipoDeComprobante.NOTA_CREDITO_C
        || tipo == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) {
      renglonNota.setImporteNeto(renglonNota.getImporteBruto());
    } else {
      renglonNota.setImporteNeto(renglonNota.getImporteBruto().add(renglonNota.getIvaNeto()));
    }
    return renglonNota;
  }

  private void validarTipoNotaCredito(TipoDeComprobante tipo) {
    TipoDeComprobante[] tiposPermitidos = {
      TipoDeComprobante.NOTA_CREDITO_A,
      TipoDeComprobante.NOTA_CREDITO_B,
      TipoDeComprobante.NOTA_CREDITO_C,
      TipoDeComprobante.NOTA_CREDITO_Y,
      TipoDeComprobante.NOTA_CREDITO_X,
      TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO,
    };
    if (!Arrays.asList(tiposPermitidos).contains(tipo)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
    }
  }

  @Override
  public RenglonNotaDebito calcularRenglonDebitoConRecibo(Recibo recibo) {
    RenglonNotaDebito renglonNota = new RenglonNotaDebito();
    String descripcion =
        "Recibo Nº "
            + recibo.getNumRecibo()
            + " "
            + (new FormatterFechaHora(FormatterFechaHora.FORMATO_FECHA_HISPANO))
                .format(recibo.getFecha());
    renglonNota.setDescripcion(descripcion);
    renglonNota.setMonto(recibo.getMonto());
    renglonNota.setImporteBruto(renglonNota.getMonto());
    renglonNota.setIvaPorcentaje(BigDecimal.ZERO);
    renglonNota.setIvaNeto(BigDecimal.ZERO);
    renglonNota.setImporteNeto(recibo.getMonto());
    return renglonNota;
  }

  @Override
  public RenglonNotaDebito calcularRenglonDebito(
      BigDecimal monto, TipoDeComprobante tipoDeComprobante) {
    RenglonNotaDebito renglonNota = new RenglonNotaDebito();
    renglonNota.setDescripcion("Gasto Administrativo");
    switch (tipoDeComprobante) {
      case NOTA_DEBITO_A:
      case NOTA_DEBITO_B:
      case NOTA_DEBITO_PRESUPUESTO:
        renglonNota.setMonto(
            monto.multiply(CIEN).divide(new BigDecimal("121"), 15, RoundingMode.HALF_UP));
        renglonNota.setIvaPorcentaje(IVA_21);
        renglonNota.setIvaNeto(renglonNota.getMonto().multiply(IVA_21.divide(CIEN, 15, RoundingMode.HALF_UP)));
        break;
      case NOTA_DEBITO_C:
      case NOTA_DEBITO_X:
        renglonNota.setMonto(monto);
        renglonNota.setIvaPorcentaje(BigDecimal.ZERO);
        renglonNota.setIvaNeto(BigDecimal.ZERO);
        break;
      case NOTA_DEBITO_Y:
        renglonNota.setMonto(
          monto.multiply(CIEN).divide(new BigDecimal("110.5"), 15, RoundingMode.HALF_UP));
        renglonNota.setIvaPorcentaje(IVA_105);
        renglonNota.setIvaNeto(renglonNota.getMonto().multiply(IVA_105.divide(CIEN, 15, RoundingMode.HALF_UP)));
        break;
      default:
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
    }
    renglonNota.setImporteBruto(renglonNota.getMonto());
    renglonNota.setImporteNeto(renglonNota.getIvaNeto().add(renglonNota.getImporteBruto()));
    return renglonNota;
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
    BigDecimal resultado = BigDecimal.ZERO;
    int indice = cantidades.length;
    for (int i = 0; i < indice; i++) {
      if (ivaPorcentajeRenglones[i].compareTo(ivaPorcentaje) == 0) {
        if (tipoDeComprobante == TipoDeComprobante.NOTA_CREDITO_A || tipoDeComprobante == TipoDeComprobante.NOTA_CREDITO_B
          || tipoDeComprobante == TipoDeComprobante.NOTA_CREDITO_C || tipoDeComprobante == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) {
          resultado = resultado.add(cantidades[i].multiply(ivaNetoRenglones[i]
            .subtract(ivaNetoRenglones[i].multiply(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))
            .add(ivaNetoRenglones[i].multiply(recargoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)))));
        } else {
          resultado = resultado.add(cantidades[i].multiply(ivaNetoRenglones[i]));
        }
      }
    }
    return resultado;
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
    if (tipoDeComprobante == TipoDeComprobante.NOTA_CREDITO_B
        || tipoDeComprobante == TipoDeComprobante.NOTA_CREDITO_C
        || tipoDeComprobante == TipoDeComprobante.NOTA_CREDITO_PRESUPUESTO) {
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
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_fechas_busqueda_invalidas", null, Locale.getDefault()));
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
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_fechas_busqueda_invalidas", null, Locale.getDefault()));
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
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_fechas_busqueda_invalidas", null, Locale.getDefault()));
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
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_nota_fechas_busqueda_invalidas", null, Locale.getDefault()));
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

  @Override
  public boolean existeNotaCreditoAnteriorSinAutorizar(ComprobanteAFIP comprobante) {
    QNotaCredito qNotaCredito = QNotaCredito.notaCredito;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
      qNotaCredito
        .idNota
        .lt(comprobante.getIdComprobante())
        .and(qNotaCredito.eliminada.eq(false))
        .and(qNotaCredito.empresa.id_Empresa.eq(comprobante.getEmpresa().getId_Empresa()))
        .and(qNotaCredito.tipoComprobante.eq(comprobante.getTipoComprobante())));
    Page<NotaCredito> notaAnterior =
      notaCreditoRepository.findAll(
        builder, PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "fecha")));
    return notaAnterior.getContent().get(0).getCAE() == 0L;
  }

  @Override
  public boolean existeNotaDebitoAnteriorSinAutorizar(ComprobanteAFIP comprobante) {
    QNotaDebito qNotaDebito = QNotaDebito.notaDebito;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qNotaDebito
            .idNota
            .lt(comprobante.getIdComprobante())
            .and(qNotaDebito.eliminada.eq(false))
            .and(qNotaDebito.empresa.id_Empresa.eq(comprobante.getEmpresa().getId_Empresa()))
            .and(qNotaDebito.tipoComprobante.eq(comprobante.getTipoComprobante())));
    Page<NotaDebito> notaAnterior =
        notaDebitoRepository.findAll(
            builder, PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "fecha")));
    return notaAnterior.getContent().get(0).getCAE() == 0L;
  }
}
