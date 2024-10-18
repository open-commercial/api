package sic.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaPedidoCriteria;
import sic.modelo.dto.NuevoRenglonPedidoDTO;
import sic.modelo.dto.NuevosResultadosComprobanteDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.modelo.dto.UbicacionDTO;
import sic.repository.PedidoRepository;
import sic.repository.RenglonPedidoRepository;
import sic.util.CalculosComprobante;
import sic.util.CustomValidator;
import sic.util.FormatoReporte;
import sic.util.JasperReportsHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class PedidoServiceImpl implements PedidoService {

  @Value("${EMAIL_DEFAULT_PROVIDER}")
  private String emailDefaultProvider;

  private final PedidoRepository pedidoRepository;
  private final RenglonPedidoRepository renglonPedidoRepository;
  private final UsuarioService usuarioService;
  private final ClienteService clienteService;
  private final ProductoService productoService;
  private final EmailServiceFactory emailServiceFactory;
  private final ReciboService reciboService;
  private final CuentaCorrienteService cuentaCorrienteService;
  private final ModelMapper modelMapper;
  private static final BigDecimal CIEN = new BigDecimal("100");
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final MessageSource messageSource;
  private final CustomValidator customValidator;
  private final JasperReportsHandler jasperReportsHandler;

  @Autowired
  public PedidoServiceImpl(
          PedidoRepository pedidoRepository,
          RenglonPedidoRepository renglonPedidoRepository,
          UsuarioService usuarioService,
          ClienteService clienteService,
          ProductoService productoService,
          EmailServiceFactory emailServiceFactory,
          ReciboService reciboService,
          CuentaCorrienteService cuentaCorrienteService,
          ModelMapper modelMapper,
          MessageSource messageSource,
          CustomValidator customValidator,
          JasperReportsHandler jasperReportsHandler) {
    this.pedidoRepository = pedidoRepository;
    this.renglonPedidoRepository = renglonPedidoRepository;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.productoService = productoService;
    this.emailServiceFactory = emailServiceFactory;
    this.reciboService = reciboService;
    this.cuentaCorrienteService = cuentaCorrienteService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
    this.jasperReportsHandler = jasperReportsHandler;
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Pedido pedido) {
    // Entrada de Datos
    // Validar Estado
    if (pedido.getEstado() != EstadoPedido.ABIERTO
            && (operacion == TipoDeOperacion.ALTA || operacion == TipoDeOperacion.ACTUALIZACION)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaja_estado_no_valido", null, Locale.getDefault()));
    }
    // Duplicados
    if (operacion == TipoDeOperacion.ALTA && pedido.getEstado() != EstadoPedido.ABIERTO) {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaja_estado_no_valido", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ALTA
        && pedidoRepository.existsByNroPedidoAndSucursal(pedido.getNroPedido(), pedido.getSucursal())) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_pedido_duplicado", null, Locale.getDefault()));
    }
    if (operacion == TipoDeOperacion.ACTUALIZACION
        && !pedidoRepository.existsByNroPedidoAndSucursal(pedido.getNroPedido(), pedido.getSucursal())) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_pedido_no_existente", null, Locale.getDefault()));
    }
    // DetalleEnvío
    if (pedido.getDetalleEnvio() == null) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_pedido_detalle_envio_vacio", null, Locale.getDefault()));
    }
    //Stock
    long[] idProducto = new long[pedido.getRenglones().size()];
    BigDecimal[] cantidad =  new BigDecimal[pedido.getRenglones().size()];
    int i = 0;
    for (RenglonPedido renglonPedido : pedido.getRenglones()) {
      idProducto[i] = renglonPedido.getIdProductoItem();
      cantidad[i] = renglonPedido.getCantidad();
      i++;
    }
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO = ProductosParaVerificarStockDTO.builder()
            .cantidad(cantidad)
            .idProducto(idProducto)
            .idPedido(pedido.getIdPedido())
            .idSucursal(pedido.getIdSucursal())
            .build();
    if ((operacion == TipoDeOperacion.ALTA || operacion == TipoDeOperacion.ACTUALIZACION)
            && !productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO).isEmpty()) {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_pedido_sin_stock", null, Locale.getDefault()));
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
  public Pedido getPedidoPorNumeroAndSucursal(long nroPedido, Sucursal sucursal) {
    return pedidoRepository.findByNroPedidoAndSucursalAndEliminado(nroPedido, sucursal, false);
  }

  @Override
  public long generarNumeroPedido(Sucursal sucursal) {
    long min = 1L;
    long max = 9999999999L; // 10 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      esRepetido = pedidoRepository.existsByNroPedidoAndSucursal(randomLong, sucursal);
    }
    return randomLong;
  }

  @Override
  @Transactional
  public Pedido guardar(Pedido pedido, List<Recibo> recibos) {
    var clienteDeUsuario = clienteService.getClientePorIdUsuario(pedido.getUsuario().getIdUsuario());
    if (pedido.getCliente().equals(clienteDeUsuario)
            && pedido.getUsuario().getRoles().contains(Rol.VENDEDOR)
            && pedido.getDescuentoPorcentaje().compareTo(BigDecimal.ZERO) > 0) {
      throw new BusinessServiceException(
              messageSource.getMessage(
                      "mensaje_no_se_puede_guardar_pedido_con_descuento_usuario_cliente_iguales",
                      null, Locale.getDefault()));
    }
    if (pedido.getFecha() == null) {
      pedido.setFecha(LocalDateTime.now());
    }
    var importe = BigDecimal.ZERO;
    for (RenglonPedido renglon : pedido.getRenglones()) {
      importe = importe.add(renglon.getImporte()).setScale(5, RoundingMode.HALF_UP);
    }
    var recargoNeto = importe.multiply(pedido.getRecargoPorcentaje()).divide(CIEN, 15, RoundingMode.HALF_UP);
    var descuentoNeto = importe.multiply(pedido.getDescuentoPorcentaje()).divide(CIEN, 15, RoundingMode.HALF_UP);
    var total = importe.add(recargoNeto).subtract(descuentoNeto);
    pedido.setSubTotal(importe);
    pedido.setRecargoNeto(recargoNeto);
    pedido.setDescuentoNeto(descuentoNeto);
    pedido.setTotal(total);
    this.validarPedidoContraPagos(pedido, recibos);
    pedido.setFecha(LocalDateTime.now());
    this.asignarDetalleEnvio(pedido);
    this.calcularCantidadDeArticulos(pedido);
    pedido.setNroPedido(this.generarNumeroPedido(pedido.getSucursal()));
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
    pedido.setEstado(EstadoPedido.ABIERTO);
    this.validarReglasDeNegocio(TipoDeOperacion.ALTA, pedido);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ALTA);
    pedido = pedidoRepository.save(pedido);
    this.actualizarCantidadReservadaDeProductosPorCambioDeEstado(pedido);
    log.info("El Pedido {} se guardó correctamente.", pedido);
    String emailCliente = pedido.getCliente().getEmail();
    if (emailCliente != null && !emailCliente.isEmpty()) {
      emailServiceFactory.getEmailService(emailDefaultProvider)
              .enviarEmail(
                      emailCliente,
                      "",
                      "Nuevo Pedido Ingresado",
                      messageSource.getMessage(
                              "mensaje_correo_pedido_recibido",
                              new Object[]{
                                      pedido.getCliente().getNombreFiscal(), "Pedido Nº " + pedido.getNroPedido()
                              },
                              Locale.getDefault()),
                      this.getReportePedido(pedido.getIdPedido()),
                      "Pedido.pdf");
      log.info("El mail del pedido nro {} se envió.", pedido.getNroPedido());
    }
    return pedido;
  }

  @Override
  @Transactional
  public void cambiarFechaDeVencimiento(long idPedido) {
    Pedido pedido = this.getPedidoNoEliminadoPorId(idPedido);
    pedido.setFechaVencimiento(pedido.getFecha().plusMinutes(pedido.getSucursal().getConfiguracionSucursal().getVencimientoLargo()));
    pedidoRepository.save(pedido);
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
        && !pedido.getSucursal().getConfiguracionSucursal().isPuntoDeRetiro()) {
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
    return pedidoRepository.findAll(
            this.getBuilderPedido(criteria, idUsuarioLoggedIn),
            this.getPageable(
                    (criteria.getPagina() == null || criteria.getPagina() < 0)
                            ? 0
                            : criteria.getPagina(),
                    criteria.getOrdenarPor(),
                    criteria.getSentido()));
  }

  @Override
  public BooleanBuilder getBuilderPedido(BusquedaPedidoCriteria criteria, long idUsuarioLoggedIn) {
    QPedido qPedido = QPedido.pedido;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getIdSucursal() != null) {
      builder.and(qPedido.sucursal.idSucursal.eq(criteria.getIdSucursal()));
    }
    if (criteria.getFechaDesde() != null || criteria.getFechaHasta() != null) {
      if (criteria.getFechaDesde() != null && criteria.getFechaHasta() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
        criteria.setFechaHasta(criteria.getFechaHasta().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        builder.and(qPedido.fecha.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0).withNano(0));
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
          case VIAJANTE -> rsPredicate.or(qPedido.usuario.eq(usuarioLogueado));
          case COMPRADOR -> {
            Cliente clienteRelacionado =
                    clienteService.getClientePorIdUsuario(idUsuarioLoggedIn);
            if (clienteRelacionado != null) {
              rsPredicate.or(qPedido.cliente.eq(clienteRelacionado));
            }
          }
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
      return switch (sentido) {
        case "ASC" -> PageRequest.of(
                pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC" -> PageRequest.of(
                pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
        default -> PageRequest.of(
                pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenDefault));
      };
    }
  }

  @Override
  @Transactional
  public void actualizar(Pedido pedido, List<RenglonPedido> renglonesAnteriores, Long idSucursalOrigen, List<Recibo> recibos) {
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
    pedido.setTotal(resultados.getTotal());
    this.validarPedidoContraPagos(pedido, recibos);
    this.asignarDetalleEnvio(pedido);
    this.calcularCantidadDeArticulos(pedido);
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, pedido);
    productoService.devolverStockPedido(pedido, TipoDeOperacion.ACTUALIZACION, renglonesAnteriores, idSucursalOrigen);
    productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
    pedidoRepository.save(pedido);
    this.actualizarCantidadReservadaDeProductosPorModificacion(pedido, renglonesAnteriores);
  }

  private void validarPedidoContraPagos(Pedido pedido, List<Recibo> recibos) {
    if (pedido.getCliente().isPuedeComprarAPlazo()) {
      pedido.setFechaVencimiento(
              pedido.getFecha().plusMinutes(pedido.getSucursal().getConfiguracionSucursal().getVencimientoLargo()));
      if (recibos != null && !recibos.isEmpty()) {
        recibos.forEach(reciboService::guardar);
      }
    } else {
      BigDecimal saldoCC = cuentaCorrienteService.getSaldoCuentaCorriente(pedido.getCliente().getIdCliente());
      if (recibos != null && !recibos.isEmpty()) {
        BigDecimal totalRecibos = recibos.stream().map(Recibo::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
        BigDecimal saldoParaCubrir = saldoCC.subtract(pedido.getTotal()).setScale(2, RoundingMode.DOWN);
        if (totalRecibos.add(saldoParaCubrir).compareTo(BigDecimal.ZERO) < 0) {
          throw new BusinessServiceException(
                  messageSource.getMessage(
                          "mensaje_cliente_no_puede_comprar_a_plazo", null, Locale.getDefault()));
        } else {
          pedido.setFechaVencimiento(
                  pedido.getFecha().plusMinutes(pedido.getSucursal().getConfiguracionSucursal().getVencimientoLargo()));
        }
        recibos.forEach(reciboService::guardar);
      } else {
        if (saldoCC.setScale(2, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) >= 0) {
          pedido.setFechaVencimiento(
                  pedido.getFecha().plusMinutes(pedido.getSucursal().getConfiguracionSucursal().getVencimientoCorto()));
        } else {
          throw new BusinessServiceException(
                  messageSource.getMessage(
                          "mensaje_cliente_saldar_cc", null, Locale.getDefault()));
        }
      }
    }
    if (pedido.getCliente().getMontoCompraMinima() != null
            && pedido.getTotal().setScale(2, RoundingMode.DOWN)
            .compareTo(pedido.getCliente().getMontoCompraMinima().setScale(2, RoundingMode.DOWN)) < 0) {
      throw new BusinessServiceException(
              messageSource.getMessage(
                      "mensaje_pedido_monto_compra_minima", null, Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public void actualizarFacturasDelPedido(Pedido pedido, List<Factura> facturas) {
    customValidator.validar(pedido);
    pedido.setFacturas(facturas);
    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void cancelar(Pedido pedido) {
    if (pedido.getEstado() == EstadoPedido.ABIERTO) {
      pedido.setEstado(EstadoPedido.CANCELADO);
      productoService.actualizarStockPedido(pedido, TipoDeOperacion.ACTUALIZACION);
      pedido = pedidoRepository.save(pedido);
      this.actualizarCantidadReservadaDeProductosPorCambioDeEstado(pedido);
      log.info(messageSource.getMessage(
              "mensaje_pedido_cancelado", new Object[]{pedido}, Locale.getDefault()));
    } else {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_no_se_puede_cancelar_pedido",
              new Object[] {pedido.getEstado()},
              Locale.getDefault()));
    }
  }

  @Override
  @Transactional
  public void eliminar(long idPedido) {
    Pedido pedido = this.getPedidoNoEliminadoPorId(idPedido);
    if (pedido.getEstado() == EstadoPedido.ABIERTO) {
      productoService.actualizarStockPedido(pedido, TipoDeOperacion.ELIMINACION);
      pedidoRepository.delete(pedido);
    } else {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_no_se_puede_eliminar_pedido",
              new Object[] {pedido.getEstado()},
              Locale.getDefault()));
    }
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglon(Long idPedido) {
    return renglonPedidoRepository.findByIdPedidoOrderByIdRenglonPedido(idPedido);
  }

  @Override
  public List<RenglonPedido> getRenglonesDelPedidoOrdenadorPorIdRenglonSegunEstadoOrClonar(
      Long idPedido, boolean clonar) {
    List<RenglonPedido> renglonPedidos =
        renglonPedidoRepository.findByIdPedidoOrderByIdRenglonPedidoAndProductoNotEliminado(
            idPedido);
    Pedido pedido = this.getPedidoNoEliminadoPorId(idPedido);
    if (pedido.getEstado().equals(EstadoPedido.ABIERTO) || clonar) {
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
  public byte[] getReportePedido(long idPedido) {
    Map<String, Object> params = new HashMap<>();
    var pedido = this.getPedidoNoEliminadoPorId(idPedido);
    params.put("pedido", pedido);
    if (pedido.getSucursal().getLogo() != null && !pedido.getSucursal().getLogo().isEmpty()) {
      try {
        params.put("logo", new ImageIcon(ImageIO.read(new URL(pedido.getSucursal().getLogo()))).getImage());
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
    var renglones = this.getRenglonesDelPedidoOrdenadorPorIdRenglon(pedido.getIdPedido());
    return jasperReportsHandler.compilar("report/Pedido.jrxml", params, renglones, FormatoReporte.PDF);
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
    nuevoRenglon.setPrecioUnitario(producto.getPrecioProducto().getPrecioLista());
    if (producto.getPrecioProducto().isOferta()
        && nuevoRenglon.getCantidad().compareTo(producto.getCantidadProducto().getCantMinima()) >= 0
        && producto.getPrecioProducto().getPorcentajeBonificacionOferta() != null) {
      nuevoRenglon.setBonificacionPorcentaje(producto.getPrecioProducto().getPorcentajeBonificacionOferta());
      nuevoRenglon.setBonificacionNeta(
          CalculosComprobante.calcularProporcion(
              nuevoRenglon.getPrecioUnitario(), producto.getPrecioProducto().getPorcentajeBonificacionOferta()));
    } else if (nuevoRenglon.getCantidad().compareTo(producto.getCantidadProducto().getCantMinima()) >= 0) {
      nuevoRenglon.setBonificacionPorcentaje(producto.getPrecioProducto().getPorcentajeBonificacionPrecio());
      nuevoRenglon.setBonificacionNeta(
          CalculosComprobante.calcularProporcion(
              nuevoRenglon.getPrecioUnitario(), producto.getPrecioProducto().getPorcentajeBonificacionPrecio()));
    } else {
      nuevoRenglon.setBonificacionPorcentaje(BigDecimal.ZERO);
      nuevoRenglon.setBonificacionNeta(BigDecimal.ZERO);
    }
    nuevoRenglon.setImporteAnterior(
        CalculosComprobante.calcularImporte(
            nuevoRenglon.getCantidad(), producto.getPrecioProducto().getPrecioLista(), BigDecimal.ZERO));
    nuevoRenglon.setImporte(
        CalculosComprobante.calcularImporte(
            nuevoRenglon.getCantidad(),
            producto.getPrecioProducto().getPrecioLista(),
            nuevoRenglon.getBonificacionNeta()));
    nuevoRenglon.setUrlImagenItem(producto.getUrlImagen());
    nuevoRenglon.setOferta(producto.getPrecioProducto().isOferta());
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

  @Scheduled(cron = "0 0/1 * * * ?")
  @Transactional
  public void cancelarPedidosAbiertos() {
    log.info(messageSource.getMessage("mensaje_cron_job_cancelar_pedidos", null, Locale.getDefault()));
    Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
    Page<Pedido> paginaPedidos = pedidoRepository.findAllByEstadoAndEliminado(EstadoPedido.ABIERTO, pageable);
    paginaPedidos.forEach(pedido -> {
          if (pedido.getFechaVencimiento().isBefore(LocalDateTime.now())) {
            this.cancelar(pedido);
          }
        });
  }

  @Override
  public long[] getArrayDeIdProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones) {
    long[] idProductoItem = new long[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      idProductoItem[i] = nuevosRenglones.get(i).getIdProductoItem();
    }
    return idProductoItem;
  }

  @Override
  public BigDecimal[] getArrayDeCantidadesProducto(List<NuevoRenglonPedidoDTO> nuevosRenglones) {
    BigDecimal[] cantidades = new BigDecimal[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      cantidades[i] = nuevosRenglones.get(i).getCantidad();
    }
    return cantidades;
  }

  @Override
  public void actualizarCantidadReservadaDeProductosPorCambioDeEstado(Pedido pedido) {
    switch (pedido.getEstado()) {
      case ABIERTO -> pedido.getRenglones().forEach(renglonPedido ->
              productoService.agregarCantidadReservada(renglonPedido.getIdProductoItem(), renglonPedido.getCantidad()));
      case CANCELADO, CERRADO -> pedido.getRenglones().forEach(renglonPedido ->
              productoService.quitarCantidadReservada(renglonPedido.getIdProductoItem(), renglonPedido.getCantidad()));
      default -> throw new ServiceException(
              messageSource.getMessage("mensaje_producto_error_actualizar_cantidad_reservada", null, Locale.getDefault()));
    }
  }

  @Override
  public void actualizarCantidadReservadaDeProductosPorModificacion(Pedido pedido, List<RenglonPedido> renglonesAnteriores) {
    if (pedido.getEstado() == EstadoPedido.ABIERTO) {
      renglonesAnteriores.forEach(renglonPedido -> productoService.quitarCantidadReservada(renglonPedido.getIdProductoItem(), renglonPedido.getCantidad()));
      pedido.getRenglones().forEach(renglonPedido ->
              productoService.agregarCantidadReservada(renglonPedido.getIdProductoItem(), renglonPedido.getCantidad()));
    } else {
      throw new ServiceException(
              messageSource.getMessage("mensaje_producto_error_actualizar_cantidad_reservada", null, Locale.getDefault()));
    }
  }
}
