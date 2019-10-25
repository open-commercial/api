package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;

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
import javax.validation.Valid;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.calculos.NuevosResultadosPedido;
import sic.modelo.calculos.Resultados;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.RenglonPedidoRepository;
import sic.service.*;
import sic.repository.PedidoRepository;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.util.CalculosComprobante;
import sic.util.FormatterFechaHora;

@Service
@Validated
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
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;

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
      ModelMapper modelMapper,
      MessageSource messageSource) {
    this.facturaService = facturaService;
    this.pedidoRepository = pedidoRepository;
    this.renglonPedidoRepository = renglonPedidoRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.correoElectronicoService = correoElectronicoService;
    this.empresaService = empresaService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  private void validarOperacion(TipoDeOperacion operacion, Pedido pedido) {
    // Entrada de Datos
    // Validar Estado
    EstadoPedido estado = pedido.getEstado();
    if ((estado != EstadoPedido.ABIERTO)
        && (estado != EstadoPedido.ACTIVO)
        && (estado != EstadoPedido.CERRADO)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaja_estado_no_valido", null, Locale.getDefault()));
    }
    // Duplicados
    if (operacion == TipoDeOperacion.ALTA
        && pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(
                pedido.getNroPedido(), pedido.getEmpresa(), false)
            != null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_pedido_duplicado", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && pedidoRepository.findByNroPedidoAndEmpresaAndEliminado(
                pedido.getNroPedido(), pedido.getEmpresa(), false)
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
    List<RenglonPedido> renglonesDelPedido = this.getRenglonesDelPedidoOrdenadoPorIdProducto(pedido.getId_Pedido());
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
                          .divide(CIEN, 2, RoundingMode.HALF_UP))));
      totalActual = totalActual.add(renglonPedido.getImporte());
      i++;
    }
    porcentajeDescuento =
        BigDecimal.ONE.subtract(
            pedido.getDescuentoPorcentaje().divide(CIEN, 2, RoundingMode.HALF_UP));
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
  public Pedido guardar(@Valid Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal) {
    this.asignarDetalleEnvio(pedido, tipoDeEnvio, idSucursal);
    this.calcularCantidadDeArticulos(pedido);
    pedido.setNroPedido(this.generarNumeroPedido(pedido.getEmpresa()));
    pedido.setEstado(EstadoPedido.ABIERTO);
    if (pedido.getObservaciones() == null || pedido.getObservaciones().equals("")) {
      pedido.setObservaciones("Los precios se encuentran sujetos a modificaciones.");
    }
    this.validarOperacion(TipoDeOperacion.ALTA, pedido);
    pedido = pedidoRepository.save(pedido);
    logger.warn("El Pedido {} se guardó correctamente.", pedido);
    String emailCliente = pedido.getCliente().getEmail();
    if (emailCliente != null && !emailCliente.isEmpty()) {
      correoElectronicoService.enviarMailPorEmpresa(
          pedido.getEmpresa().getId_Empresa(),
          emailCliente,
          "",
          "Nuevo Pedido Ingresado",
          messageSource.getMessage(
              "mensaje_correo_pedido_recibido",
              new Object[] {
                pedido.getCliente().getNombreFiscal(), "Pedido Nº " + pedido.getNroPedido()
              },
              Locale.getDefault()),
          this.getReportePedido(pedido.getId_Pedido()),
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

  private void asignarDetalleEnvio(Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal) {
    if (tipoDeEnvio == TipoDeEnvio.USAR_UBICACION_FACTURACION
        && pedido.getCliente().getUbicacionFacturacion() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault()));
    }
    if (tipoDeEnvio == TipoDeEnvio.USAR_UBICACION_ENVIO
        && pedido.getCliente().getUbicacionEnvio() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_envio_vacia", null, Locale.getDefault()));
    }
    if (tipoDeEnvio == TipoDeEnvio.RETIRO_EN_SUCURSAL && idSucursal == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_sucursal_vacia", null, Locale.getDefault()));
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
    builder.and(
        qPedido.empresa.id_Empresa.eq(criteria.getIdEmpresa()).and(qPedido.eliminado.eq(false)));
    if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
      criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
      criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59));
      String dateTemplate = "convert({0}, datetime)";
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        DateExpression<LocalDateTime> fDesde =
            Expressions.dateTemplate(
                LocalDateTime.class,
                dateTemplate,
                FormatterFechaHora.formatoFecha(
                    criteria.getFechaDesde(), FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL));
        DateExpression<LocalDateTime> fHasta =
            Expressions.dateTemplate(
                LocalDateTime.class,
                dateTemplate,
                FormatterFechaHora.formatoFecha(
                    criteria.getFechaHasta(), FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL));
        builder.and(qPedido.fecha.between(fDesde, fHasta));
      } else if (criteria.getFechaDesde() != null) {
        DateExpression<LocalDateTime> fDesde =
            Expressions.dateTemplate(
                LocalDateTime.class,
                dateTemplate,
                FormatterFechaHora.formatoFecha(
                    criteria.getFechaDesde(), FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL));
        builder.and(qPedido.fecha.after(fDesde));
      } else if (criteria.getFechaHasta() != null) {
        DateExpression<LocalDateTime> fHasta =
            Expressions.dateTemplate(
                LocalDateTime.class,
                dateTemplate,
                FormatterFechaHora.formatoFecha(
                    criteria.getFechaHasta(), FormatterFechaHora.FORMATO_FECHAHORA_INTERNACIONAL));
        builder.and(qPedido.fecha.before(fHasta));
      }
    }
    if (criteria.getIdCliente() != null)
      builder.and(qPedido.cliente.id_Cliente.eq(criteria.getIdCliente()));
    if (criteria.getIdUsuario() != null)
      builder.and(qPedido.usuario.id_Usuario.eq(criteria.getIdUsuario()));
    if (criteria.getIdViajante() != null)
      builder.and(qPedido.cliente.viajante.id_Usuario.eq(criteria.getIdViajante()));
    if (criteria.getNroPedido() != null) builder.and(qPedido.nroPedido.eq(criteria.getNroPedido()));
    if (criteria.getEstadoPedido() != null)
      builder.and(qPedido.estado.eq(criteria.getEstadoPedido()));
    if (criteria.getTipoDeEnvio() != null) builder.and(qPedido.tipoDeEnvio.eq(criteria.getTipoDeEnvio()));
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
    return builder;
  }

  private Pageable getPageable(int pagina, String ordenarPor, String sentido) {
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  @Transactional
  public void actualizar(@Valid Pedido pedido, TipoDeEnvio tipoDeEnvio, Long idSucursal) {
    this.asignarDetalleEnvio(pedido, tipoDeEnvio, idSucursal);
    this.calcularCantidadDeArticulos(pedido);
    this.validarOperacion(TipoDeOperacion.ACTUALIZACION, pedido);
    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void actualizarFacturasDelPedido(@Valid Pedido pedido, List<Factura> facturas) {
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
      pedidoRepository.save(pedido);
    }
    return pedido.isEliminado();
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglon(Long idPedido) {
    return renglonPedidoRepository.findByIdPedidoOrderByIdRenglonPedido(idPedido);
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedidoOrdenadoPorIdProducto(Long idPedido) {
    return renglonPedidoRepository.findByIdPedidoOrderByIdProductoItem(idPedido);
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
  public byte[] getReportePedido(long idPedido) {
    ClassLoader classLoader = PedidoServiceImpl.class.getClassLoader();
    InputStream isFileReport = classLoader.getResourceAsStream("sic/vista/reportes/Pedido.jasper");
    Map<String, Object> params = new HashMap<>();
    Pedido pedido = this.getPedidoNoEliminadoPorId(idPedido);
    params.put("pedido", pedido);
    if (pedido.getEmpresa().getLogo() != null && !pedido.getEmpresa().getLogo().isEmpty()) {
      try {
        params.put(
            "logo", new ImageIcon(ImageIO.read(new URL(pedido.getEmpresa().getLogo()))).getImage());
      } catch (IOException ex) {
        throw new ServiceException(messageSource.getMessage(
          "mensaje_empresa_404_logo", null, Locale.getDefault()), ex);
      }
    }
    String detalleEnvio;
    if (pedido.getTipoDeEnvio() == TipoDeEnvio.RETIRO_EN_SUCURSAL) {
      detalleEnvio = "Retira en Sucursal: " + pedido.getEnvio();
    } else {
      detalleEnvio = pedido.getEnvio();
    }
    params.put("detalleEnvio", detalleEnvio);
    List<RenglonPedido> renglones = this.getRenglonesDelPedidoOrdenadorPorIdRenglon(pedido.getId_Pedido());
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
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
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

  @Override
  public Resultados calcularResultadosPedido(NuevosResultadosPedido calculoPedido) {
    Resultados resultados = Resultados.builder().build();
    resultados.setDescuentoPorcentaje(calculoPedido.getDescuentoPorcentaje());
    resultados.setRecargoPorcentaje(calculoPedido.getRecargoPorcentaje());
    calculoPedido
        .getRenglones()
        .forEach(
            renglonPedidoDTO ->
                resultados.setSubTotal(
                    renglonPedidoDTO
                        .getCantidad()
                        .multiply(
                            renglonPedidoDTO
                                .getPrecioUnitario()
                                .subtract(
                                    renglonPedidoDTO
                                        .getDescuentoPorcentaje()
                                        .divide(new BigDecimal("100"), 2, RoundingMode.FLOOR)
                                        .multiply(renglonPedidoDTO.getPrecioUnitario())))));
    resultados.setSubTotalBruto(
        resultados
            .getSubTotal()
            .subtract(
                calculoPedido
                    .getDescuentoPorcentaje()
                    .divide(new BigDecimal("100"), 2, RoundingMode.FLOOR))
            .multiply(resultados.getSubTotal()));
    resultados.setTotal(resultados.getSubTotalBruto());
    return resultados;
  }
}
