package sic.service.impl;

import com.querydsl.core.BooleanBuilder;
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
import sic.modelo.*;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.repository.RenglonTraspasoRepository;
import sic.repository.TraspasoRepository;
import sic.service.IProductoService;
import sic.service.ISucursalService;
import sic.service.ITraspasoService;
import sic.service.IUsuarioService;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
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
      MessageSource messageSource) {
    this.traspasoRepository = traspasoRepository;
    this.renglonTraspasoRepository = renglonTraspasoRepository;
    this.productoService = productoService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
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
  public Traspaso guardar(NuevoTraspasoDTO nuevoTraspasoDTO) {
    Traspaso traspaso = new Traspaso();
    traspaso.setFechaDeAlta(LocalDateTime.now());
    traspaso.setNroTraspaso(this.generarNroDeTraspaso());
    Sucursal sucursalOrigen =
        sucursalService.getSucursalPorId(nuevoTraspasoDTO.getIdSucursalOrigen());
    Sucursal sucursalDestino =
        sucursalService.getSucursalPorId(nuevoTraspasoDTO.getIdSucursalDestino());
    traspaso.setSucursalOrigen(sucursalOrigen);
    traspaso.setSucursalDestino(sucursalDestino);
    traspaso.setNroPedido(nuevoTraspasoDTO.getNroPedido());
    traspaso.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(nuevoTraspasoDTO.getIdUsuario()));
    List<RenglonTraspaso> renglonesTraspaso = new ArrayList<>();
    nuevoTraspasoDTO
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
  public List<Traspaso> guardarTraspasosPorPedido(Pedido pedido) {
    List<Traspaso> traspasos = new ArrayList<>();
    this.construirNuevosTraspasosPorPedido(pedido).stream()
        .filter(nuevoTraspaso -> !nuevoTraspaso.getIdProductoConCantidad().isEmpty())
        .forEach(nuevoTraspaso -> traspasos.add(this.guardar(nuevoTraspaso)));
    return traspasos;
  }

  @Override
  public List<NuevoTraspasoDTO> construirNuevosTraspasosPorPedido(Pedido pedido) {
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
      List<NuevoTraspasoDTO> nuevosTraspasos = new ArrayList<>();
      sucursales.stream()
          .filter(sucursal -> sucursal.getIdSucursal() != pedido.getSucursal().getIdSucursal())
          .forEach(
              sucursal -> {
                NuevoTraspasoDTO nuevoTraspasoDTO =
                    NuevoTraspasoDTO.builder()
                        .nroPedido(pedido.getNroPedido())
                        .idSucursalOrigen(sucursal.getIdSucursal())
                        .idUsuario(pedido.getUsuario().getIdUsuario())
                        .idProductoConCantidad(new HashMap<>())
                        .build();
                nuevosTraspasos.add(nuevoTraspasoDTO);
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
          if (cantidadFaltante.compareTo(BigDecimal.ZERO) != 0) {
            if (cantidadFaltante.compareTo(cantidadEnSucursal.getCantidad()) <= 0
                && cantidadEnSucursal.getCantidad().compareTo(BigDecimal.ZERO) != 0) {
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
            } else if (cantidadEnSucursal.getCantidad().compareTo(BigDecimal.ZERO) != 0) {
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
    if (traspaso.getNroPedido() != null
        && traspasoRepository.findByNroPedido(traspaso.getNroPedido()) != null) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_traspaso_error_eliminar_con_pedido",
              new Object[] {traspaso},
              Locale.getDefault()));
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
            criteria.getSentido()));
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
    return builder;
  }

  @Override
  public Pageable getPageable(Integer pagina, String ordenarPor, String sentido) {
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
              pagina, TAMANIO_PAGINA_DEFAULT, Sort.by(Sort.Direction.DESC, ordenarPor));
      }
    }
  }
}
