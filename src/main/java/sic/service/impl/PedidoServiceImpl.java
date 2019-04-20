package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.MessageFormat;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.*;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.RenglonPedidoRepository;
import sic.service.*;
import sic.repository.PedidoRepository;
import sic.util.CalculosComprobante;
import sic.util.FormatterFechaHora;

@Service
public class PedidoServiceImpl implements IPedidoService {

  private final PedidoRepository pedidoRepository;
  private final RenglonPedidoRepository renglonPedidoRepository;
  private final IFacturaService facturaService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IProductoService productoService;
  private final ICorreoElectronicoService correoElectronicoService;
  private final IEmpresaService empresaService;
  private final ModelMapper modelMapper;
  private static final BigDecimal CIEN = new BigDecimal("100");
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");

  @Autowired
  public PedidoServiceImpl(
      PedidoRepository pedidoRepository,
      RenglonPedidoRepository renglonPedidoRepository,
      IFacturaService facturaService,
      IUsuarioService usuarioService,
      IClienteService clienteService,
      IProductoService productoService,
      ICorreoElectronicoService correoElectronicoService,
      IEmpresaService empresaService,
      ModelMapper modelMapper) {
    this.facturaService = facturaService;
    this.pedidoRepository = pedidoRepository;
    this.renglonPedidoRepository = renglonPedidoRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.correoElectronicoService = correoElectronicoService;
    this.empresaService = empresaService;
    this.modelMapper = modelMapper;
  }

