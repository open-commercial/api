package sic.service.impl;

import java.io.IOException;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import sic.modelo.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;
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
import sic.service.*;
import sic.util.CalculosComprobante;
import sic.util.FormatterFechaHora;
import sic.repository.FacturaVentaRepository;
import sic.repository.FacturaCompraRepository;
import sic.repository.FacturaRepository;
import sic.repository.RenglonFacturaRepository;

@Service
public class FacturaServiceImpl implements IFacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaVentaRepository facturaVentaRepository;
    private final FacturaCompraRepository facturaCompraRepository;
    private final RenglonFacturaRepository renglonFacturaRepository;
    private final IProductoService productoService;
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final IPedidoService pedidoService;
    private final INotaService notaService;
    private final ICuentaCorrienteService cuentaCorrienteService;
    private final IAfipService afipService;
    private final IReciboService reciboService;
    private final IUsuarioService usuarioService;
    private final IClienteService clienteService;
    private static final BigDecimal IVA_21 = new BigDecimal("21");
    private static final BigDecimal IVA_105 = new BigDecimal("10.5");
    private static final BigDecimal CIEN = new BigDecimal("100");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  @Lazy
  public FacturaServiceImpl(
      FacturaRepository facturaRepository,
      FacturaVentaRepository facturaVentaRepository,
      FacturaCompraRepository facturaCompraRepository,
      RenglonFacturaRepository renglonFacturaRepository,
      IProductoService productoService,
      IConfiguracionDelSistemaService configuracionDelSistemaService,
      IPedidoService pedidoService,
      INotaService notaService,
      ICuentaCorrienteService cuentaCorrienteService,
      IAfipService afipService,
      IReciboService reciboService,
      IUsuarioService usuarioService,
      IClienteService clienteService) {
    this.facturaRepository = facturaRepository;
    this.facturaVentaRepository = facturaVentaRepository;
    this.facturaCompraRepository = facturaCompraRepository;
    this.renglonFacturaRepository = renglonFacturaRepository;
    this.productoService = productoService;
    this.configuracionDelSistemaService = configuracionDelSistemaService;
    this.pedidoService = pedidoService;
    this.notaService = notaService;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.afipService = afipService;
    this.reciboService = reciboService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
  }

    @Override
    public Factura getFacturaPorId(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura);
        if (factura == null) {
            throw new EntityNotFoundException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_eliminada"));
        }
        return factura;
    }

  @Override
  public List<Factura> getFacturasDelPedido(Long idPedido) {
    return facturaVentaRepository.findAllByPedidoAndEliminada(
        pedidoService.getPedidoPorId(idPedido), false);
  }

  @Override
  public TipoDeComprobante[] getTipoFacturaCompra(Empresa empresa, Proveedor proveedor) {
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())) {
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
  public TipoDeComprobante[] getTipoFacturaVenta(Empresa empresa, Cliente cliente) {
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())) {
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
  public TipoDeComprobante[] getTiposFacturaSegunEmpresa(Empresa empresa) {
    if (CategoriaIVA.discriminaIVA(empresa.getCategoriaIVA())) {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[5];
      tiposPermitidos[0] = TipoDeComprobante.FACTURA_A;
      tiposPermitidos[1] = TipoDeComprobante.FACTURA_B;
      tiposPermitidos[2] = TipoDeComprobante.FACTURA_X;
      tiposPermitidos[3] = TipoDeComprobante.FACTURA_Y;
      tiposPermitidos[4] = TipoDeComprobante.PRESUPUESTO;
      return tiposPermitidos;
    } else {
      TipoDeComprobante[] tiposPermitidos = new TipoDeComprobante[4];
      tiposPermitidos[0] = TipoDeComprobante.FACTURA_C;
      tiposPermitidos[1] = TipoDeComprobante.FACTURA_X;
      tiposPermitidos[2] = TipoDeComprobante.FACTURA_Y;
      tiposPermitidos[3] = TipoDeComprobante.PRESUPUESTO;
      return tiposPermitidos;
    }
  }

    @Override
    public List<RenglonFactura> getRenglonesDeLaFactura(Long idFactura) {
        return this.getFacturaPorId(idFactura).getRenglones();
    }

    @Override
    public List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(Long idFactura) {
        return notaService.getRenglonesFacturaModificadosParaNotaCredito(idFactura);
    }

    @Override
    public RenglonFactura getRenglonFactura(Long idRenglonFactura) {
        return renglonFacturaRepository.findOne(idRenglonFactura);
    }

    @Override
    public Page<FacturaCompra> buscarFacturaCompra(BusquedaFacturaCompraCriteria criteria) {
        //Fecha de Factura
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
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
        return facturaCompraRepository.findAll(this.getBuilderCompra(criteria), criteria.getPageable());
    }

    @Override
    public Page<FacturaVenta> buscarFacturaVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
        //Fecha de Factura
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
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
        return facturaVentaRepository.findAll(this.getBuilderVenta(criteria, idUsuarioLoggedIn), criteria.getPageable());
    }

  private BooleanBuilder getBuilderCompra(BusquedaFacturaCompraCriteria criteria) {
    QFacturaCompra qFacturaCompra = QFacturaCompra.facturaCompra;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qFacturaCompra
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qFacturaCompra.eliminada.eq(false)));
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
      builder.and(qFacturaCompra.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscaPorProveedor())
      builder.and(qFacturaCompra.proveedor.id_Proveedor.eq(criteria.getIdProveedor()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qFacturaCompra.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaPorProducto())
      builder.and(qFacturaCompra.renglones.any().idProductoItem.eq(criteria.getIdProducto()));
    if (criteria.isBuscaPorNumeroFactura())
      builder
          .and(qFacturaCompra.numSerie.eq(criteria.getNumSerie()))
          .and(qFacturaCompra.numFactura.eq(criteria.getNumFactura()));
    return builder;
  }

  private BooleanBuilder getBuilderVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
    QFacturaVenta qFacturaVenta = QFacturaVenta.facturaVenta;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qFacturaVenta
            .empresa
            .id_Empresa
            .eq(criteria.getIdEmpresa())
            .and(qFacturaVenta.eliminada.eq(false)));
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
      builder.and(qFacturaVenta.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscaCliente())
      builder.and(qFacturaVenta.clienteDTO.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.isBuscaPorTipoComprobante())
      builder.and(qFacturaVenta.tipoComprobante.eq(criteria.getTipoComprobante()));
    if (criteria.isBuscaUsuario())
      builder.and(qFacturaVenta.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.isBuscaViajante())
      builder.and(qFacturaVenta.clienteDTO.idViajante.eq(criteria.getIdViajante()));
    if (criteria.isBuscaPorNumeroFactura())
      builder
          .and(qFacturaVenta.numSerie.eq(criteria.getNumSerie()))
          .and(qFacturaVenta.numFactura.eq(criteria.getNumFactura()));
    if (criteria.isBuscarPorPedido())
      builder.and(qFacturaVenta.pedido.nroPedido.eq(criteria.getNroPedido()));
    if (criteria.isBuscaPorProducto())
      builder.and(qFacturaVenta.renglones.any().idProductoItem.eq(criteria.getIdProducto()));
    Usuario usuarioLogueado = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
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
                      qFacturaVenta.clienteDTO.idViajante.eq(usuarioLogueado.getId_Usuario()));
                }
                if (rol == Rol.COMPRADOR) {
                  Cliente clienteRelacionado =
                      clienteService.getClientePorIdUsuarioYidEmpresa(
                          idUsuarioLoggedIn, criteria.getIdEmpresa());
                  if (clienteRelacionado != null) {
                    rsPredicate.or(
                        qFacturaVenta.clienteDTO.id_Cliente.eq(clienteRelacionado.getId_Cliente()));
                  } else {
                    rsPredicate.or(qFacturaVenta.clienteDTO.isNull());
                  }
                }
              });
      builder.and(rsPredicate);
    }
    return builder;
  }

    private Factura procesarFactura(Factura factura) {
        factura.setEliminada(false);
        if (factura instanceof FacturaVenta) {
            factura.setFecha(new Date());
            factura.setNumSerie(configuracionDelSistemaService
                    .getConfiguracionDelSistemaPorEmpresa(factura.getEmpresa()).getNroPuntoDeVentaAfip());
            factura.setNumFactura(this.calcularNumeroFacturaVenta(factura.getTipoComprobante(),
                    factura.getNumSerie(), factura.getEmpresa().getId_Empresa()));
        }
        this.validarFactura(factura);
        return factura;
    }

  @Override
  @Transactional
  public List<FacturaVenta> guardar(
      List<FacturaVenta> facturas, Long idPedido, List<Recibo> recibos) {
    List<FacturaVenta> facturasProcesadas = new ArrayList<>();
    facturas.forEach(
        f ->
            productoService.actualizarStock(
                this.getIdsProductosYCantidades(f),
                TipoDeOperacion.ALTA,
                Movimiento.VENTA,
                f.getTipoComprobante()));
    if (idPedido != null) {
      Pedido pedido = pedidoService.getPedidoPorId(idPedido);
      facturas.forEach(f -> f.setPedido(pedido));
      for (Factura f : facturas) {
        FacturaVenta facturaGuardada =
            facturaVentaRepository.save((FacturaVenta) this.procesarFactura(f));
        this.cuentaCorrienteService.asentarEnCuentaCorriente(facturaGuardada);
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
      for (Factura f : facturas) {
        FacturaVenta facturaGuardada;
        facturaGuardada = facturaVentaRepository.save((FacturaVenta) this.procesarFactura(f));
        this.cuentaCorrienteService.asentarEnCuentaCorriente(facturaGuardada);
        facturasProcesadas.add(facturaGuardada);
        logger.warn("La Factura {} se guardó correctamente.", facturaGuardada);
        if (recibos != null) {
          recibos.forEach(reciboService::guardar);
        }
      }
    }
    return facturasProcesadas;
  }

  @Override
  @Transactional
  public List<FacturaCompra> guardar(List<FacturaCompra> facturas) {
    List<FacturaCompra> facturasProcesadas = new ArrayList<>();
    facturas.forEach(
        f ->
            productoService.actualizarStock(
                this.getIdsProductosYCantidades(f),
                TipoDeOperacion.ALTA,
                Movimiento.COMPRA,
                f.getTipoComprobante()));
    for (Factura f : facturas) {
      FacturaCompra facturaGuardada = null;
      if (f instanceof FacturaCompra) {
        facturaGuardada = facturaCompraRepository.save((FacturaCompra) this.procesarFactura(f));
        this.cuentaCorrienteService.asentarEnCuentaCorriente(facturaGuardada);
      }
      facturasProcesadas.add(facturaGuardada);
    }
    return facturasProcesadas;
  }

    private Map<Long, BigDecimal> getIdsProductosYCantidades(Factura factura) {
        Map<Long, BigDecimal> idsYCantidades = new HashMap<>();
        factura.getRenglones().forEach(r ->
            idsYCantidades.put(r.getIdProductoItem(), r.getCantidad()));
        return idsYCantidades;
    }

    private void validarFactura(Factura factura) {
        //Entrada de Datos
        if (factura.getFechaVencimiento() != null) {
            Calendar calFechaVencimiento = new GregorianCalendar();
            calFechaVencimiento.setTime(factura.getFechaVencimiento());
            calFechaVencimiento.set(Calendar.HOUR, 0);
            calFechaVencimiento.set(Calendar.MINUTE, 0);
            calFechaVencimiento.set(Calendar.SECOND, 0);
            calFechaVencimiento.set(Calendar.MILLISECOND, 0);
            Calendar calFechaFactura = new GregorianCalendar();
            calFechaFactura.setTime(factura.getFecha());
            calFechaFactura.set(Calendar.HOUR, 0);
            calFechaFactura.set(Calendar.MINUTE, 0);
            calFechaFactura.set(Calendar.SECOND, 0);
            calFechaFactura.set(Calendar.MILLISECOND, 0);
          if (calFechaFactura.getTime().compareTo(calFechaVencimiento.getTime()) > 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_factura_fecha_invalida"));
          }
        }
        //Requeridos
        if (factura.getFecha() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fecha_vacia"));
        }
        if (factura.getTipoComprobante() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_tipo_factura_vacia"));
        }
        if (factura.getTransportista() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_transportista_vacio"));
        }
        if (factura.getRenglones() == null || factura.getRenglones().isEmpty()) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_renglones_vacio"));
        }
        if (factura.getEmpresa() == null) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_empresa_vacia"));
        }
        if (factura instanceof FacturaCompra) {
            FacturaCompra facturaCompra = (FacturaCompra) factura;
            if (facturaCompra.getProveedor() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_proveedor_vacio"));
            }
          if (factura.getFecha().compareTo(new Date()) > 0) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_factura_compra_fecha_incorrecta"));
          }
        }
        if (factura instanceof FacturaVenta) {
            FacturaVenta facturaVenta = (FacturaVenta) factura;
            if (facturaVenta.getClienteDTO() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_cliente_vacio"));
            }
            if (facturaVenta.getUsuario() == null) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_usuario_vacio"));
            }
            if (facturaVenta.getCAE() != 0L) {
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_factura_venta_CAE"));
            }
        }
    // Calculos
    // SubTotal
    BigDecimal[] importes = new BigDecimal[factura.getRenglones().size()];
    int i = 0;
    for (RenglonFactura renglon : factura.getRenglones()) {
      importes[i] = renglon.getImporte();
      i++;
    }
    if (factura.getSubTotal().compareTo(CalculosComprobante.calcularSubTotal(importes)) != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_factura_sub_total_no_valido"));
    }
    // SubTotalBruto
    if (factura
            .getSubTotalBruto()
            .compareTo(
                CalculosComprobante.calcularSubTotalBruto(
                    (factura.getTipoComprobante() == TipoDeComprobante.FACTURA_B
                        || factura.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO),
                    factura.getSubTotal(),
                    factura.getRecargoNeto(),
                    factura.getDescuentoNeto(),
                    factura.getIva105Neto(),
                    factura.getIva21Neto()))
        != 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_factura_sub_total_bruto_no_valido"));
    }
    // IVA
    i = 0;
    if (factura.getTipoComprobante() == TipoDeComprobante.FACTURA_A
        || factura.getTipoComprobante() == TipoDeComprobante.FACTURA_B
        || factura.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO) {
      BigDecimal[] ivaPorcentajes = new BigDecimal[factura.getRenglones().size()];
      BigDecimal[] ivaNetos = new BigDecimal[factura.getRenglones().size()];
      BigDecimal[] cantidades = new BigDecimal[factura.getRenglones().size()];
      for (RenglonFactura renglon : factura.getRenglones()) {
        ivaPorcentajes[i] = renglon.getIvaPorcentaje();
        ivaNetos[i] = renglon.getIvaNeto();
        cantidades[i] = renglon.getCantidad();
        i++;
      }
      BigDecimal ivaNeto21 =
          this.calcularIvaNetoFactura(
              factura.getTipoComprobante(),
              cantidades,
              ivaPorcentajes,
              ivaNetos,
              IVA_21,
              factura.getDescuentoPorcentaje(),
              factura.getRecargoPorcentaje());
      BigDecimal ivaNeto105 =
          this.calcularIvaNetoFactura(
              factura.getTipoComprobante(),
              cantidades,
              ivaPorcentajes,
              ivaNetos,
              IVA_105,
              factura.getDescuentoPorcentaje(),
              factura.getRecargoPorcentaje());
      if (factura.getIva21Neto().compareTo(ivaNeto21) != 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_factura_iva21_no_valido"));
      }
      if (factura.getIva105Neto().compareTo(ivaNeto105) != 0) {
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_factura_iva105_no_valido"));
      }
    }
    if ((factura.getTipoComprobante() == TipoDeComprobante.FACTURA_X
            || factura.getTipoComprobante() == TipoDeComprobante.FACTURA_C)
        && (factura.getIva21Neto().compareTo(BigDecimal.ZERO) != 0
            || factura.getIva105Neto().compareTo(BigDecimal.ZERO) != 0)) {
        throw new BusinessServiceException(
                ResourceBundle.getBundle("Mensajes").getString("mensaje_factura_iva_no_valido"));
    }
    // Total
    BigDecimal total =
        CalculosComprobante.calcularTotal(
            factura.getSubTotalBruto(), factura.getIva105Neto(), factura.getIva21Neto());
    if (factura.getTotal().compareTo(total) != 0
        || factura.getTotal().compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_factura_total_no_valido"));
    }
    }

    @Override
    @Transactional
    public FacturaVenta autorizarFacturaVenta(FacturaVenta fv) {
    Cliente cliente = clienteService.getClientePorId(fv.getIdCliente());
    ComprobanteAFIP comprobante = ComprobanteAFIP.builder()
                .fecha(fv.getFecha())
                .tipoComprobante(fv.getTipoComprobante())
                .CAE(fv.getCAE())
                .vencimientoCAE(fv.getVencimientoCAE())
                .numSerieAfip(fv.getNumSerieAfip())
                .numFacturaAfip(fv.getNumFacturaAfip())
                .empresa(fv.getEmpresa())
                .cliente(cliente)
                .subtotalBruto(fv.getSubTotalBruto())
                .iva105neto(fv.getIva105Neto())
                .iva21neto(fv.getIva21Neto())
                .montoNoGravado(BigDecimal.ZERO)
                .total(fv.getTotal())
                .build();
        afipService.autorizar(comprobante);
        fv.setCAE(comprobante.getCAE());
        fv.setVencimientoCAE(comprobante.getVencimientoCAE());
        fv.setNumSerieAfip(comprobante.getNumSerieAfip());
        fv.setNumFacturaAfip(comprobante.getNumFacturaAfip());
        cuentaCorrienteService.updateCAEFactura(fv.getId_Factura(), comprobante.getCAE());
        return fv;
    }

    @Override
    public BigDecimal calcularTotalFacturadoVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
        //Fecha de Factura
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
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
        BigDecimal totalFacturado = facturaVentaRepository.calcularTotalFacturadoVenta(this.getBuilderVenta(criteria,idUsuarioLoggedIn));
        return (totalFacturado != null? totalFacturado : BigDecimal.ZERO);
    }

    @Override
    public BigDecimal  calcularTotalFacturadoCompra(BusquedaFacturaCompraCriteria criteria) {
        //Fecha de Factura
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
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
      BigDecimal totalFacturado = facturaCompraRepository.calcularTotalFacturadoCompra(this.getBuilderCompra(criteria));
      return (totalFacturado != null? totalFacturado : BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calcularIvaVenta(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
        //Fecha de Factura
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
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
        TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B};
        BigDecimal ivaVenta = facturaVentaRepository.calcularIVAVenta(this.getBuilderVenta(criteria, idUsuarioLoggedIn), tipoFactura);
        return (ivaVenta != null? ivaVenta : BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calcularIvaCompra(BusquedaFacturaCompraCriteria criteria) {
        //Fecha de Factura
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
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
        TipoDeComprobante[] tipoFactura = {TipoDeComprobante.FACTURA_A};
      BigDecimal ivaCompra = facturaCompraRepository.calcularIVACompra(this.getBuilderCompra(criteria), tipoFactura);
      return (ivaCompra != null? ivaCompra : BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calcularGananciaTotal(BusquedaFacturaVentaCriteria criteria, long idUsuarioLoggedIn) {
        //Fecha de Factura
        if (criteria.isBuscaPorFecha() && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
            throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_factura_fechas_busqueda_invalidas"));
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
        BigDecimal gananciaTotal = facturaVentaRepository.calcularGananciaTotal(this.getBuilderVenta(criteria,idUsuarioLoggedIn));
        return (gananciaTotal != null? gananciaTotal : BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calcularIVANetoRenglon(Movimiento movimiento, TipoDeComprobante tipo, Producto producto, BigDecimal descuentoPorcentaje) {
        BigDecimal resultado = BigDecimal.ZERO;
        if (movimiento == Movimiento.COMPRA) {
            if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B) {
                resultado = producto.getPrecioCosto()
                        .multiply(BigDecimal.ONE.subtract(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP))
                                .multiply(producto.getIvaPorcentaje()
                                        .divide(CIEN, 15, RoundingMode.HALF_UP)));
            }
        } else if (movimiento == Movimiento.VENTA) {
            if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B || tipo == TipoDeComprobante.PRESUPUESTO) {
                resultado = producto.getPrecioVentaPublico()
                        .multiply(BigDecimal.ONE.subtract(descuentoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP))
                                .multiply(producto.getIvaPorcentaje()
                                        .divide(CIEN, 15, RoundingMode.HALF_UP)));
            }
        }
        return resultado;
    }

    @Override
    public BigDecimal calcularIvaNetoFactura(TipoDeComprobante tipo, BigDecimal[] cantidades, BigDecimal[] ivaPorcentajeRenglones,
            BigDecimal[] ivaNetoRenglones, BigDecimal ivaPorcentaje, BigDecimal descuentoPorcentaje, BigDecimal recargoPorcentaje) {
        BigDecimal resultado = BigDecimal.ZERO;
        int indice = cantidades.length;
        for (int i = 0; i < indice; i++) {
            if (ivaPorcentajeRenglones[i].compareTo(ivaPorcentaje) == 0) {
                if (tipo == TipoDeComprobante.FACTURA_A || tipo == TipoDeComprobante.FACTURA_B
                        || tipo == TipoDeComprobante.FACTURA_C || tipo == TipoDeComprobante.PRESUPUESTO) {
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
    public BigDecimal calcularPrecioUnitario(Movimiento movimiento, TipoDeComprobante tipoDeComprobante, Producto producto) {
        BigDecimal ivaResultado;
        BigDecimal resultado = BigDecimal.ZERO;
        if (movimiento == Movimiento.COMPRA) {
            if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_A) || tipoDeComprobante.equals(TipoDeComprobante.FACTURA_X)) {
                resultado = producto.getPrecioCosto();
            } else {
                ivaResultado = producto.getPrecioCosto().multiply(producto.getIvaPorcentaje()).divide(CIEN, 15, RoundingMode.HALF_UP);
                resultado = producto.getPrecioCosto().add(ivaResultado);
            }
        }
        if (movimiento == Movimiento.VENTA) {
            switch (tipoDeComprobante) {
                case FACTURA_A:
                case FACTURA_X:
                    resultado = producto.getPrecioVentaPublico();
                    break;
                case FACTURA_Y:
                    ivaResultado = producto.getIvaPorcentaje().divide(CIEN, 15, RoundingMode.HALF_UP).divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP).multiply(producto.getPrecioVentaPublico());
                    resultado = producto.getPrecioVentaPublico().add(ivaResultado);
                    break;
                default:
                    resultado = producto.getPrecioLista();
                    break;
            }
        }
        if (movimiento == Movimiento.PEDIDO) {
            resultado = producto.getPrecioLista();
        }
        return resultado;
    }

    @Override
    public long calcularNumeroFacturaVenta(TipoDeComprobante tipoDeComprobante, long serie, long idEmpresa) {
        Long numeroFactura = facturaVentaRepository.buscarMayorNumFacturaSegunTipo(tipoDeComprobante, serie, idEmpresa);
        if (numeroFactura == null) {
            return 1; // No existe ninguna Factura anterior
        } else {
            return 1 + numeroFactura;
        }
    }

    @Override
    public byte[] getReporteFacturaVenta(Factura factura) {
        ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
        InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/FacturaVenta.jasper");
        Map<String, Object> params = new HashMap<>();
        ConfiguracionDelSistema cds = configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(factura.getEmpresa());
        params.put("preImpresa", cds.isUsarFacturaVentaPreImpresa());
        if (factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_B) || factura.getTipoComprobante().equals(TipoDeComprobante.PRESUPUESTO)) {
            factura.setSubTotalBruto(factura.getTotal());
            factura.setIva105Neto(BigDecimal.ZERO);
            factura.setIva21Neto(BigDecimal.ZERO);
        }
        params.put("facturaVenta", factura);
        if (factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_A) || factura.getTipoComprobante().equals(TipoDeComprobante.FACTURA_B)
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
        if (factura.getEmpresa().getLogo() != null && !factura.getEmpresa().getLogo().isEmpty()) {
            try {
                params.put("logo", new ImageIcon(ImageIO.read(new URL(factura.getEmpresa().getLogo()))).getImage());
            } catch (IOException ex) {
                logger.error(ex.getMessage());
                throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_empresa_404_logo"), ex);
            }
        }
        List<RenglonFactura> renglones = this.getRenglonesDeLaFactura(factura.getId_Factura());
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones);
         try {
            return JasperExportManager.exportReportToPdf(JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
            logger.error(ex.getMessage());
            throw new ServiceException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_reporte"), ex);
        }
    }

  @Override
  public List<RenglonFactura> getRenglonesPedidoParaFacturar(
      long idPedido, TipoDeComprobante tipoDeComprobante) {
    List<RenglonFactura> renglonesRestantes = new ArrayList<>();
    List<RenglonPedido> renglonesPedido = pedidoService.getRenglonesDelPedido(idPedido);
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
                    this.calcularRenglon(
                        tipoDeComprobante,
                        Movimiento.VENTA,
                        r.getCantidad()
                            .subtract(renglonesDeFacturas.get(r.getIdProductoItem()).getCantidad()),
                        r.getIdProductoItem(),
                        r.getDescuentoPorcentaje(),
                        false));
              }
            } else {
              renglonesRestantes.add(
                  this.calcularRenglon(
                      tipoDeComprobante,
                      Movimiento.VENTA,
                      r.getCantidad(),
                      r.getIdProductoItem(),
                      r.getDescuentoPorcentaje(),
                      false));
            }
          });
    } else {
      renglonesPedido.forEach(
          r ->
              renglonesRestantes.add(
                  this.calcularRenglon(
                      tipoDeComprobante,
                      Movimiento.VENTA,
                      r.getCantidad(),
                      r.getIdProductoItem(),
                      r.getDescuentoPorcentaje(),
                      false)));
    }
    return renglonesRestantes;
  }

  @Override
  public boolean pedidoTotalmenteFacturado(Pedido pedido) {
        boolean facturado = false;
        Map<Long, RenglonFactura> renglonesDeFacturas = pedidoService.getRenglonesFacturadosDelPedido(pedido.getId_Pedido());
        if (!renglonesDeFacturas.isEmpty()) {
            for (RenglonPedido r : pedido.getRenglones()) {
                if (renglonesDeFacturas.containsKey(r.getIdProductoItem())) {
                    facturado = (r.getCantidad().compareTo(renglonesDeFacturas.get(r.getIdProductoItem()).getCantidad()) < 1);
                } else {
                    return false;
                }
            }
        }
        return facturado;
    }

  @Override
  public RenglonFactura calcularRenglon(
      TipoDeComprobante tipoDeComprobante,
      Movimiento movimiento,
      BigDecimal cantidad,
      long idProducto,
      BigDecimal descuentoPorcentaje,
      boolean dividiendoRenglonFactura) {
    Producto producto = productoService.getProductoPorId(idProducto);
    /*if (dividiendoRenglonFactura == false && cantidad < producto.getBulto()
            && (movimiento == Movimiento.VENTA || movimiento == Movimiento.PEDIDO)) {
        throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                .getString("mensaje_producto_cantidad_menor_a_minima"));
    }*/
    RenglonFactura nuevoRenglon = new RenglonFactura();
    nuevoRenglon.setIdProductoItem(producto.getIdProducto());
    nuevoRenglon.setCodigoItem(producto.getCodigo());
    nuevoRenglon.setDescripcionItem(producto.getDescripcion());
    nuevoRenglon.setMedidaItem(producto.getMedida().getNombre());
    nuevoRenglon.setCantidad(cantidad);
    nuevoRenglon.setPrecioUnitario(
        this.calcularPrecioUnitario(movimiento, tipoDeComprobante, producto));
    if (descuentoPorcentaje.compareTo(CIEN) > 0) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_descuento_mayor_cien"));
    }
    nuevoRenglon.setDescuentoPorcentaje(descuentoPorcentaje);
    nuevoRenglon.setDescuentoNeto(
        CalculosComprobante.calcularProporcion(
            nuevoRenglon.getPrecioUnitario(), descuentoPorcentaje));
    nuevoRenglon.setIvaPorcentaje(producto.getIvaPorcentaje());
    if (tipoDeComprobante.equals(TipoDeComprobante.FACTURA_Y)) {
      nuevoRenglon.setIvaPorcentaje(
          producto.getIvaPorcentaje().divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP));
    }
    nuevoRenglon.setIvaNeto(
        this.calcularIVANetoRenglon(
            movimiento, tipoDeComprobante, producto, nuevoRenglon.getDescuentoPorcentaje()));
    nuevoRenglon.setGananciaPorcentaje(producto.getGananciaPorcentaje());
    nuevoRenglon.setGananciaNeto(producto.getGananciaNeto());
    nuevoRenglon.setImporte(
        CalculosComprobante.calcularImporte(
            cantidad, nuevoRenglon.getPrecioUnitario(), nuevoRenglon.getDescuentoNeto()));
    return nuevoRenglon;
  }

    @Override
    public List<FacturaVenta> dividirFactura(FacturaVenta facturaADividir, int[] indices) {
        FacturaVenta facturaSinIVA = new FacturaVenta();
        facturaSinIVA.setClienteDTO(facturaADividir.getClienteDTO());
        facturaSinIVA.setUsuario(facturaADividir.getUsuario());
        facturaSinIVA.setPedido(facturaADividir.getPedido());
        facturaSinIVA.setDescuentoPorcentaje(facturaADividir.getDescuentoPorcentaje());
        facturaSinIVA.setRecargoPorcentaje(facturaADividir.getRecargoPorcentaje());
        FacturaVenta facturaConIVA = new FacturaVenta();
        facturaConIVA.setClienteDTO(facturaADividir.getClienteDTO());
        facturaConIVA.setUsuario(facturaADividir.getUsuario());
        facturaConIVA.setPedido(facturaADividir.getPedido());
        facturaConIVA.setTipoComprobante(facturaADividir.getTipoComprobante());
        facturaConIVA.setDescuentoPorcentaje(facturaADividir.getDescuentoPorcentaje());
        facturaConIVA.setRecargoPorcentaje(facturaADividir.getRecargoPorcentaje());
        List<FacturaVenta> facturas = new ArrayList<>();
        this.agregarRenglonesAFacturaSinIVA(facturaSinIVA, indices, facturaADividir.getRenglones());
        this.agregarRenglonesAFacturaConIVA(facturaConIVA, indices,facturaADividir.getRenglones());
        if (!facturaSinIVA.getRenglones().isEmpty()) {
            this.procesarFacturaSinIVA(facturaADividir, facturaSinIVA);
            facturas.add(facturaSinIVA);
        }
        this.procesarFacturaConIVA(facturaADividir, facturaConIVA);
        facturas.add(facturaConIVA);
        return facturas;
    }

    private FacturaVenta procesarFacturaSinIVA(FacturaVenta facturaADividir, FacturaVenta facturaSinIVA) {
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
        facturaSinIVA.setDescuentoNeto(CalculosComprobante.calcularProporcion(facturaSinIVA.getSubTotal(), facturaSinIVA.getDescuentoPorcentaje()));
        facturaSinIVA.setRecargoNeto(CalculosComprobante.calcularProporcion(facturaSinIVA.getSubTotal(), facturaSinIVA.getRecargoPorcentaje()));
        facturaSinIVA.setIva105Neto(this.calcularIvaNetoFactura(facturaSinIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_105, facturaADividir.getDescuentoPorcentaje(), facturaADividir.getRecargoPorcentaje()));
        facturaSinIVA.setIva21Neto(this.calcularIvaNetoFactura(facturaSinIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_21, facturaADividir.getDescuentoPorcentaje(), facturaADividir.getRecargoPorcentaje()));
        facturaSinIVA.setSubTotalBruto(CalculosComprobante.calcularSubTotalBruto((facturaSinIVA.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || facturaSinIVA.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO), facturaSinIVA.getSubTotal(),
                facturaSinIVA.getRecargoNeto(), facturaSinIVA.getDescuentoNeto(), facturaSinIVA.getIva105Neto(), facturaSinIVA.getIva21Neto()));
        facturaSinIVA.setTotal(CalculosComprobante.calcularTotal(facturaSinIVA.getSubTotalBruto(), facturaSinIVA.getIva105Neto(), facturaSinIVA.getIva21Neto()));
        facturaSinIVA.setObservaciones(facturaADividir.getObservaciones());
        facturaSinIVA.setEmpresa(facturaADividir.getEmpresa());
        facturaSinIVA.setEliminada(facturaADividir.isEliminada());
        return facturaSinIVA;
    }

    private FacturaVenta procesarFacturaConIVA(FacturaVenta facturaADividir, FacturaVenta facturaConIVA) {
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
        facturaConIVA.setDescuentoNeto(CalculosComprobante.calcularProporcion(facturaConIVA.getSubTotal(), facturaConIVA.getDescuentoPorcentaje()));
        facturaConIVA.setRecargoNeto(CalculosComprobante.calcularProporcion(facturaConIVA.getSubTotal(), facturaConIVA.getRecargoPorcentaje()));
        facturaConIVA.setIva105Neto(this.calcularIvaNetoFactura(facturaConIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_105, facturaADividir.getDescuentoPorcentaje(), facturaADividir.getRecargoPorcentaje()));
        facturaConIVA.setIva21Neto(this.calcularIvaNetoFactura(facturaConIVA.getTipoComprobante(), cantidades,
                ivaPorcentajeRenglones, ivaNetoRenglones, IVA_21, facturaADividir.getDescuentoPorcentaje(), facturaADividir.getRecargoPorcentaje()));
        facturaConIVA.setSubTotalBruto(CalculosComprobante.calcularSubTotalBruto((facturaConIVA.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || facturaConIVA.getTipoComprobante() == TipoDeComprobante.PRESUPUESTO), facturaConIVA.getSubTotal(),
                facturaConIVA.getRecargoNeto(), facturaConIVA.getDescuentoNeto(), facturaConIVA.getIva105Neto(), facturaConIVA.getIva21Neto()));
        facturaConIVA.setTotal(CalculosComprobante.calcularTotal(facturaConIVA.getSubTotalBruto(), facturaConIVA.getIva105Neto(), facturaConIVA.getIva21Neto()));
        facturaConIVA.setObservaciones(facturaADividir.getObservaciones());
        facturaConIVA.setEmpresa(facturaADividir.getEmpresa());
        facturaConIVA.setEliminada(facturaADividir.isEliminada());
        return facturaConIVA;
    }

  private FacturaVenta agregarRenglonesAFacturaSinIVA(
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
            this.calcularRenglon(
                TipoDeComprobante.FACTURA_X,
                Movimiento.VENTA,
                cantidadProductosRenglonFacturaSinIVA,
                renglon.getIdProductoItem(),
                renglon.getDescuentoPorcentaje(),
                true);
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
    return facturaSinIVA;
  }

  private FacturaVenta agregarRenglonesAFacturaConIVA(
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
              crearRenglonConIVA(
                  facturaConIVA.getTipoComprobante(),
                  cantidadProductosRenglonFacturaConIVA,
                  renglon.getIdProductoItem(),
                  renglon.getDescuentoPorcentaje(),
                  true));
          renglonMarcado++;
          numeroDeRenglon++;
        } else {
          numeroDeRenglon++;
          renglonesConIVA.add(
              crearRenglonConIVA(
                  facturaConIVA.getTipoComprobante(),
                  renglon.getCantidad(),
                  renglon.getIdProductoItem(),
                  renglon.getDescuentoPorcentaje(),
                  false));
        }
      } else {
        numeroDeRenglon++;
        renglonesConIVA.add(
            crearRenglonConIVA(
                facturaConIVA.getTipoComprobante(),
                renglon.getCantidad(),
                renglon.getIdProductoItem(),
                renglon.getDescuentoPorcentaje(),
                false));
      }
    }
    facturaConIVA.setRenglones(renglonesConIVA);
    return facturaConIVA;
  }

  private RenglonFactura crearRenglonConIVA(
      TipoDeComprobante tipoDeComprobante,
      BigDecimal cantidad,
      long idProductoItem,
      BigDecimal descuentoPorcentaje,
      boolean dividir) {
    return this.calcularRenglon(
        tipoDeComprobante,
        Movimiento.VENTA,
        cantidad,
        idProductoItem,
        descuentoPorcentaje,
        dividir);
  }
}
