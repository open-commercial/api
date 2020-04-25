package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.Resultados;
import sic.modelo.dto.*;
import sic.repository.RenglonPedidoRepository;
import sic.service.*;
import sic.repository.PedidoRepository;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.util.CalculosComprobante;
import sic.util.CustomValidator;

@Service
public class PedidoServiceImpl implements IPedidoService {

  private final PedidoRepository pedidoRepository;
  private final RenglonPedidoRepository renglonPedidoRepository;
  private final IFacturaVentaService facturaVentaService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IProductoService productoService;
  private final ICorreoElectronicoService correoElectronicoService;
  private final IConfiguracionSucursalService configuracionSucursal;
  private final ICuentaCorrienteService cuentaCorrienteService;
  private final ModelMapper modelMapper;
  private static final BigDecimal CIEN = new BigDecimal("100");
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  public PedidoServiceImpl(
    PedidoRepository pedidoRepository,
    RenglonPedidoRepository renglonPedidoRepository,
    IFacturaVentaService facturaVentaService,
    IUsuarioService usuarioService,
    IClienteService clienteService,
    IProductoService productoService,
    ICorreoElectronicoService correoElectronicoService,
    IConfiguracionSucursalService configuracionSucursal,
    ICuentaCorrienteService cuentaCorrienteService,
    ModelMapper modelMapper,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.facturaVentaService = facturaVentaService;
    this.pedidoRepository = pedidoRepository;
    this.renglonPedidoRepository = renglonPedidoRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.correoElectronicoService = correoElectronicoService;
    this.configuracionSucursal = configuracionSucursal;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  private void validarOperacion(TipoDeOperacion operacion, Pedido pedido) {
    // Entrada de Datos
    // Validar Estado
    EstadoPedido estado = pedido.getEstado();
    if ((estado != EstadoPedido.ABIERTO)
        && (estado != EstadoPedido.CERRADO)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaja_estado_no_valido", null, Locale.getDefault()));
    }
    // Duplicados
    if (operacion == TipoDeOperacion.ALTA
        && pedidoRepository.findByNroPedidoAndSucursalAndEliminado(
                pedido.getNroPedido(), pedido.getSucursal(), false)
            != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_pedido_duplicado", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && pedidoRepository.findByNroPedidoAndSucursalAndEliminado(
                pedido.getNroPedido(), pedido.getSucursal(), false)
            == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_pedido_no_existente", null, Locale.getDefault()));
    }
    // DetalleEnvío
    if (pedido.getDetalleEnvio() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_pedido_detalle_envio_vacio", null, Locale.getDefault()));
    }
  }