  private void validarPedido(TipoDeOperacion operacion, Pedido pedido) {
    // Entrada de Datos
    // Requeridos
    if (pedido.getFecha() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pedido_fecha_vacia"));
    }
    if (pedido.getRenglones() == null || pedido.getRenglones().isEmpty()) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_pedido_renglones_vacio"));
    }
    if (pedido.getEmpresa() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pedido_empresa_vacia"));
    }
    if (pedido.getUsuario() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pedido_usuario_vacio"));
    }
    if (pedido.getCliente() == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pedido_cliente_vacio"));
    }
    // Validar Estado
    EstadoPedido estado = pedido.getEstado();
    if ((estado != EstadoPedido.ABIERTO)
        && (estado != EstadoPedido.ACTIVO)
        && (estado != EstadoPedido.CERRADO)) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaja_estado_no_valido"));
    }
    // Duplicados
    if (operacion == TipoDeOperacion.ALTA
        && pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(
                pedido.getNroPedido(), pedido.getEmpresa(), false)
            != null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pedido_duplicado"));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(
                pedido.getNroPedido(), pedido.getEmpresa(), false)
            == null) {
      throw new BusinessServiceException(RESOURCE_BUNDLE.getString("mensaje_pedido_no_existente"));
    }
    // DetalleEnvío
    if (pedido.getDetalleEnvio() == null) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_pedido_detalle_envio_vacio"));
    }
  }

  @Override
  public Pedido getPedidoPorId(Long idPedido) {
    Pedido pedido = pedidoRepository.findById(idPedido);
    if (pedido == null) {
      throw new EntityNotFoundException(RESOURCE_BUNDLE.getString("mensaje_pedido_no_existente"));
    }
    return pedido;
  }

  @Override
  public Pedido actualizarEstadoPedido(Pedido pedido) {
    pedido.setEstado(EstadoPedido.ACTIVO);
    if (this.getFacturasDelPedido(pedido.getId_Pedido()).isEmpty()) {
      pedido.setEstado(EstadoPedido.ABIERTO);
    }
    if (facturaService.pedidoTotalmenteFacturado(pedido)) {
      pedido.setEstado(EstadoPedido.CERRADO);
    }
    return pedido;
  }

  @Override
  public Pedido calcularTotalActualDePedido(Pedido pedido) {
    BigDecimal porcentajeDescuento;
    BigDecimal totalActual = BigDecimal.ZERO;
    List<Long> idsProductos = new ArrayList<>();
    List<RenglonPedido> renglonesDelPedido = this.getRenglonesDelPedido(pedido.getId_Pedido());
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
                          .getDescuentoPorcentaje()
                          .divide(CIEN, 15, RoundingMode.HALF_UP))));
      totalActual = totalActual.add(renglonPedido.getImporte());
      i++;
    }
    porcentajeDescuento =
        BigDecimal.ONE.subtract(
            pedido.getDescuentoPorcentaje().divide(CIEN, 15, RoundingMode.HALF_UP));
    pedido.setTotalActual(totalActual.multiply(porcentajeDescuento));
    return pedido;
  }

  @Override
  public long generarNumeroPedido(Empresa empresa) {
    long min = 1L;
    long max = 9999999999L; // 10 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      Pedido p = pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(randomLong, empresa, false);
      if (p == null) esRepetido = false;
    }
    return randomLong;
  }

  @Override
  public List<Factura> getFacturasDelPedido(long idPedido) {
    return facturaService.getFacturasDelPedido(idPedido);
  }

  @Override
  @Transactional
  public Pedido guardar(Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal) {
    this.asignarDetalleEnvio(pedido, tipoDeEnvio, idSucursal);
    pedido.setFecha(new Date());
    pedido.setNroPedido(this.generarNumeroPedido(pedido.getEmpresa()));
    pedido.setEstado(EstadoPedido.ABIERTO);
    if (pedido.getObservaciones() == null || pedido.getObservaciones().equals("")) {
      pedido.setObservaciones("Los precios se encuentran sujetos a modificaciones.");
    }
    this.validarPedido(TipoDeOperacion.ALTA, pedido);
    pedido = pedidoRepository.save(pedido);
    logger.warn("El Pedido {} se guardó correctamente.", pedido);
    String emailCliente = pedido.getCliente().getEmail();
    if (emailCliente != null && !emailCliente.isEmpty()) {
      correoElectronicoService.enviarMailPorEmpresa(
          pedido.getEmpresa().getId_Empresa(),
          emailCliente,
          "",
          "Nuevo Pedido Ingresado",
          MessageFormat.format(
              RESOURCE_BUNDLE.getString("mensaje_correo_pedido_recibido"),
              pedido.getCliente().getNombreFiscal(),
              "Pedido Nº " + pedido.getNroPedido()),
          this.getReportePedido(pedido),
          "Reporte");
      logger.warn("El mail del pedido nro {} se envió.", pedido.getNroPedido());
    }
    return pedido;
  }

  private void asignarDetalleEnvio(Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal) {
    if (tipoDeEnvio == TipoDeEnvio.USAR_UBICACION_FACTURACION
        && pedido.getCliente().getUbicacionFacturacion() == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_ubicacion_facturacion_vacia"));
    }
    if (tipoDeEnvio == TipoDeEnvio.USAR_UBICACION_ENVIO
        && pedido.getCliente().getUbicacionEnvio() == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_ubicacion_envio_vacia"));
    }
    if (tipoDeEnvio == TipoDeEnvio.RETIRO_EN_SUCURSAL && idSucursal == null) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_ubicacion_sucursal_vacia"));
    }
    if (tipoDeEnvio == TipoDeEnvio.USAR_UBICACION_FACTURACION) {
      pedido.setDetalleEnvio(
          modelMapper.map(pedido.getCliente().getUbicacionFacturacion(), UbicacionDTO.class));
    }
    if (tipoDeEnvio == TipoDeEnvio.USAR_UBICACION_ENVIO) {
      pedido.setDetalleEnvio(
          modelMapper.map(pedido.getCliente().getUbicacionEnvio(), UbicacionDTO.class));
    }
    if (tipoDeEnvio == TipoDeEnvio.RETIRO_EN_SUCURSAL) {
      pedido.setDetalleEnvio(
          modelMapper.map(
              empresaService.getEmpresaPorId(idSucursal).getUbicacion(), UbicacionDTO.class));
    }
    pedido.setTipoDeEnvio(tipoDeEnvio);
  }

  @Override
  public Page<Pedido> buscarConCriteria(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn) {
    // Fecha
    if (criteria.isBuscaPorFecha()
        && (criteria.getFechaDesde() == null || criteria.getFechaHasta() == null)) {
      throw new BusinessServiceException(
          RESOURCE_BUNDLE.getString("mensaje_pedido_fechas_busqueda_invalidas"));
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
    QPedido qPedido = QPedido.pedido;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(
        qPedido.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qPedido.eliminado.eq(false)));
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
      builder.and(qPedido.fecha.between(fDesde, fHasta));
    }
    if (criteria.isBuscaCliente()) builder.and(qPedido.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.isBuscaUsuario()) builder.and(qPedido.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.isBuscaPorViajante()) builder.and(qPedido.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.isBuscaPorNroPedido()) builder.and(qPedido.nroPedido.eq(criteria.getNroPedido()));
    if (criteria.isBuscaPorEstadoPedido())
      builder.and(qPedido.estado.eq(criteria.getEstadoPedido()));
    if (criteria.isBuscaPorEnvio()) builder.and(qPedido.tipoDeEnvio.eq(criteria.getTipoDeEnvio()));
    if (criteria.isBuscaPorProducto())
      builder.and(qPedido.renglones.any().idProductoItem.eq(criteria.getIdProducto()));
    Usuario usuarioLogueado = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
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
                clienteService.getClientePorIdUsuarioYidEmpresa(
                    idUsuarioLoggedIn, criteria.getIdEmpresa());
            if (clienteRelacionado != null) {
              rsPredicate.or(qPedido.cliente.eq(clienteRelacionado));
            }
            break;
        }
      }
      builder.and(rsPredicate);
    }
    Page<Pedido> pedidos = pedidoRepository.findAll(builder, criteria.getPageable());
    pedidos.getContent().forEach(this::calcularTotalActualDePedido);
    return pedidos;
  }

  @Override
  @Transactional
  public void actualizar(Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal) {
    this.asignarDetalleEnvio(pedido, tipoDeEnvio, idSucursal);
    this.validarPedido(TipoDeOperacion.ACTUALIZACION, pedido);
    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void actualizarFacturasDelPedido(Pedido pedido, List<Factura> facturas) {
    pedido.setFacturas(facturas);
    this.validarPedido(TipoDeOperacion.ACTUALIZACION, pedido);
    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public boolean eliminar(long idPedido) {
    Pedido pedido = this.getPedidoPorId(idPedido);
    if (pedido.getEstado() == EstadoPedido.ABIERTO) {
      pedido.setEliminado(true);
      pedidoRepository.save(pedido);
    }
    return pedido.isEliminado();
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedido(Long idPedido) {
    return renglonPedidoRepository.findByIdPedido(idPedido);
  }

  @Override
  public Map<Long, RenglonFactura> getRenglonesFacturadosDelPedido(long nroPedido) {
    List<RenglonFactura> renglonesDeFacturas = new ArrayList<>();
    this.getFacturasDelPedido(nroPedido)
        .forEach(
            f ->
                f.getRenglones()
                    .forEach(
                        r ->
                            renglonesDeFacturas.add(
                                facturaService.calcularRenglon(
                                    f.getTipoComprobante(),
                                    Movimiento.VENTA,
                                    r.getCantidad(),
                                    r.getIdProductoItem(),
                                    r.getDescuentoPorcentaje(),
                                    false))));
    HashMap<Long, RenglonFactura> listaRenglonesUnificados = new HashMap<>();
    if (!renglonesDeFacturas.isEmpty()) {
      renglonesDeFacturas.forEach(
          r -> {
            if (listaRenglonesUnificados.containsKey(r.getIdProductoItem())) {
              listaRenglonesUnificados
                  .get(r.getIdProductoItem())
                  .setCantidad(
                      listaRenglonesUnificados
                          .get(r.getIdProductoItem())
                          .getCantidad()
                          .add(r.getCantidad()));
            } else {
              listaRenglonesUnificados.put(r.getIdProductoItem(), r);
            }
          });
    }
    return listaRenglonesUnificados;
  }

  @Override
  public byte[] getReportePedido(Pedido pedido) {
    ClassLoader classLoader = PedidoServiceImpl.class.getClassLoader();
    InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/Pedido.jasper");
    Map<String, Object> params = new HashMap<>();
    params.put("pedido", pedido);
    if (pedido.getEmpresa().getLogo() != null && !pedido.getEmpresa().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(pedido.getEmpresa().getLogo()))).getImage());
      } catch (IOException ex) {
        logger.error(ex.getMessage());
        throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_empresa_404_logo"), ex);
      }
    }
    String detalleEnvio;
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.RETIRO_EN_SUCURSAL) {
      detalleEnvio = "Retira en Sucursal: " + pedido.getEnvio();
    } else {
      detalleEnvio = pedido.getEnvio();
    }
    params.put("detalleEnvio", detalleEnvio);
    List<RenglonPedido> renglones = this.getRenglonesDelPedido(pedido.getId_Pedido());
    JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones);
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params, ds));
    } catch (JRException ex) {
      logger.error(ex.getMessage());
      throw new ServiceException(RESOURCE_BUNDLE.getString("mensaje_error_reporte"), ex);
    }
  }

  @Override
  public BigDecimal calcularDescuentoNeto(
      BigDecimal precioUnitario, BigDecimal descuentoPorcentaje) {
    BigDecimal resultado = BigDecimal.ZERO;
    if (descuentoPorcentaje.compareTo(BigDecimal.ZERO) != 0) {
      resultado =
          precioUnitario.multiply(descuentoPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
    }
    return resultado;
  }

  @Override
  public RenglonPedido calcularRenglonPedido(
      long idProducto, BigDecimal cantidad, BigDecimal descuentoPorcentaje) {
    RenglonPedido nuevoRenglon = new RenglonPedido();
    Producto producto = productoService.getProductoPorId(idProducto);
    nuevoRenglon.setIdProductoItem(producto.getIdProducto());
    nuevoRenglon.setCantidad(cantidad);
    nuevoRenglon.setCodigoItem(producto.getCodigo());
    nuevoRenglon.setDescripcionItem(producto.getDescripcion());
    nuevoRenglon.setMedidaItem(producto.getMedida().getNombre());
    nuevoRenglon.setPrecioUnitario(producto.getPrecioLista());
    nuevoRenglon.setDescuentoPorcentaje(descuentoPorcentaje);
    nuevoRenglon.setDescuentoNeto(
        this.calcularDescuentoNeto(producto.getPrecioLista(), descuentoPorcentaje));
    nuevoRenglon.setImporte(
        CalculosComprobante.calcularImporte(
            nuevoRenglon.getCantidad(),
            producto.getPrecioLista(),
            nuevoRenglon.getDescuentoNeto()));
    return nuevoRenglon;
  }

  @Override
  public List<RenglonPedido> calcularRenglonesPedido(
      List<NuevoRenglonPedidoDTO> nuevosRenglonesPedidoDTO) {
    List<RenglonPedido> renglonesPedido = new ArrayList<>();
    nuevosRenglonesPedidoDTO.forEach(
        nuevoRenglonesPedidoDTO ->
            renglonesPedido.add(
                this.calcularRenglonPedido(
                    nuevoRenglonesPedidoDTO.getIdProductoItem(),
                    nuevoRenglonesPedidoDTO.getCantidad(),
                    nuevoRenglonesPedidoDTO.getDescuentoPorcentaje())));
    return renglonesPedido;
  }
}
