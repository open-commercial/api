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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.*;
import sic.repository.RenglonTraspasoRepository;
import sic.repository.TraspasoRepository;
import sic.service.*;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class TraspasoServiceImpl implements ITraspasoService {

  private final TraspasoRepository traspasoRepository;
  private final RenglonTraspasoRepository renglonTraspasoRepository;
  private final IProductoService productoService;
  private final ISucursalService sucursalService;
  private final IUsuarioService usuarioService;
  private final IPedidoService pedidoService;
  private final MessageSource messageSource;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public TraspasoServiceImpl(
      TraspasoRepository traspasoRepository,
      RenglonTraspasoRepository renglonTraspasoRepository,
      IProductoService productoService,
      ISucursalService sucursalService,
      IUsuarioService usuarioService,
      IPedidoService pedidoService,
      MessageSource messageSource) {
    this.traspasoRepository = traspasoRepository;
    this.renglonTraspasoRepository = renglonTraspasoRepository;
    this.productoService = productoService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.pedidoService = pedidoService;
    this.messageSource = messageSource;
  }

  @Override
  public Traspaso getTraspasoNoEliminadoPorid(Long idTraspaso) {
    Optional<Traspaso> traspaso = traspasoRepository.findById(idTraspaso);
    if (traspaso.isPresent()) {
      return traspaso.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_traspaso_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public List<RenglonTraspaso> getRenglonesTraspaso(Long idTraspaso) {
    return renglonTraspasoRepository.findByIdTraspasoOrderByIdRenglonTraspaso(idTraspaso);
  }

  @Override
  public Traspaso guardarTraspasoDePedido(NuevoTraspasoDePedidoDTO nuevoTraspasoDePedidoDTO) {
    Traspaso traspaso = new Traspaso();
    traspaso.setFechaDeAlta(LocalDateTime.now());
    traspaso.setNroTraspaso(this.generarNroDeTraspaso());
    Sucursal sucursalOrigen =
        sucursalService.getSucursalPorId(nuevoTraspasoDePedidoDTO.getIdSucursalOrigen());
    Sucursal sucursalDestino =
        sucursalService.getSucursalPorId(nuevoTraspasoDePedidoDTO.getIdSucursalDestino());
    traspaso.setSucursalOrigen(sucursalOrigen);
    traspaso.setSucursalDestino(sucursalDestino);
    traspaso.setNroPedido(nuevoTraspasoDePedidoDTO.getNroPedido());
    traspaso.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(nuevoTraspasoDePedidoDTO.getIdUsuario()));
    List<RenglonTraspaso> renglonesTraspaso = new ArrayList<>();
    nuevoTraspasoDePedidoDTO
        .getIdProductoConCantidad()
        .forEach(
            (idProducto, cantidad) -> {
              Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
              RenglonTraspaso renglonTraspaso = new RenglonTraspaso();
              renglonTraspaso.setIdProducto(producto.getIdProducto());
              renglonTraspaso.setCodigoProducto(producto.getCodigo());
              renglonTraspaso.setCantidadProducto(cantidad);
              renglonTraspaso.setDescripcionProducto(producto.getDescripcion());
              renglonTraspaso.setNombreMedidaProducto(producto.getNombreMedida());
              renglonesTraspaso.add(renglonTraspaso);
            });
    traspaso.setRenglones(renglonesTraspaso);
    traspaso = traspasoRepository.save(traspaso);
    logger.warn(
        messageSource.getMessage(
            "mensaje_traspaso_realizado", new Object[] {traspaso}, Locale.getDefault()));
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ALTA);
    return traspaso;
  }

  @Override
  public Traspaso guardarTraspaso(NuevoTraspasoDTO nuevoTraspasoDTO, long idUsuario) {
    if (nuevoTraspasoDTO.getCantidad().length != nuevoTraspasoDTO.getIdProducto().length) {
      throw new BusinessServiceException(
              messageSource.getMessage(
                      "mensaje_traspaso_renglones_parametros_no_validos",
                      null,
                      Locale.getDefault()));
    }
    Traspaso traspaso = new Traspaso();
    traspaso.setFechaDeAlta(LocalDateTime.now());
    traspaso.setNroTraspaso(this.generarNroDeTraspaso());
    Sucursal sucursalOrigen =
            sucursalService.getSucursalPorId(nuevoTraspasoDTO.getIdSucursalOrigen());
    Sucursal sucursalDestino =
            sucursalService.getSucursalPorId(nuevoTraspasoDTO.getIdSucursalDestino());
    traspaso.setSucursalOrigen(sucursalOrigen);
    traspaso.setSucursalDestino(sucursalDestino);
    traspaso.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuario));
    List<RenglonTraspaso> renglonesTraspaso = new ArrayList<>();
    for (int i = 0; i < nuevoTraspasoDTO.getIdProducto().length; i++) {
      Producto producto = productoService.getProductoNoEliminadoPorId(nuevoTraspasoDTO.getIdProducto()[i]);
      RenglonTraspaso renglonTraspaso = new RenglonTraspaso();
      renglonTraspaso.setIdProducto(producto.getIdProducto());
      renglonTraspaso.setCodigoProducto(producto.getCodigo());
      renglonTraspaso.setCantidadProducto(nuevoTraspasoDTO.getCantidad()[i]);
      renglonTraspaso.setDescripcionProducto(producto.getDescripcion());
      renglonTraspaso.setNombreMedidaProducto(producto.getNombreMedida());
      renglonesTraspaso.add(renglonTraspaso);
    }
    traspaso.setRenglones(renglonesTraspaso);
    traspaso = traspasoRepository.save(traspaso);
    logger.warn(
            messageSource.getMessage(
                    "mensaje_traspaso_realizado", new Object[] {traspaso}, Locale.getDefault()));
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ALTA);
    return traspaso;
  }

  @Override
  public List<Traspaso> guardarTraspasosPorPedido(Pedido pedido) {
    List<Traspaso> traspasos = new ArrayList<>();
    this.construirNuevosTraspasosPorPedido(pedido).stream()
        .filter(nuevoTraspaso -> !nuevoTraspaso.getIdProductoConCantidad().isEmpty())
        .forEach(nuevoTraspaso -> traspasos.add(this.guardarTraspasoDePedido(nuevoTraspaso)));
    return traspasos;
  }

  @Override
  public List<NuevoTraspasoDePedidoDTO> construirNuevosTraspasosPorPedido(Pedido pedido) {
    long[] idProducto = new long[pedido.getRenglones().size()];
    BigDecimal[] cantidad = new BigDecimal[pedido.getRenglones().size()];
    int i = 0;
    for (RenglonPedido renglonPedido : pedido.getRenglones()) {
      idProducto[i] = renglonPedido.getIdProductoItem();
      cantidad[i] = renglonPedido.getCantidad();
      i++;
    }
    ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
        ProductosParaVerificarStockDTO.builder()
            .cantidad(cantidad)
            .idProducto(idProducto)
            .idSucursal(pedido.getIdSucursal())
            .build();
    List<ProductoFaltanteDTO> faltantes =
        productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
    if (!faltantes.isEmpty()) {
      List<Sucursal> sucursales = sucursalService.getSucusales(false);
      List<NuevoTraspasoDePedidoDTO> nuevosTraspasos = new ArrayList<>();
      sucursales.stream()
          .filter(sucursal -> sucursal.getIdSucursal() != pedido.getSucursal().getIdSucursal())
          .forEach(
              sucursal -> {
                NuevoTraspasoDePedidoDTO nuevoTraspasoDePedidoDTO =
                    NuevoTraspasoDePedidoDTO.builder()
                        .nroPedido(pedido.getNroPedido())
                        .idSucursalOrigen(sucursal.getIdSucursal())
                        .idUsuario(pedido.getUsuario().getIdUsuario())
                        .idProductoConCantidad(new HashMap<>())
                        .build();
                nuevosTraspasos.add(nuevoTraspasoDePedidoDTO);
              });
      for (ProductoFaltanteDTO productoFaltante : faltantes) {
        BigDecimal cantidadFaltante =
            productoFaltante
                .getCantidadSolicitada()
                .subtract(productoFaltante.getCantidadDisponible());
        Producto producto =
            productoService.getProductoNoEliminadoPorId(productoFaltante.getIdProducto());
        List<CantidadEnSucursal> listaOrdenadaPorCantidad = new ArrayList<>();
        producto.getCantidadEnSucursales().stream()
            .filter(
                cantidadEnSucursal ->
                    !cantidadEnSucursal.getIdSucursal().equals(pedido.getIdSucursal()))
            .forEach(listaOrdenadaPorCantidad::add);
        listaOrdenadaPorCantidad.sort(
            (cantidad1, cantidad2) -> cantidad2.getCantidad().compareTo(cantidad1.getCantidad()));
        for (CantidadEnSucursal cantidadEnSucursal : listaOrdenadaPorCantidad) {
          if (cantidadFaltante.compareTo(BigDecimal.ZERO) > 0) {
            if (cantidadFaltante.compareTo(cantidadEnSucursal.getCantidad()) <= 0) {
              BigDecimal cantidadFaltanteLambda = cantidadFaltante;
              nuevosTraspasos.stream()
                  .filter(
                      nuevoTraspaso ->
                          nuevoTraspaso
                              .getIdSucursalOrigen()
                              .equals(cantidadEnSucursal.getIdSucursal()))
                  .forEach(
                      nuevoTraspaso -> {
                        nuevoTraspaso.setIdSucursalDestino(pedido.getSucursal().getIdSucursal());
                        nuevoTraspaso
                            .getIdProductoConCantidad()
                            .put(producto.getIdProducto(), cantidadFaltanteLambda);
                      });
              cantidadFaltante = BigDecimal.ZERO;
            } else if (cantidadEnSucursal.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
              cantidadFaltante = cantidadFaltante.subtract(cantidadEnSucursal.getCantidad());
              nuevosTraspasos.stream()
                  .filter(
                      nuevoTraspaso ->
                          nuevoTraspaso
                              .getIdSucursalOrigen()
                              .equals(cantidadEnSucursal.getIdSucursal()))
                  .forEach(
                      nuevoTraspaso -> {
                        nuevoTraspaso.setIdSucursalDestino(pedido.getSucursal().getIdSucursal());
                        nuevoTraspaso
                            .getIdProductoConCantidad()
                            .put(producto.getIdProducto(), cantidadEnSucursal.getCantidad());
                      });
            }
          }
        }
      }
      return nuevosTraspasos;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void eliminarTraspasoDePedido(Pedido pedido) {
    Traspaso traspaso = traspasoRepository.findByNroPedido(pedido.getNroPedido());
    if (traspaso != null) {
      this.eliminar(traspasoRepository.findByNroPedido(pedido.getNroPedido()).getIdTraspaso());
    }
  }

  @Override
  public void eliminar(Long idTraspaso) {
    Traspaso traspaso = this.getTraspasoNoEliminadoPorid(idTraspaso);
    if (traspaso.getNroPedido() != null) {
      Pedido pedidoDeTraspaso =
          pedidoService.getPedidoPorNumeroAndSucursal(
              traspaso.getNroPedido(), traspaso.getSucursalDestino());
      if (pedidoDeTraspaso != null && pedidoDeTraspaso.getEstado() == EstadoPedido.CERRADO) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_traspaso_error_eliminar_con_pedido",
                new Object[] {traspaso},
                Locale.getDefault()));
      }
    }
    productoService.actualizarStockTraspaso(traspaso, TipoDeOperacion.ELIMINACION);
    traspasoRepository.delete(traspaso);
  }

  @Override
  public String generarNroDeTraspaso() {
    long min = 1L;
    long max = 99999L; // 5 digitos
    long randomLong = 0L;
    boolean esRepetido = true;
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      String nroTraspaso = Long.toString(randomLong);
      Traspaso t = traspasoRepository.findByNroTraspaso(nroTraspaso);
      if (t == null) esRepetido = false;
    }
    return Long.toString(randomLong);
  }

  @Override
  public Page<Traspaso> buscarTraspasos(BusquedaTraspasoCriteria criteria) {
    return traspasoRepository.findAll(
        this.getBuilderTraspaso(criteria),
        this.getPageable(
            (criteria.getPagina() == null || criteria.getPagina() < 0) ? 0 : criteria.getPagina(),
            criteria.getOrdenarPor(),
            criteria.getSentido(), TAMANIO_PAGINA_DEFAULT));
  }

  @Override
  public BooleanBuilder getBuilderTraspaso(BusquedaTraspasoCriteria criteria) {
    QTraspaso qTraspaso = QTraspaso.traspaso;
    BooleanBuilder builder = new BooleanBuilder();
    if (criteria.getIdSucursalOrigen() != null) {
      builder.and(qTraspaso.sucursalOrigen.idSucursal.eq(criteria.getIdSucursalOrigen()));
    }
    if (criteria.getIdSucursalDestino() != null) {
      builder.and(qTraspaso.sucursalDestino.idSucursal.eq(criteria.getIdSucursalDestino()));
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
            qTraspaso.fechaDeAlta.between(criteria.getFechaDesde(), criteria.getFechaHasta()));
      } else if (criteria.getFechaDesde() != null) {
        criteria.setFechaDesde(criteria.getFechaDesde().withHour(0).withMinute(0).withSecond(0));
        builder.and(qTraspaso.fechaDeAlta.after(criteria.getFechaDesde()));
      } else if (criteria.getFechaHasta() != null) {
        criteria.setFechaHasta(
            criteria
                .getFechaHasta()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999));
        builder.and(qTraspaso.fechaDeAlta.before(criteria.getFechaHasta()));
      }
    }
    if (criteria.getIdUsuario() != null)
      builder.and(qTraspaso.usuario.idUsuario.eq(criteria.getIdUsuario()));
    if (criteria.getNroTraspaso() != null)
      builder.and(qTraspaso.nroTraspaso.eq(criteria.getNroTraspaso()));
    if (criteria.getNroPedido() != null)
      builder.and(qTraspaso.nroPedido.eq(criteria.getNroPedido()));
    if (criteria.getIdProducto() != null)
      builder.and(qTraspaso.renglones.any().idProducto.eq(criteria.getIdProducto()));
    return builder;
  }

  @Override
  public Pageable getPageable(Integer pagina, String ordenarPor, String sentido, int tamanioPagina) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "fecha";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(
          pagina, tamanioPagina, Sort.by(Sort.Direction.DESC, ordenDefault));
    } else {
      return switch (sentido) {
        case "ASC" -> PageRequest.of(
                pagina, tamanioPagina, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC" -> PageRequest.of(
                pagina, tamanioPagina, Sort.by(Sort.Direction.DESC, ordenarPor));
        default -> PageRequest.of(
                pagina, tamanioPagina, Sort.by(Sort.Direction.DESC, ordenarPor));
      };
    }
  }

  @Override
  public byte[] getReporteTraspaso(BusquedaTraspasoCriteria criteria) {
    List<Traspaso> traspasosParaReporte =
        traspasoRepository
            .findAll(
                this.getBuilderTraspaso(criteria),
                this.getPageable(
                    (criteria.getPagina() == null || criteria.getPagina() < 0)
                        ? 0
                        : criteria.getPagina(),
                    criteria.getOrdenarPor(),
                    criteria.getSentido(), Integer.MAX_VALUE))
            .getContent();
    if (traspasosParaReporte.isEmpty()) {
      throw new BusinessServiceException(
              messageSource.getMessage(
                      "mensaje_traspaso_reporte_sin_traspasos",
                      null,
                      Locale.getDefault()));
    }
    Map<Long, RenglonReporteTraspasoDTO> renglones = new HashMap<>();
    traspasosParaReporte.forEach(traspaso ->
      traspaso.getRenglones().forEach(renglonTraspaso -> {
        if (renglones.get(renglonTraspaso.getIdProducto()) != null)
          renglones.get(renglonTraspaso.getIdProducto())
                  .setCantidad(renglones.get(renglonTraspaso.getIdProducto()).getCantidad().add(renglonTraspaso.getCantidadProducto()));
        else {
          RenglonReporteTraspasoDTO renglonReporteTraspasoDTO =
                  RenglonReporteTraspasoDTO.builder()
                          .sucursalOrigen(traspaso.getNombreSucursalOrigen())
                          .sucursalDestino(traspaso.getNombreSucursalDestino())
                          .cantidad(renglonTraspaso.getCantidadProducto())
                          .codigo(renglonTraspaso.getCodigoProducto())
                          .descripcion(renglonTraspaso.getDescripcionProducto())
                          .medida(renglonTraspaso.getNombreMedidaProducto())
                          .build();
          renglones.put(renglonTraspaso.getIdProducto(), renglonReporteTraspasoDTO);
        }
      }));
    ClassLoader classLoader = TraspasoServiceImpl.class.getClassLoader();
    InputStream isFileReport =
        classLoader.getResourceAsStream("sic/vista/reportes/Traspasos.jasper");
    JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(renglones.values());
    Map<String, Object> params = new HashMap<>();
    params.put("FechaDesde", criteria.getFechaDesde());
    params.put("FechaHasta", criteria.getFechaHasta());
    Sucursal sucursalPredeterminada = sucursalService.getSucursalPredeterminada();
    if (sucursalPredeterminada.getLogo() != null && !sucursalPredeterminada.getLogo().isEmpty()) {
      try {
        params.put(
            "logo",
            new ImageIcon(ImageIO.read(new URL(sucursalPredeterminada.getLogo()))).getImage());
      } catch (IOException ex) {
        throw new ServiceException(
            messageSource.getMessage("mensaje_sucursal_404_logo", null, Locale.getDefault()), ex);
      }
    }
    try {
      return JasperExportManager.exportReportToPdf(
          JasperFillManager.fillReport(isFileReport, params, ds));
    } catch (JRException ex) {
      throw new ServiceException(
          messageSource.getMessage("mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
  }
}