  @Override
  public Pedido getPedidoNoEliminadoPorId(long idPedido) {
    Optional<Pedido> pedido = pedidoRepository
      .findById(idPedido);
    if (pedido.isPresent() && !pedido.get().isEliminado()) {
      return pedido.get();
    } else {
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_pedido_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public Pedido calcularTotalActualDePedido(Pedido pedido) {
    BigDecimal totalActual = BigDecimal.ZERO;
    List<RenglonPedido> renglonesDelPedido = this.getRenglonesDelPedidoOrdenadoPorIdProducto(pedido.getIdPedido());
    List<Long> idsProductos = new ArrayList<>();
    renglonesDelPedido.forEach(r -> idsProductos.add(r.getIdProductoItem()));
    List<Producto> productos = productoService.getMultiplesProductosPorId(idsProductos);
    int i = 0;
    for (RenglonPedido renglonPedido : renglonesDelPedido) {
      BigDecimal precioUnitario = productos.get(i).getPrecioLista();
      renglonPedido.setImporte(
          precioUnitario
              .multiply(renglonPedido.getCantidad())
              .multiply(
                  BigDecimal.ONE.subtract(
                      renglonPedido
                          .getBonificacionPorcentaje()
                          .divide(CIEN, 15, RoundingMode.HALF_UP))));
      totalActual = totalActual.add(renglonPedido.getImporte());
      i++;
    }
    BigDecimal porcentajeDescuento =
        pedido.getDescuentoPorcentaje().divide(CIEN, 2, RoundingMode.HALF_UP);
    BigDecimal porcentajeRecargo =
        pedido.getRecargoPorcentaje().divide(CIEN, 2, RoundingMode.HALF_UP);
    pedido.setTotalActual(
        totalActual
            .subtract(totalActual.multiply(porcentajeDescuento))
            .add(totalActual.multiply(porcentajeRecargo)));
    pedido
        .getCliente()
        .setSaldoCuentaCorriente(cuentaCorrienteService.getSaldoCuentaCorriente(pedido.getCliente().getIdCliente()));
    return pedido;
  }

  @Override
  public long generarNumeroPedido(Sucursal sucursal) {
    long min = 1L;
    long max = 9999999999L; // 10 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      Pedido p = pedidoRepository.findByNroPedidoAndSucursalAndEliminado(randomLong, sucursal, false);
      if (p == null) esRepetido = false;
    }
    return randomLong;
  }

  @Override
  public List<Factura> getFacturasDelPedido(long idPedido) {
    return facturaVentaService.getFacturasDelPedido(idPedido);
  }

  @Override
  @Transactional
  public Pedido guardar(Pedido pedido) {
    BigDecimal importe = BigDecimal.ZERO;
    for (RenglonPedido renglon : pedido.getRenglones()) {
      importe = importe.add(renglon.getImporte()).setScale(5, RoundingMode.HALF_UP);
    }
    BigDecimal recargoNeto =
        importe.multiply(pedido.getRecargoPorcentaje()).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal descuentoNeto =
        importe.multiply(pedido.getDescuentoPorcentaje()).divide(CIEN, 15, RoundingMode.HALF_UP);
    BigDecimal total = importe.add(recargoNeto).subtract(descuentoNeto);
    if (pedido.getCliente().getMontoCompraMinima() != null
        && total.compareTo(pedido.getCliente().getMontoCompraMinima()) < 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_pedido_monto_compra_minima", null, Locale.getDefault()));
    }
    pedido.setSubTotal(importe);
    pedido.setRecargoNeto(recargoNeto);
    pedido.setDescuentoNeto(descuentoNeto);
    pedido.setTotalEstimado(total);
    pedido.setTotalActual(total);
    pedido.setFecha(LocalDateTime.now());
    this.asignarDetalleEnvio(pedido);
    this.calcularCantidadDeArticulos(pedido);
    pedido.setNroPedido(this.generarNumeroPedido(pedido.getSucursal()));
    pedido.setEstado(EstadoPedido.ABIERTO);
    if (pedido.getObservaciones() == null || pedido.getObservaciones().equals("")) {
      pedido.setObservaciones("Los precios se encuentran sujetos a modificaciones.");
    }
    pedido
        .getRenglones()
        .forEach(
            renglonPedido ->
                renglonPedido.setUrlImagenItem(
                    productoService
                        .getProductoNoEliminadoPorId(renglonPedido.getIdProductoItem())
                        .getUrlImagen()));
    this.validarOperacion(TipoDeOperacion.ALTA, pedido);
    Map<Long, BigDecimal> idsYCantidades = new HashMap<>();
    pedido.getRenglones().forEach(p -> idsYCantidades.put(p.getIdProductoItem(), p.getCantidad()));
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ALTA);
    pedido = pedidoRepository.save(pedido);
    logger.warn("El Pedido {} se guardó correctamente.", pedido);
    String emailCliente = pedido.getCliente().getEmail();
    if (emailCliente != null && !emailCliente.isEmpty()) {
      correoElectronicoService.enviarEmail(
          emailCliente,
          "",
          "Nuevo Pedido Ingresado",
          messageSource.getMessage(
              "mensaje_correo_pedido_recibido",
              new Object[] {
                pedido.getCliente().getNombreFiscal(), "Pedido Nº " + pedido.getNroPedido()
              },
              Locale.getDefault()),
          this.getReportePedido(pedido.getIdPedido()),
          "Reporte");
      logger.warn("El mail del pedido nro {} se envió.", pedido.getNroPedido());
    }
    this.calcularTotalActualDePedido(pedido);
    return pedido;
  }

  private void calcularCantidadDeArticulos(Pedido pedido) {
    pedido.setCantidadArticulos(BigDecimal.ZERO);
    pedido
        .getRenglones()
        .forEach(
            r -> pedido.setCantidadArticulos(pedido.getCantidadArticulos().add(r.getCantidad())));
  }

  private void asignarDetalleEnvio(Pedido pedido) {
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.USAR_UBICACION_FACTURACION
        && pedido.getCliente().getUbicacionFacturacion() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault()));
    }
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.USAR_UBICACION_ENVIO
        && pedido.getCliente().getUbicacionEnvio() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_envio_vacia", null, Locale.getDefault()));
    }
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.RETIRO_EN_SUCURSAL
        && !configuracionSucursal
            .getConfiguracionSucursal(pedido.getSucursal())
            .isPuntoDeRetiro()) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_pedido_sucursal_entrega_no_valida", null, Locale.getDefault()));
    }
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.USAR_UBICACION_FACTURACION) {
      pedido.setDetalleEnvio(
          modelMapper.map(pedido.getCliente().getUbicacionFacturacion(), UbicacionDTO.class));
      if (pedido.getCliente().getUbicacionEnvio() == null) {
        pedido
            .getCliente()
            .setUbicacionEnvio(
                this.crearUbicacionNuevaConDatosDeOtraUbicacion(
                    pedido.getCliente().getUbicacionFacturacion()));
      }
    }
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.USAR_UBICACION_ENVIO) {
      pedido.setDetalleEnvio(
          modelMapper.map(pedido.getCliente().getUbicacionEnvio(), UbicacionDTO.class));
      if (pedido.getCliente().getUbicacionFacturacion() == null) {
        pedido
            .getCliente()
            .setUbicacionFacturacion(
                this.crearUbicacionNuevaConDatosDeOtraUbicacion(pedido.getCliente().getUbicacionEnvio()));
      }
    }
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.RETIRO_EN_SUCURSAL) {
      pedido.setDetalleEnvio(
          modelMapper.map(pedido.getSucursal().getUbicacion(), UbicacionDTO.class));
    }
  }

  private Ubicacion crearUbicacionNuevaConDatosDeOtraUbicacion(Ubicacion ubicacion) {
    Ubicacion nuevaUbicacion = new Ubicacion();
    nuevaUbicacion.setLocalidad(ubicacion.getLocalidad());
    nuevaUbicacion.setCalle(ubicacion.getCalle());
    nuevaUbicacion.setDepartamento(ubicacion.getDepartamento());
    nuevaUbicacion.setDescripcion(ubicacion.getDescripcion());
    nuevaUbicacion.setLatitud(ubicacion.getLatitud());
    nuevaUbicacion.setLongitud(ubicacion.getLongitud());
    nuevaUbicacion.setNumero(ubicacion.getNumero());
    nuevaUbicacion.setPiso(ubicacion.getPiso());
    return nuevaUbicacion;
  }

  @Override
  public Page<Pedido> buscarPedidos(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn) {
    Page<Pedido> pedidos =
        pedidoRepository.findAll(
            this.getBuilderPedido(criteria, idUsuarioLoggedIn),
            this.getPageable(
                (criteria.getPagina() == null || criteria.getPagina() < 0)
                    ? 0
                    : criteria.getPagina(),
                criteria.getOrdenarPor(),
                criteria.getSentido()));
    pedidos.getContent().forEach(this::calcularTotalActualDePedido);
    return pedidos;
  }

  private BooleanBuilder getBuilderPedido(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn) {
    QPedido qPedido = QPedido.pedido;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getIdSucursal() != null) {
      builder.and(qPedido.sucursal.idSucursal.eq(criteria.getIdSucursal()));
    }
    if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
        criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        builder.and(qPedido.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
        builder.and(qPedido.fecha.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        builder.and(qPedido.fecha.before(criteria.getFechaHasta()));
      }
    }
    if (criteria.getIdCliente() != null)
      builder.and(qPedido.cliente.idCliente.eq(criteria.getIdCliente()));
    if (criteria.getIdUsuario() != null)
      builder.and(qPedido.usuario.idUsuario.eq(criteria.getIdUsuario()));
    if (criteria.getIdViajante() != null)
      builder.and(qPedido.cliente.viajante.idUsuario.eq(criteria.getIdViajante()));
    if (criteria.getNroPedido() != null) builder.and(qPedido.nroPedido.eq(criteria.getNroPedido()));
    if (criteria.getEstadoPedido() != null)
      builder.and(qPedido.estado.eq(criteria.getEstadoPedido()));
    if (criteria.getTipoDeEnvio() != null)
      builder.and(qPedido.tipoDeEnvio.eq(criteria.getTipoDeEnvio()));
    if (criteria.getIdProducto() != null)
      builder.and(qPedido.renglones.any().idProductoItem.eq(criteria.getIdProducto()));
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn);
    BooleanBuilder rsPredicate = new BooleanBuilder();
    if (!usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
        && !usuarioLogueado.getRoles().contains(Rol.VENDEDOR)
        && !usuarioLogueado.getRoles().contains(Rol.ENCARGADO)) {
      for (Rol rol : usuarioLogueado.getRoles()) {
        switch (rol) {
          case VIAJANTE:
            rsPredicate.or(qPedido.cliente.viajante.eq(usuarioLogueado));
            break;
          case COMPRADOR:
            Cliente clienteRelacionado =
                clienteService.getClientePorIdUsuario(idUsuarioLoggedIn);
            if (clienteRelacionado != null) {
              rsPredicate.or(qPedido.cliente.eq(clienteRelacionado));
            }
            break;
        }
      }
      builder.and(rsPredicate);
    }
    builder.and(qPedido.eliminado.eq(false));
    return builder;
  }

  private Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  @Transactional
  public void actualizar(Pedido pedido, List<RenglonPedido> renglonesAnteriores) {
    //de los renglones, sacar ids y cantidades, array de nuevosResultadosPedido
    BigDecimal[] importesDeRenglones = new BigDecimal[pedido.getRenglones().size()];
    int i = 0;
    for (RenglonPedido renglon : pedido.getRenglones()) {
      importesDeRenglones[i] = renglon.getImporte();
      i++;
    }
    Resultados resultados =
        this.calcularResultadosPedido(
            NuevosResultadosComprobanteDTO.builder()
                .importe(importesDeRenglones)
                .descuentoPorcentaje(
                        pedido.getDescuentoPorcentaje() != null
                        ? pedido.getDescuentoPorcentaje()
                        : BigDecimal.ZERO)
                .recargoPorcentaje(
                        pedido.getRecargoPorcentaje() != null
                        ? pedido.getRecargoPorcentaje()
                        : BigDecimal.ZERO)
                .build());
    pedido.setSubTotal(resultados.getSubTotal());
    pedido.setDescuentoPorcentaje(resultados.getDescuentoPorcentaje());
    pedido.setDescuentoNeto(resultados.getDescuentoNeto());
    pedido.setRecargoPorcentaje(resultados.getRecargoPorcentaje());
    pedido.setRecargoNeto(resultados.getRecargoNeto());
    pedido.setTotalEstimado(resultados.getTotal());
    pedido.setTotalActual(resultados.getTotal());
    this.asignarDetalleEnvio(pedido);
    this.calcularCantidadDeArticulos(pedido);
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, pedido);
    productoService.devolverStockPedido(pedido, TipoDeOperacion.ACTUALIZACION, renglonesAnteriores);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void actualizarFacturasDelPedido(Pedido pedido, List<Factura> facturas) {
    customValidator.validar(pedido);
    pedido.setFacturas(facturas);
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, pedido);
    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public boolean eliminar(long idPedido) {
    Pedido pedido = this.getPedidoNoEliminadoPorId(idPedido);
    if (pedido.getEstado() == EstadoPedido.ABIERTO) {
      pedido.setEliminado(true);
      productoService.actualizarStockPedido(
          pedido, TipoDeOperacion.ELIMINACION);
      pedidoRepository.save(pedido);
    }
    return pedido.isEliminado();
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglon(Long idPedido) {
    return renglonPedidoRepository.findByIdPedidoOrderByIdRenglonPedido(idPedido);
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstado(Long idPedido) {
    List<RenglonPedido> renglonPedidos =
        renglonPedidoRepository.findByIdPedidoOrderByIdRenglonPedidoAndProductoNotEliminado(idPedido);
    Pedido pedido = this.getPedidoNoEliminadoPorId(idPedido);
    if (pedido.getEstado().equals(EstadoPedido.ABIERTO)) {
      long[] idProductoItem = new long[renglonPedidos.size()];
      BigDecimal[] cantidad = new BigDecimal[renglonPedidos.size()];
      for (int i = 0; i < renglonPedidos.size(); i++) {
        idProductoItem[i] = renglonPedidos.get(i).getIdProductoItem();
        cantidad[i] = renglonPedidos.get(i).getCantidad();
      }
      renglonPedidos = this.calcularRenglonesPedido(idProductoItem, cantidad);
    }
    return renglonPedidos;
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedidoOrdenadoPorIdProducto(Long idPedido) {
    return renglonPedidoRepository.findByIdPedidoOrderByIdProductoItem(idPedido);
  }

  @Override
  public Map<Long, BigDecimal> getRenglonesFacturadosDelPedido(long idPedido) {
    List<RenglonFactura> renglonesDeFacturas = new ArrayList<>();
    this.getFacturasDelPedido(idPedido).forEach(f -> renglonesDeFacturas.addAll(f.getRenglones()));
    HashMap<Long, BigDecimal> listaRenglonesUnificados = new HashMap<>();
    if (!renglonesDeFacturas.isEmpty()) {
      renglonesDeFacturas.forEach(
          r -> {
            if (listaRenglonesUnificados.containsKey(r.getIdProductoItem())) {
              listaRenglonesUnificados.put(
                  r.getIdProductoItem(),
                  listaRenglonesUnificados.get(r.getIdProductoItem()).add(r.getCantidad()));
            } else {
              listaRenglonesUnificados.put(r.getIdProductoItem(), r.getCantidad());
            }
          });
    }
    return listaRenglonesUnificados;
  }

  @Override
  public byte[] getReportePedido(long idPedido) {
    ClassLoader classLoader = PedidoServiceImpl.class.getClassLoader();
    InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/Pedido.jasper");
    Map<String, Object> params = new HashMap<>();
    Pedido pedido = this.getPedidoNoEliminadoPorId(idPedido);
    params.put("pedido", pedido);
    if (pedido.getSucursal().getLogo() != null && !pedido.getSucursal().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(pedido.getSucursal().getLogo()))).getImage());
      } catch (IOException ex) {
        throw new ServiceException(messageSource.getMessage(
          "mensaje_sucursal_404_logo", null, Locale.getDefault()), ex);
      }
    }
    String detalleEnvio;
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.RETIRO_EN_SUCURSAL) {
      detalleEnvio = "Retira en Sucursal: " + pedido.getEnvio();
    } else {
      detalleEnvio = pedido.getEnvio();
    }
    params.put("detalleEnvio", detalleEnvio);
    List<RenglonPedido> renglones =
        this.getRenglonesDelPedidoOrdenadorPorIdRenglon(pedido.getIdPedido());
    JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones);
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params, ds));
    } catch (JRException ex) {
      throw new ServiceException(messageSource.getMessage(
        "mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
  }

  @Override
  public RenglonPedido calcularRenglonPedido(long idProducto, BigDecimal cantidad) {
    if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_cantidad_igual_menor_cero", null, Locale.getDefault()));
    }
    RenglonPedido nuevoRenglon = new RenglonPedido();
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    nuevoRenglon.setIdProductoItem(producto.getIdProducto());
    nuevoRenglon.setCantidad(cantidad);
    nuevoRenglon.setCodigoItem(producto.getCodigo());
    nuevoRenglon.setDescripcionItem(producto.getDescripcion());
    nuevoRenglon.setMedidaItem(producto.getMedida().getNombre());
    nuevoRenglon.setPrecioUnitario(producto.getPrecioLista());
    if (producto.isOferta()
        && nuevoRenglon.getCantidad().compareTo(producto.getBulto()) >= 0
        && producto.getPorcentajeBonificacionOferta() != null) {
      nuevoRenglon.setBonificacionPorcentaje(producto.getPorcentajeBonificacionOferta());
      nuevoRenglon.setBonificacionNeta(
          CalculosComprobante.calcularProporcion(
              nuevoRenglon.getPrecioUnitario(), producto.getPorcentajeBonificacionOferta()));
    } else if (nuevoRenglon.getCantidad().compareTo(producto.getBulto()) >= 0) {
      nuevoRenglon.setBonificacionPorcentaje(producto.getPorcentajeBonificacionPrecio());
      nuevoRenglon.setBonificacionNeta(
          CalculosComprobante.calcularProporcion(
              nuevoRenglon.getPrecioUnitario(), producto.getPorcentajeBonificacionPrecio()));
    } else {
      nuevoRenglon.setBonificacionPorcentaje(BigDecimal.ZERO);
      nuevoRenglon.setBonificacionNeta(BigDecimal.ZERO);
    }
    nuevoRenglon.setImporteAnterior(
        CalculosComprobante.calcularImporte(
            nuevoRenglon.getCantidad(), producto.getPrecioLista(), BigDecimal.ZERO));
    nuevoRenglon.setImporte(
        CalculosComprobante.calcularImporte(
            nuevoRenglon.getCantidad(),
            producto.getPrecioLista(),
            nuevoRenglon.getBonificacionNeta()));
    nuevoRenglon.setUrlImagenItem(producto.getUrlImagen());
    nuevoRenglon.setOferta(producto.isOferta());
    return nuevoRenglon;
  }

  @Override
  public List<RenglonPedido> calcularRenglonesPedido(long[] idProductoItem, BigDecimal[] cantidad) {
    if (idProductoItem.length != cantidad.length) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_pedido_renglones_parametros_no_validos", null, Locale.getDefault()));
    }
    List<RenglonPedido> renglones = new ArrayList<>();
    for (int i = 0; i < idProductoItem.length; ++i) {
      renglones.add(this.calcularRenglonPedido(idProductoItem[i], cantidad[i]));
    }
    return renglones;
  }

  @Override
  public Resultados calcularResultadosPedido(NuevosResultadosComprobanteDTO calculoPedido) {
    Resultados resultados = Resultados.builder().build();
    resultados.setDescuentoPorcentaje(
        calculoPedido.getDescuentoPorcentaje() != null
            ? calculoPedido.getDescuentoPorcentaje()
            : BigDecimal.ZERO);
    resultados.setRecargoPorcentaje(
        calculoPedido.getDescuentoPorcentaje() != null
            ? calculoPedido.getRecargoPorcentaje()
            : BigDecimal.ZERO);
    BigDecimal subTotal = BigDecimal.ZERO;
    for (BigDecimal importe: calculoPedido.getImporte()) {
      subTotal = subTotal.add(importe);
    }
    resultados.setSubTotal(subTotal);
    resultados.setDescuentoNeto(resultados.getSubTotal().multiply(resultados.getDescuentoPorcentaje().divide(new BigDecimal("100"), 2, RoundingMode.FLOOR)));
    resultados.setRecargoNeto(resultados.getSubTotal().multiply(resultados.getRecargoPorcentaje().divide(new BigDecimal("100"), 2, RoundingMode.FLOOR)));
    resultados.setSubTotalBruto(resultados.getSubTotal().subtract(resultados.getDescuentoNeto()).add(resultados.getRecargoNeto()));
    resultados.setTotal(resultados.getSubTotalBruto());
    return resultados;
  }

  @Override
  public Pedido getPedidoPorIdPayment(String idPayment) {
    return pedidoRepository.findByIdPaymentAndEliminado(idPayment, false);
  }

  @Scheduled(cron = "30 0 0 * * *")
  public void cerrarPedidosAbiertos() {
    QPedido qPedido = QPedido.pedido;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qPedido.estado.eq(EstadoPedido.ABIERTO)).and(qPedido.eliminado.eq(false));
    Iterable<Pedido> pedidosAbiertos = pedidoRepository.findAll(builder);
    pedidosAbiertos.forEach(
        pedido -> {
          if (pedido.getFecha().isBefore(LocalDateTime.now().minusDays(2L))) {
            pedido.setEstado(EstadoPedido.CERRADO);
            pedidoRepository.save(pedido);
            productoService.actualizarStockPedido(
                pedido, TipoDeOperacion.ACTUALIZACION);
          }
        });
  }
}
