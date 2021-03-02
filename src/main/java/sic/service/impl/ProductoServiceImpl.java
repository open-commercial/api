package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import sic.modelo.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.exception.BusinessServiceException;
import sic.exception.ServiceException;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.*;
import sic.modelo.embeddable.CantidadProductoEmbeddable;
import sic.modelo.embeddable.PrecioProductoEmbeddable;
import sic.repository.ProductoFavoritoRepository;
import sic.repository.ProductoRepository;
import sic.service.*;
import sic.util.CalculosComprobante;
import sic.util.CustomValidator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ProductoServiceImpl implements IProductoService {

  private final ProductoRepository productoRepository;
  private final ProductoFavoritoRepository productoFavoritoRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final BigDecimal CIEN = new BigDecimal("100");
  private static final long TAMANIO_MAXIMO_IMAGEN = 1024000L;
  private final IRubroService rubroService;
  private final IProveedorService proveedorService;
  private final IMedidaService medidaService;
  private final ICarritoCompraService carritoCompraService;
  private final IPhotoVideoUploader photoVideoUploader;
  private final ISucursalService sucursalService;
  private final ITraspasoService traspasoService;
  private final IPedidoService pedidoService;
  private final IClienteService clienteService;
  private final ICorreoElectronicoService correoElectronicoService;
  private static final int TAMANIO_PAGINA_DEFAULT = 15;
  private static final String FORMATO_XLSX = "xlsx";
  private static final String FORMATO_PDF = "pdf";
  private static final String URL_PRODUCTO_SIN_IMAGEN = "https://res.cloudinary.com/hf0vu1bg2/image/upload/q_10/f_jpg/v1545616229/assets/sin_imagen.jpg";
  private final MessageSource messageSource;
  private final CustomValidator customValidator;

  @Autowired
  @Lazy
  public ProductoServiceImpl(
    ProductoRepository productoRepository,
    ProductoFavoritoRepository productoFavoritoRepository,
    IRubroService rubroService,
    IProveedorService proveedorService,
    IMedidaService medidaService,
    ICarritoCompraService carritoCompraService,
    IPhotoVideoUploader photoVideoUploader,
    ISucursalService sucursalService,
    ITraspasoService traspasoService,
    IPedidoService pedidoService,
    IClienteService clienteService,
    ICorreoElectronicoService correoElectronicoService,
    MessageSource messageSource,
    CustomValidator customValidator) {
    this.productoRepository = productoRepository;
    this.productoFavoritoRepository = productoFavoritoRepository;
    this.rubroService = rubroService;
    this.proveedorService = proveedorService;
    this.medidaService = medidaService;
    this.carritoCompraService = carritoCompraService;
    this.photoVideoUploader = photoVideoUploader;
    this.sucursalService = sucursalService;
    this.traspasoService = traspasoService;
    this.pedidoService = pedidoService;
    this.clienteService = clienteService;
    this.correoElectronicoService = correoElectronicoService;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Producto producto) {
    if (producto.getPrecioProducto().isOferta()
        && producto.getPrecioProducto().getPorcentajeBonificacionOferta().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_oferta_inferior_0", null, Locale.getDefault()));
    }
    // Codigo
    if (!producto.getCodigo().equals("")) {
      Producto productoDuplicado = this.getProductoPorCodigo(producto.getCodigo());
      if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
          && productoDuplicado != null
          && !producto.getIdProducto().equals(productoDuplicado.getIdProducto())) {
        throw new BusinessServiceException(
            messageSource.getMessage(
                "mensaje_producto_duplicado_codigo", null, Locale.getDefault()));
      }
      if (operacion.equals(TipoDeOperacion.ALTA)
          && productoDuplicado != null
          && !producto.getCodigo().equals("")) {
        throw new BusinessServiceException(
          messageSource.getMessage(
            "mensaje_producto_duplicado_codigo", null, Locale.getDefault()));
      }
    }
    // Descripcion
    Producto productoDuplicado = this.getProductoPorDescripcion(producto.getDescripcion());
    if (operacion.equals(TipoDeOperacion.ALTA) && productoDuplicado != null) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_duplicado_descripcion", null, Locale.getDefault()));
    }
    if (operacion.equals(TipoDeOperacion.ACTUALIZACION)
        && productoDuplicado != null
        && !producto.getIdProducto().equals(productoDuplicado.getIdProducto()))
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_duplicado_descripcion", null, Locale.getDefault()));
    this.validarCalculos(producto);
  }

  @Override
  public void validarCalculos(Producto producto) {
    Double[] iva = {10.5, 21.0, 0.0};
    if (!Arrays.asList(iva).contains(producto.getPrecioProducto().getIvaPorcentaje().doubleValue())) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_error_iva_no_valido",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getPrecioProducto().getGananciaNeto()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularGananciaNeto(
                        producto.getPrecioProducto().getPrecioCosto(), producto.getPrecioProducto().getGananciaPorcentaje())
                    .setScale(3, RoundingMode.HALF_UP))
        != 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_ganancia_neta_incorrecta",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getPrecioProducto().getPrecioVentaPublico()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularPVP(producto.getPrecioProducto().getPrecioCosto(), producto.getPrecioProducto().getGananciaPorcentaje())
                    .setScale(3, RoundingMode.HALF_UP))
        != 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_precio_venta_publico_incorrecto",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getPrecioProducto().getIvaNeto()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularIVANeto(producto.getPrecioProducto().getPrecioVentaPublico(), producto.getPrecioProducto().getIvaPorcentaje())
                    .setScale(3, RoundingMode.HALF_UP))
        != 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_iva_neto_incorrecto",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getPrecioProducto().getPrecioLista()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularPrecioLista(
                        producto.getPrecioProducto().getPrecioVentaPublico(), producto.getPrecioProducto().getIvaPorcentaje())
                    .setScale(3, RoundingMode.HALF_UP))
        != 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_precio_lista_incorrecto",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
  }

  @Override
  public Page<Producto> buscarProductos(BusquedaProductoCriteria criteria, Long idSucursal) {
    Page<Producto> productos =
        productoRepository.findAll(
            this.getBuilder(criteria),
            this.getPageable(
                criteria.getPagina(),
                criteria.getOrdenarPor(),
                criteria.getSentido(),
                TAMANIO_PAGINA_DEFAULT));
    productos.stream()
        .forEach(
            producto ->
              this.calcularCantidadEnSucursalesDisponible(producto, idSucursal));
    return productos;
  }

  @Override
  public void marcarFavoritos(Page<Producto> productos, long idUsuario) {
    List<Producto> productosFavoritos = this.getProductosFavoritosDelClientePorIdUsuario(idUsuario);
    productos.forEach(p -> {
      if (productosFavoritos.contains(p)) p.setFavorito(true);
    });
  }

  @Override
  public List<Producto> buscarProductosParaReporte(BusquedaProductoCriteria criteria) {
    criteria.setPagina(0);
    return productoRepository
        .findAll(
            this.getBuilder(criteria),
            this.getPageable(
                criteria.getPagina(),
                criteria.getOrdenarPor(),
                criteria.getSentido(),
                Integer.MAX_VALUE))
        .getContent();
  }

  @Override
  public Pageable getPageable(Integer pagina, String ordenarPor, String sentido, int tamanioPagina) {
    if (pagina == null) pagina = 0;
    String ordenDefault = "descripcion";
    if (ordenarPor == null || sentido == null) {
      return PageRequest.of(pagina, tamanioPagina, Sort.by(Sort.Direction.ASC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          return PageRequest.of(pagina, tamanioPagina, Sort.by(Sort.Direction.ASC, ordenarPor));
        case "DESC":
          return PageRequest.of(pagina, tamanioPagina, Sort.by(Sort.Direction.DESC, ordenarPor));
        default:
          return PageRequest.of(pagina, tamanioPagina, Sort.by(Sort.Direction.DESC, ordenDefault));
      }
    }
  }

  @Override
  public BooleanBuilder getBuilder(BusquedaProductoCriteria criteria) {
    QProducto qProducto = QProducto.producto;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qProducto.eliminado.eq(false));
    if (criteria.getCodigo() != null && criteria.getDescripcion() != null)
      builder.and(
          qProducto
              .codigo
              .containsIgnoreCase(criteria.getCodigo())
              .or(this.buildPredicadoDescripcion(criteria.getDescripcion(), qProducto)));
    else {
      if (criteria.getCodigo() != null)
        builder.and(qProducto.codigo.containsIgnoreCase(criteria.getCodigo()));
      if (criteria.getDescripcion() != null)
        builder.and(this.buildPredicadoDescripcion(criteria.getDescripcion(), qProducto));
    }
    if (criteria.getIdRubro() != null)
      builder.and(qProducto.rubro.idRubro.eq(criteria.getIdRubro()));
    if (criteria.getIdProveedor() != null)
      builder.and(qProducto.proveedor.idProveedor.eq(criteria.getIdProveedor()));
    if (criteria.isListarSoloFaltantes())
      builder
          .and(qProducto.cantidadProducto.cantidadEnSucursales.any().cantidad.loe(qProducto.cantidadProducto.cantMinima))
          .and(qProducto.cantidadProducto.ilimitado.eq(false));
    if (criteria.isListarSoloEnStock())
      builder
          .and(qProducto.cantidadProducto.cantidadEnSucursales.any().cantidad.gt(BigDecimal.ZERO))
          .and(qProducto.cantidadProducto.ilimitado.eq(false));
    if (criteria.getPublico() != null) {
      if (criteria.getPublico()) builder.and(qProducto.publico.isTrue());
      else builder.and(qProducto.publico.isFalse());
    }
    if (criteria.getOferta() != null && criteria.getOferta())
      builder.and(qProducto.precioProducto.oferta.isTrue());
    return builder;
  }

  private BooleanBuilder buildPredicadoDescripcion(String descripcion, QProducto qProducto) {
    String[] terminos = descripcion.split(" ");
    BooleanBuilder descripcionProducto = new BooleanBuilder();
    for (String termino : terminos) {
      descripcionProducto.and(qProducto.descripcion.containsIgnoreCase(termino));
    }
    return descripcionProducto;
  }

  @Override
  @Transactional
  public Producto guardar(
      NuevoProductoDTO nuevoProductoDTO, long idMedida, long idRubro, long idProveedor) {
    customValidator.validar(nuevoProductoDTO);
    if (nuevoProductoDTO.getCodigo() == null) nuevoProductoDTO.setCodigo("");
    Producto producto = new Producto();
    producto.setMedida(medidaService.getMedidaNoEliminadaPorId(idMedida));
    producto.setRubro(rubroService.getRubroNoEliminadoPorId(idRubro));
    producto.setProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor));
    producto.setCodigo(nuevoProductoDTO.getCodigo());
    producto.setDescripcion(nuevoProductoDTO.getDescripcion());
    Set<CantidadEnSucursal> altaCantidadesEnSucursales = new HashSet<>();
    sucursalService
        .getSucusales(false)
        .forEach(
            sucursal -> {
              CantidadEnSucursal cantidad = new CantidadEnSucursal();
              cantidad.setCantidad(BigDecimal.ZERO);
              cantidad.setSucursal(sucursal);
              altaCantidadesEnSucursales.add(cantidad);
            });
    producto.setCantidadProducto(new CantidadProductoEmbeddable());
    producto.getCantidadProducto().setCantidadEnSucursales(altaCantidadesEnSucursales);
    producto
        .getCantidadProducto()
        .getCantidadEnSucursales()
        .forEach(
            cantidadEnSucursal ->
                nuevoProductoDTO.getCantidadEnSucursal().keySet().stream()
                    .filter(idSucursal -> idSucursal.equals(cantidadEnSucursal.getIdSucursal()))
                    .forEach(
                        idSucursal ->
                          cantidadEnSucursal.setCantidad(
                              nuevoProductoDTO.getCantidadEnSucursal().get(idSucursal))
                        ));
    producto.getCantidadProducto().setCantidadTotalEnSucursales(
        producto.getCantidadProducto().getCantidadEnSucursales().stream()
            .map(CantidadEnSucursal::getCantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    producto.getCantidadProducto().setCantMinima(nuevoProductoDTO.getCantMinima());
    producto.getCantidadProducto().setBulto(nuevoProductoDTO.getBulto());
    producto.setPrecioProducto(new PrecioProductoEmbeddable());
    producto.getPrecioProducto().setPrecioCosto(nuevoProductoDTO.getPrecioCosto());
    producto.getPrecioProducto().setGananciaPorcentaje(nuevoProductoDTO.getGananciaPorcentaje());
    producto.getPrecioProducto().setGananciaNeto(nuevoProductoDTO.getGananciaNeto());
    producto.getPrecioProducto().setPrecioVentaPublico(nuevoProductoDTO.getPrecioVentaPublico());
    producto.getPrecioProducto().setIvaPorcentaje(nuevoProductoDTO.getIvaPorcentaje());
    producto.getPrecioProducto().setIvaNeto(nuevoProductoDTO.getIvaNeto());
    producto.getPrecioProducto().setPrecioLista(nuevoProductoDTO.getPrecioLista());
    producto.getPrecioProducto().setOferta(nuevoProductoDTO.isOferta());
    producto.getPrecioProducto().setPorcentajeBonificacionOferta(
        nuevoProductoDTO.getPorcentajeBonificacionOferta() != null
            ? nuevoProductoDTO.getPorcentajeBonificacionOferta()
            : BigDecimal.ZERO);
    producto.getPrecioProducto().setPorcentajeBonificacionPrecio(
        nuevoProductoDTO.getPorcentajeBonificacionPrecio() != null
            ? nuevoProductoDTO.getPorcentajeBonificacionPrecio()
            : BigDecimal.ZERO);
    producto.setPublico(nuevoProductoDTO.isPublico());
    producto.setNota(nuevoProductoDTO.getNota());
    producto.setFechaVencimiento(nuevoProductoDTO.getFechaVencimiento());
    producto.setFechaAlta(LocalDateTime.now());
    producto.setFechaUltimaModificacion(LocalDateTime.now());
    this.calcularPrecioBonificado(producto);
    this.validarReglasDeNegocio(TipoDeOperacion.ALTA, producto);
    producto.getCantidadProducto().setIlimitado(false);
    producto.getCantidadProducto().setCantidadReservada(BigDecimal.ZERO);
    producto = productoRepository.save(producto);
    logger.warn(
        messageSource.getMessage(
            "mensaje_producto_guardado", new Object[] {producto}, Locale.getDefault()));
    if (nuevoProductoDTO.getImagen() != null)
      producto.setUrlImagen(
          this.subirImagenProducto(producto.getIdProducto(), nuevoProductoDTO.getImagen()));
    return producto;
  }

  @Override
  @Transactional
  public void actualizar(Producto productoPorActualizar, Producto productoPersistido, byte[] imagen) {
    productoPorActualizar.setFechaAlta(productoPersistido.getFechaAlta());
    productoPorActualizar.setFechaUltimaModificacion(LocalDateTime.now());
    customValidator.validar(productoPorActualizar);
    productoPorActualizar.setEliminado(productoPersistido.isEliminado());
    if ((productoPersistido.getUrlImagen() != null && !productoPersistido.getUrlImagen().isEmpty())
            && (productoPorActualizar.getUrlImagen() == null
            || productoPorActualizar.getUrlImagen().isEmpty())) {
      photoVideoUploader.borrarImagen(
              Producto.class.getSimpleName() + productoPersistido.getIdProducto());
    }
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, productoPorActualizar);
    this.calcularPrecioBonificado(productoPorActualizar);
    if (productoPersistido.isPublico() && !productoPorActualizar.isPublico()) {
      carritoCompraService.eliminarItem(productoPersistido.getIdProducto());
    }
    //se setea siempre en false momentaniamente
    productoPorActualizar.getCantidadProducto().setIlimitado(false);
    productoPorActualizar.setVersion(productoPersistido.getVersion());
    photoVideoUploader.isUrlValida(productoPorActualizar.getUrlImagen());
    productoPorActualizar = productoRepository.save(productoPorActualizar);
    logger.warn(
        messageSource.getMessage(
            "mensaje_producto_actualizado",
            new Object[] {productoPorActualizar},
            Locale.getDefault()));
    if (imagen != null) this.subirImagenProducto(productoPorActualizar.getIdProducto(), imagen);
  }

  private void calcularPrecioBonificado(Producto producto) {
    producto.getPrecioProducto().setPrecioBonificado(producto.getPrecioProducto().getPrecioLista());
    if (producto.getPrecioProducto().isOferta()
        && producto.getPrecioProducto().getPorcentajeBonificacionOferta() != null
        && producto.getPrecioProducto().getPorcentajeBonificacionOferta().compareTo(BigDecimal.ZERO) > 0) {
      producto.getPrecioProducto().setPrecioBonificado(
          producto
                  .getPrecioProducto().getPrecioLista()
              .subtract(
                  CalculosComprobante.calcularProporcion(
                      producto.getPrecioProducto().getPrecioLista(), producto.getPrecioProducto().getPorcentajeBonificacionOferta())));
    } else if (producto.getPrecioProducto().getPorcentajeBonificacionPrecio() != null
        && producto.getPrecioProducto().getPorcentajeBonificacionPrecio().compareTo(BigDecimal.ZERO) > 0) {
      producto.getPrecioProducto().setOferta(false);
      producto.getPrecioProducto().setPrecioBonificado(
          producto
                  .getPrecioProducto().getPrecioLista()
              .subtract(
                  CalculosComprobante.calcularProporcion(
                      producto.getPrecioProducto().getPrecioLista(), producto.getPrecioProducto().getPorcentajeBonificacionPrecio())));
    }
  }

  @Override
  public void devolverStockPedido(
      Pedido pedido, TipoDeOperacion tipoDeOperacion, List<RenglonPedido> renglonesAnteriores, Long idSucursalOrigen) {
    if (tipoDeOperacion == TipoDeOperacion.ACTUALIZACION
        && pedido.getEstado() == EstadoPedido.ABIERTO
        && renglonesAnteriores != null
        && !renglonesAnteriores.isEmpty()) {
      renglonesAnteriores.forEach(
          renglonAnterior -> {
            Optional<Producto> productoAnterior =
                productoRepository.findById(renglonAnterior.getIdProductoItem());
            if (productoAnterior.isPresent() && !productoAnterior.get().getCantidadProducto().isIlimitado()) {
              this.agregarStock(
                  productoAnterior.get(),
                  (idSucursalOrigen != null ? idSucursalOrigen : pedido.getSucursal().getIdSucursal()),
                  renglonAnterior.getCantidad());
            } else {
              logger.warn(
                  messageSource.getMessage(
                      "mensaje_error_actualizar_stock_producto_eliminado",
                      null,
                      Locale.getDefault()));
            }
          });
    }
  }

  @Override
  public void actualizarStockPedido(Pedido pedido, TipoDeOperacion tipoDeOperacion) {
    switch (tipoDeOperacion) {
      case ALTA -> traspasoService.guardarTraspasosPorPedido(pedido);
      case ELIMINACION -> traspasoService.eliminarTraspasoDePedido(pedido);
      case ACTUALIZACION -> {
        if (pedido.getEstado() == EstadoPedido.ABIERTO) {
          traspasoService.eliminarTraspasoDePedido(pedido);
          traspasoService.guardarTraspasosPorPedido(pedido);
        } else if (pedido.getEstado() == EstadoPedido.CANCELADO) {
          traspasoService.eliminarTraspasoDePedido(pedido);
        }
      }
    }
    pedido
        .getRenglones()
        .forEach(
            renglones -> {
              Optional<Producto> producto =
                  productoRepository.findById(renglones.getIdProductoItem());
              if (producto.isPresent() && !producto.get().getCantidadProducto().isIlimitado()) {
                if (tipoDeOperacion == TipoDeOperacion.ALTA
                    || (tipoDeOperacion == TipoDeOperacion.ACTUALIZACION
                        && pedido.getEstado() == EstadoPedido.ABIERTO)) {
                  this.quitarStock(
                      producto.get(),
                      pedido.getSucursal().getIdSucursal(),
                      renglones.getCantidad());
                }
                if ((tipoDeOperacion == TipoDeOperacion.ELIMINACION
                        && pedido.getEstado() == EstadoPedido.ABIERTO)
                    || (tipoDeOperacion == TipoDeOperacion.ACTUALIZACION
                        && pedido.getEstado() == EstadoPedido.CANCELADO)) {
                  this.agregarStock(
                      producto.get(),
                      pedido.getSucursal().getIdSucursal(),
                      renglones.getCantidad());
                }
              } else {
                logger.warn(
                    messageSource.getMessage(
                        "mensaje_error_actualizar_stock_producto_eliminado",
                        null,
                        Locale.getDefault()));
              }
            });
  }

  @Override
  public void actualizarStockFacturaCompra(
      Map<Long, BigDecimal> idsYCantidades,
      Long idSucursal,
      TipoDeOperacion operacion,
      Movimiento movimiento) {
    idsYCantidades.forEach(
        (idProducto, cantidad) -> {
          Optional<Producto> producto = productoRepository.findById(idProducto);
          if (producto.isPresent() && !producto.get().getCantidadProducto().isIlimitado()) {
            if (movimiento == Movimiento.COMPRA && operacion == TipoDeOperacion.ELIMINACION) {
              this.quitarStock(producto.get(), idSucursal, cantidad);
            }
            if (movimiento == Movimiento.COMPRA && operacion == TipoDeOperacion.ALTA) {
              this.agregarStock(producto.get(), idSucursal, cantidad);
            }
          } else {
            logger.warn(
                messageSource.getMessage(
                    "mensaje_error_actualizar_stock_producto_eliminado",
                    null,
                    Locale.getDefault()));
          }
        });
  }

  @Override
  public void actualizarStockNotaCredito(
      Map<Long, BigDecimal> idsYCantidades, Long idSucursal, TipoDeOperacion operacion, Movimiento movimiento) {
    idsYCantidades.forEach(
        (idProducto, cantidad) -> {
          Optional<Producto> producto = productoRepository.findById(idProducto);
          if (producto.isPresent() && !producto.get().isEliminado() && !producto.get().getCantidadProducto().isIlimitado()) {
            switch (operacion) {
              case ALTA -> {
                switch (movimiento) {
                  case VENTA -> this.agregarStock(producto.get(), idSucursal, cantidad);
                  case COMPRA -> this.quitarStock(producto.get(), idSucursal, cantidad);
                  default -> throw new BusinessServiceException(
                          messageSource.getMessage("mensaje_preference_tipo_de_movimiento_no_soportado", null, Locale.getDefault()));
                }
              }
              case ELIMINACION -> {
                switch (movimiento) {
                  case VENTA -> this.quitarStock(producto.get(), idSucursal, cantidad);
                  case COMPRA -> this.agregarStock(producto.get(), idSucursal, cantidad);
                  default -> throw new BusinessServiceException(
                          messageSource.getMessage("mensaje_preference_tipo_de_movimiento_no_soportado", null, Locale.getDefault()));
                }
              }
              default -> throw new BusinessServiceException(
                      messageSource.getMessage("mensaje_operacion_no_soportada", null, Locale.getDefault()));
            }
          } else {
            logger.warn(
                messageSource.getMessage(
                    "mensaje_error_actualizar_stock_producto_eliminado",
                    null,
                    Locale.getDefault()));
          }
        });
  }

  @Override
  public void actualizarStockTraspaso(Traspaso traspaso, TipoDeOperacion tipoDeOperacion) {
    switch (tipoDeOperacion) {
      case ALTA -> {
        //control de stock
        long[] idProducto = new long[traspaso.getRenglones().size()];
        BigDecimal[] cantidad = new BigDecimal[traspaso.getRenglones().size()];
        int indice = 0;
        for (RenglonTraspaso renglon : traspaso.getRenglones()) {
          idProducto[indice] = renglon.getIdProducto();
          cantidad[indice] = renglon.getCantidadProducto();
          indice++;
        }
        ProductosParaVerificarStockDTO productosParaVerificarStockDTO =
                ProductosParaVerificarStockDTO.builder()
                        .idProducto(idProducto)
                        .cantidad(cantidad)
                        .idSucursal(traspaso.getSucursalOrigen().getIdSucursal())
                        .build();
        if (!this.getProductosSinStockDisponibleParaTraspaso(productosParaVerificarStockDTO).isEmpty()) {
          throw new BusinessServiceException(
                  messageSource.getMessage("mensaje_traspaso_sin_stock", null, Locale.getDefault()));
        }
        traspaso
        .getRenglones()
        .forEach(
                renglonTraspaso -> {
                  Producto producto =
                          this.getProductoNoEliminadoPorId(renglonTraspaso.getIdProducto());
                  this.quitarStock(
                          producto,
                          traspaso.getSucursalOrigen().getIdSucursal(),
                          renglonTraspaso.getCantidadProducto());
                  this.agregarStock(
                          producto,
                          traspaso.getSucursalDestino().getIdSucursal(),
                          renglonTraspaso.getCantidadProducto());
                });
      }
      case ELIMINACION -> traspaso
              .getRenglones()
              .forEach(
                      renglonTraspaso -> {
                        Optional<Producto> producto =
                                productoRepository.findById(renglonTraspaso.getIdProducto());
                        if (producto.isPresent()) {
                          this.quitarStock(
                                  producto.get(),
                                  traspaso.getSucursalDestino().getIdSucursal(),
                                  renglonTraspaso.getCantidadProducto());
                          this.agregarStock(
                                  producto.get(),
                                  traspaso.getSucursalOrigen().getIdSucursal(),
                                  renglonTraspaso.getCantidadProducto());
                        }
                      });
      default -> throw new BusinessServiceException(
              messageSource.getMessage(
                      "mensaje_operacion_no_soportada", null, Locale.getDefault()));
    }
  }

  private Producto agregarStock(Producto producto, long idSucursal, BigDecimal cantidad) {
    producto
        .getCantidadProducto().getCantidadEnSucursales()
        .forEach(
            cantidadEnSucursal -> {
              if (cantidadEnSucursal.getSucursal().getIdSucursal() == idSucursal) {
                cantidadEnSucursal.setCantidad(cantidadEnSucursal.getCantidad().add(cantidad));
              }
            });
    producto.getCantidadProducto().setCantidadTotalEnSucursales(
        producto.getCantidadProducto().getCantidadEnSucursales().stream()
            .map(CantidadEnSucursal::getCantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    producto = productoRepository.save(producto);
    logger.warn(
        messageSource.getMessage(
            "mensaje_producto_agrega_stock",
            new Object[] {cantidad, producto},
            Locale.getDefault()));
    return producto;
  }

  private Producto quitarStock(Producto producto, long idSucursal, BigDecimal cantidad) {
    producto
        .getCantidadProducto().getCantidadEnSucursales()
        .forEach(
            cantidadEnSucursal -> {
              if (cantidadEnSucursal.getSucursal().getIdSucursal() == idSucursal) {
                cantidadEnSucursal.setCantidad(cantidadEnSucursal.getCantidad().subtract(cantidad));
              }
            });
    producto.getCantidadProducto().setCantidadTotalEnSucursales(
        producto.getCantidadProducto().getCantidadEnSucursales().stream()
            .map(CantidadEnSucursal::getCantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    producto = productoRepository.save(producto);
    logger.warn(
        messageSource.getMessage(
            "mensaje_producto_quita_stock",
            new Object[] {cantidad, producto},
            Locale.getDefault()));
    return producto;
  }

  @Override
  @Transactional
  public void eliminarMultiplesProductos(long[] idProducto) {
    if (this.contieneDuplicados(idProducto)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_error_ids_duplicados", null, Locale.getDefault()));
    }
    List<Producto> productos = new ArrayList<>();
    for (Long i : idProducto) {
      Producto producto = this.getProductoNoEliminadoPorId(i);
      if (producto == null) {
        throw new EntityNotFoundException(messageSource.getMessage(
          "mensaje_producto_no_existente", null, Locale.getDefault()));
      }
      carritoCompraService.eliminarItem(i);
      producto.setEliminado(true);
      if (producto.getUrlImagen() != null && !producto.getUrlImagen().isEmpty()) {
        photoVideoUploader.borrarImagen(Producto.class.getSimpleName() + producto.getIdProducto());
      }
      productos.add(producto);
    }
    productoRepository.saveAll(productos);
  }

  @Override
  @Transactional
  public void actualizarMultiples(ProductosParaActualizarDTO productosParaActualizarDTO, Usuario usuarioLogueado) {
    boolean actualizaPrecios =
        productosParaActualizarDTO.getGananciaPorcentaje() != null
            && productosParaActualizarDTO.getIvaPorcentaje() != null
            && productosParaActualizarDTO.getPrecioCosto() != null;
    boolean aplicaDescuentoRecargoPorcentaje = productosParaActualizarDTO.getDescuentoRecargoPorcentaje() != null;
    if (aplicaDescuentoRecargoPorcentaje && actualizaPrecios) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_modificar_producto_no_permitido", null, Locale.getDefault()));
    }
    // Requeridos
    if (this.contieneDuplicados(productosParaActualizarDTO.getIdProducto())) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_error_ids_duplicados", null, Locale.getDefault()));
    }
    List<Producto> productos = new ArrayList<>();
    for (long i : productosParaActualizarDTO.getIdProducto()) {
      productos.add(this.getProductoNoEliminadoPorId(i));
    }
    BigDecimal multiplicador = BigDecimal.ZERO;
    if (aplicaDescuentoRecargoPorcentaje) {
      if (productosParaActualizarDTO.getDescuentoRecargoPorcentaje().compareTo(BigDecimal.ZERO)
          > 0) {
        multiplicador =
            productosParaActualizarDTO
                .getDescuentoRecargoPorcentaje()
                .divide(CIEN, 15, RoundingMode.HALF_UP)
                .add(BigDecimal.ONE);
      } else {
        multiplicador =
            BigDecimal.ONE.subtract(
                productosParaActualizarDTO
                    .getDescuentoRecargoPorcentaje()
                    .abs()
                    .divide(CIEN, 15, RoundingMode.HALF_UP));
      }
    }
    for (Producto p : productos) {
      if (productosParaActualizarDTO.getIdMedida() != null) {
        p.setMedida(medidaService.getMedidaNoEliminadaPorId(productosParaActualizarDTO.getIdMedida()));
      }
      if (productosParaActualizarDTO.getIdRubro() != null) {
        Rubro rubro = rubroService.getRubroNoEliminadoPorId(productosParaActualizarDTO.getIdRubro());
        p.setRubro(rubro);
      }
      if (productosParaActualizarDTO.getIdProveedor() != null) {
        Proveedor proveedor =
            proveedorService.getProveedorNoEliminadoPorId(productosParaActualizarDTO.getIdProveedor());
        p.setProveedor(proveedor);
      }
      if (usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR) && productosParaActualizarDTO.getCantidadVentaMinima() != null
                && productosParaActualizarDTO.getCantidadVentaMinima().compareTo(BigDecimal.ZERO) > 0) {
          p.getCantidadProducto().setBulto(productosParaActualizarDTO.getCantidadVentaMinima());
      }
      if (actualizaPrecios) {
        p.getPrecioProducto().setPrecioCosto(productosParaActualizarDTO.getPrecioCosto());
        p.getPrecioProducto().setGananciaPorcentaje(productosParaActualizarDTO.getGananciaPorcentaje());
        p.getPrecioProducto().setGananciaNeto(this.calcularGananciaNeto(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setPrecioVentaPublico(this.calcularPVP(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setIvaPorcentaje(productosParaActualizarDTO.getIvaPorcentaje());
        p.getPrecioProducto().setIvaNeto(this.calcularIVANeto(p.getPrecioProducto().getPrecioVentaPublico(), p.getPrecioProducto().getIvaPorcentaje()));
        p.getPrecioProducto().setPrecioLista(
                this.calcularPrecioLista(p.getPrecioProducto().getPrecioVentaPublico(), p.getPrecioProducto().getIvaPorcentaje()));
        p.setFechaUltimaModificacion(LocalDateTime.now());
        if (productosParaActualizarDTO.getPorcentajeBonificacionOferta() != null
                && productosParaActualizarDTO.getPorcentajeBonificacionOferta().compareTo(BigDecimal.ZERO)
                >= 0) {
          p.getPrecioProducto().setOferta(true);
          p.getPrecioProducto().setPorcentajeBonificacionOferta(
                  productosParaActualizarDTO.getPorcentajeBonificacionOferta());
        } else {
          p.getPrecioProducto().setOferta(false);
        }
      }
      if (aplicaDescuentoRecargoPorcentaje) {
        p.getPrecioProducto().setPrecioCosto(p.getPrecioProducto().getPrecioCosto().multiply(multiplicador));
        p.getPrecioProducto().setGananciaNeto(this.calcularGananciaNeto(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setPrecioVentaPublico(this.calcularPVP(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setIvaNeto(this.calcularIVANeto(p.getPrecioProducto().getPrecioVentaPublico(), p.getPrecioProducto().getIvaPorcentaje()));
        p.getPrecioProducto().setPrecioLista(
            this.calcularPrecioLista(p.getPrecioProducto().getPrecioVentaPublico(), p.getPrecioProducto().getIvaPorcentaje()));
        p.setFechaUltimaModificacion(LocalDateTime.now());
      }
      if (productosParaActualizarDTO.getIdMedida() != null
          || productosParaActualizarDTO.getIdRubro() != null
          || productosParaActualizarDTO.getIdProveedor() != null
          || actualizaPrecios
          || aplicaDescuentoRecargoPorcentaje) {
        p.setFechaUltimaModificacion(LocalDateTime.now());
      }
      if (productosParaActualizarDTO.getPublico() != null) {
        p.setPublico(productosParaActualizarDTO.getPublico());
        if (!productosParaActualizarDTO.getPublico())
          carritoCompraService.eliminarItem(p.getIdProducto());
      }
      if (productosParaActualizarDTO.getPorcentajeBonificacionPrecio() != null
          && productosParaActualizarDTO.getPorcentajeBonificacionPrecio().compareTo(BigDecimal.ZERO)
              >= 0) {
        p.getPrecioProducto().setPorcentajeBonificacionPrecio(
            productosParaActualizarDTO.getPorcentajeBonificacionPrecio());
      }
      this.calcularPrecioBonificado(p);
      this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, p);
    }
    productoRepository.saveAll(productos);
    logger.warn("Los Productos {} se modificaron correctamente.", productos);
  }

  @Override
  @Transactional
  public void guardarCantidadesDeSucursalNueva(Sucursal sucursal) {
    CantidadEnSucursal cantidadNueva = new CantidadEnSucursal();
    cantidadNueva.setSucursal(sucursal);
    cantidadNueva.setCantidad(BigDecimal.ZERO);
    List<Producto> productos =
      productoRepository.findAllByEliminado(false);
    productos.forEach(producto -> producto.getCantidadProducto().getCantidadEnSucursales().add(cantidadNueva));
    this.productoRepository.saveAll(productos);
  }

  @Override
  @Transactional
  public String subirImagenProducto(long idProducto, byte[] imagen) {
    if (imagen.length > TAMANIO_MAXIMO_IMAGEN)
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_error_tamanio_no_valido", null, Locale.getDefault()));
    String urlImagen =
        photoVideoUploader.subirImagen(Producto.class.getSimpleName() + idProducto, imagen);
    productoRepository.actualizarUrlImagen(idProducto, urlImagen);
    return urlImagen;
  }

  @Override
  public Producto getProductoNoEliminadoPorId(long idProducto) {
    Optional<Producto> producto = productoRepository.findById(idProducto);
    if (producto.isPresent() && !producto.get().isEliminado()) {
      return producto.get();
    } else {
      throw new EntityNotFoundException(
          messageSource.getMessage("mensaje_producto_no_existente", null, Locale.getDefault()));
    }
  }

  @Override
  public boolean isFavorito(long idUsuario, long idProducto) {
    Producto producto = this.getProductoNoEliminadoPorId(idProducto);
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    return productoFavoritoRepository.existsByClienteAndProducto(cliente, producto);
  }

  @Override
  public Producto getProductoPorCodigo(String codigo) {
    if (codigo.isEmpty()) {
      return null;
    } else {
      return productoRepository.findByCodigoAndEliminado(codigo, false);
    }
  }

  @Override
  public Producto getProductoPorDescripcion(String descripcion) {
    return productoRepository.findByDescripcionAndEliminado(descripcion, false);
  }

  @Override
  public BigDecimal calcularValorStock(BusquedaProductoCriteria criteria) {
    return productoRepository.calcularValorStock(this.getBuilder(criteria));
  }

  @Override
  public List<ProductoFaltanteDTO> getProductosSinStockDisponible(
      ProductosParaVerificarStockDTO productosParaVerificarStockDTO) {
    if (productosParaVerificarStockDTO.getIdSucursal() <= 0) {
    throw new BusinessServiceException(
            messageSource.getMessage("mensaje_producto_consulta_stock_sin_sucursal", null, Locale.getDefault()));}
    List<ProductoFaltanteDTO> productosFaltantes = new ArrayList<>();
    int longitudIds = productosParaVerificarStockDTO.getIdProducto().length;
    int longitudCantidades = productosParaVerificarStockDTO.getCantidad().length;
    HashMap<Long, BigDecimal> listaIdsAndCantidades = new HashMap<>();
    this.validarLongitudDeArrays(longitudIds, longitudCantidades);
    if (productosParaVerificarStockDTO.getIdPedido() != null) {
      List<RenglonPedido> renglonesDelPedido =
              pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(productosParaVerificarStockDTO.getIdPedido());
      if (!renglonesDelPedido.isEmpty()){
      renglonesDelPedido.forEach(renglonPedido -> listaIdsAndCantidades.put(renglonPedido.getIdProductoItem(), renglonPedido.getCantidad()));
      }
    }
    for (int i = 0; i < longitudIds; i++) {
      Producto producto =
          this.getProductoNoEliminadoPorId(productosParaVerificarStockDTO.getIdProducto()[i]);
      this.calcularCantidadEnSucursalesDisponible(producto, productosParaVerificarStockDTO.getIdSucursal());
      BigDecimal cantidadParaCalcular = productosParaVerificarStockDTO.getCantidad()[i];
      if (!listaIdsAndCantidades.isEmpty() && listaIdsAndCantidades.get(producto.getIdProducto()) != null) {
          cantidadParaCalcular = cantidadParaCalcular.subtract(listaIdsAndCantidades.get(producto.getIdProducto()));
      }
      BigDecimal cantidadSolicitada = cantidadParaCalcular;
      producto.getCantidadProducto().getCantidadEnSucursalesDisponible().stream()
            .filter(cantidadEnSucursal -> (cantidadEnSucursal.getSucursal().getConfiguracionSucursal().isComparteStock()
                    || cantidadEnSucursal.getSucursal().getIdSucursal() == productosParaVerificarStockDTO.getIdSucursal()))
            .forEach(
                cantidadEnSucursal -> {
                  if (!producto.getCantidadProducto().isIlimitado()
                      && producto.getCantidadProducto().getCantidadEnSucursalesDisponible()
                          .stream()
                          .map(CantidadEnSucursal::getCantidad)
                          .reduce(BigDecimal.ZERO,BigDecimal::add)
                          .compareTo(cantidadSolicitada) < 0
                  && cantidadSolicitada.compareTo(BigDecimal.ZERO) > 0) {
                    productosFaltantes
                            .add(this.construirNuevoProductoFaltante(producto, cantidadSolicitada, cantidadEnSucursal.getCantidad(), cantidadEnSucursal.getIdSucursal()));
                  }
            });
    }
    return productosFaltantes;
  }

  @Override
  public void validarLongitudDeArrays(int longitudIds, int longitudCantidades) {
    if (longitudIds != longitudCantidades)
       throw new BusinessServiceException(
            messageSource.getMessage("mensaje_error_logitudes_arrays", null, Locale.getDefault()));
  }

  @Override
  public List<ProductoFaltanteDTO> getProductosSinStockDisponibleParaTraspaso(
          ProductosParaVerificarStockDTO productosParaVerificarStockDTO) {
    List<ProductoFaltanteDTO> productosFaltantes = new ArrayList<>();
    int longitudIds = productosParaVerificarStockDTO.getIdProducto().length;
    int longitudCantidades = productosParaVerificarStockDTO.getCantidad().length;
    this.validarLongitudDeArrays(longitudIds, longitudCantidades);
    for (int i = 0; i < longitudIds; i++) {
      Producto producto =
              this.getProductoNoEliminadoPorId(productosParaVerificarStockDTO.getIdProducto()[i]);
      BigDecimal cantidadSolicitada = productosParaVerificarStockDTO.getCantidad()[i];
      this.calcularCantidadEnSucursalesDisponible(producto, productosParaVerificarStockDTO.getIdSucursal());
      producto.getCantidadProducto().getCantidadEnSucursalesDisponible().stream()
              .filter(
                      cantidadEnSucursal ->
                              cantidadEnSucursal
                                      .getIdSucursal()
                                      .equals(productosParaVerificarStockDTO.getIdSucursal()))
              .forEach(
                      cantidadEnSucursal -> {
                        if (!producto.getCantidadProducto().isIlimitado()
                                && cantidadEnSucursal.getCantidad().compareTo(cantidadSolicitada) < 0
                                && cantidadSolicitada.compareTo(BigDecimal.ZERO) > 0) {
                          productosFaltantes
                                  .add(this.construirNuevoProductoFaltante(producto, cantidadSolicitada, cantidadEnSucursal.getCantidad(), cantidadEnSucursal.getIdSucursal()));
                        }
                      });
    }
    return productosFaltantes;
  }

  @Override
  public ProductoFaltanteDTO construirNuevoProductoFaltante(Producto producto, BigDecimal cantidadSolicitada, BigDecimal cantidadDisponible, long idSucursal) {
    ProductoFaltanteDTO productoFaltanteDTO = new ProductoFaltanteDTO();
    productoFaltanteDTO.setIdProducto(producto.getIdProducto());
    productoFaltanteDTO.setCodigo(producto.getCodigo());
    productoFaltanteDTO.setDescripcion(producto.getDescripcion());
    Sucursal sucursal = sucursalService.getSucursalPorId(idSucursal);
    productoFaltanteDTO.setNombreSucursal(sucursal.getNombre());
    productoFaltanteDTO.setIdSucursal(sucursal.getIdSucursal());
    productoFaltanteDTO.setCantidadSolicitada(cantidadSolicitada);
    productoFaltanteDTO.setCantidadDisponible(cantidadDisponible);
    return productoFaltanteDTO;
  }

  @Override
  public BigDecimal calcularGananciaPorcentaje(
      BigDecimal precioDeListaNuevo,
      BigDecimal precioDeListaAnterior,
      BigDecimal pvp,
      BigDecimal ivaPorcentaje,
      BigDecimal impInternoPorcentaje,
      BigDecimal precioCosto,
      boolean ascendente) {
    // evita la division por cero
    if (precioCosto.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    BigDecimal resultado;
    if (!ascendente) {
      resultado =
          pvp.subtract(precioCosto).divide(precioCosto, 15, RoundingMode.HALF_UP).multiply(CIEN);
    } else if (precioDeListaAnterior.compareTo(BigDecimal.ZERO) == 0
        || precioCosto.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    } else {
      resultado = precioDeListaNuevo;
      BigDecimal porcentajeIncremento =
          precioDeListaNuevo.divide(precioDeListaAnterior, 15, RoundingMode.HALF_UP);
      resultado =
          resultado.subtract(
              porcentajeIncremento.multiply(
                  impInternoPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP).multiply(pvp)));
      resultado =
          resultado.subtract(
              porcentajeIncremento.multiply(
                  ivaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP).multiply(pvp)));
      resultado =
          resultado
              .subtract(precioCosto)
              .multiply(CIEN)
              .divide(precioCosto, 15, RoundingMode.HALF_UP);
    }
    return resultado;
  }

  @Override
  public BigDecimal calcularGananciaNeto(BigDecimal precioCosto, BigDecimal gananciaPorcentaje) {
    return precioCosto.multiply(gananciaPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calcularPVP(BigDecimal precioCosto, BigDecimal gananciaPorcentaje) {
    return precioCosto.add(
        precioCosto.multiply(gananciaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP)));
  }

  @Override
  public BigDecimal calcularIVANeto(BigDecimal pvp, BigDecimal ivaPorcentaje) {
    return pvp.multiply(ivaPorcentaje).divide(CIEN, 15, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal calcularPrecioLista(BigDecimal pvp, BigDecimal ivaPorcentaje) {
    BigDecimal resulIVA = pvp.multiply(ivaPorcentaje.divide(CIEN, 15, RoundingMode.HALF_UP));
    return pvp.add(resulIVA);
  }

  @Override
  @Async
  public void getListaDePreciosEnXls(BusquedaProductoCriteria criteria, long idSucursal) {
    List<Producto> productos = this.buscarProductosParaReporte(criteria);
    this.enviarListaDeProductosPorEmail(sucursalService.getSucursalPorId(idSucursal).getEmail(),
            this.getListaDePrecios(productos, FORMATO_XLSX), FORMATO_XLSX);
  }

  @Override
  @Async
  public void getListaDePreciosEnPdf(BusquedaProductoCriteria criteria, long idSucursal) {
    List<Producto> productos = this.buscarProductosParaReporte(criteria);
    this.enviarListaDeProductosPorEmail(sucursalService.getSucursalPorId(idSucursal).getEmail(),
            this.getListaDePrecios(productos, FORMATO_PDF), FORMATO_PDF);
  }

  @Override
  public void enviarListaDeProductosPorEmail(String mailTo, byte[] listaDeProductos, String formato) {
    correoElectronicoService.enviarEmail(
            mailTo,
            "",
            "Listado de productos",
            "",
            listaDeProductos,
            "ListaDeProductos." + formato);
  }

  public byte[] getListaDePrecios(List<Producto> productos, String formato) {
    Map<String, Object> params = new HashMap<>();
    Sucursal sucursalPredeterminada =  sucursalService.getSucursalPredeterminada();
    if (sucursalPredeterminada.getLogo() != null && !sucursalPredeterminada.getLogo().isEmpty()) {
      try {
        params.put(
                "logo", new ImageIcon(ImageIO.read(new URL(sucursalPredeterminada.getLogo()))).getImage());
        params.put(
                "productoSinImagen", new ImageIcon(ImageIO.read(new URL(URL_PRODUCTO_SIN_IMAGEN))).getImage());
      } catch (IOException | NullPointerException ex) {
        throw new ServiceException(messageSource.getMessage(
                "mensaje_recurso_no_encontrado", null, Locale.getDefault()), ex);
      }
    }
    JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(productos);
    JasperReport jasperDesign;
    try {
      jasperDesign = JasperCompileManager.compileReport("src/main/resources/sic/vista/reportes/ListaPreciosProductos.jrxml");
    } catch (JRException ex) {
      throw new ServiceException(messageSource.getMessage(
              "mensaje_error_reporte", null, Locale.getDefault()), ex);
    }
    switch (formato) {
      case "xlsx":
        try {
          return xlsReportToArray(JasperFillManager.fillReport(jasperDesign, params, ds));
        } catch (JRException ex) {
          throw new ServiceException(messageSource.getMessage(
                  "mensaje_error_reporte", null, Locale.getDefault()), ex);
        }
      case "pdf":
        try {
          return JasperExportManager.exportReportToPdf(
                  JasperFillManager.fillReport(jasperDesign, params, ds));
        } catch (JRException ex) {
          throw new ServiceException(messageSource.getMessage(
                  "mensaje_error_reporte", null, Locale.getDefault()), ex);
        }
      default:
        throw new BusinessServiceException(messageSource.getMessage(
                "mensaje_formato_no_valido", null, Locale.getDefault()));
    }
  }

  private byte[] xlsReportToArray(JasperPrint jasperPrint) {
    byte[] bytes = null;
    try {
      JRXlsxExporter jasperXlsxExportMgr = new JRXlsxExporter();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      SimpleOutputStreamExporterOutput simpleOutputStreamExporterOutput =
          new SimpleOutputStreamExporterOutput(out);
      jasperXlsxExportMgr.setExporterInput(new SimpleExporterInput(jasperPrint));
      jasperXlsxExportMgr.setExporterOutput(simpleOutputStreamExporterOutput);
      jasperXlsxExportMgr.exportReport();
      bytes = out.toByteArray();
      out.close();
    } catch (JRException ex) {
      throw new ServiceException(messageSource.getMessage(
        "mensaje_error_reporte", null, Locale.getDefault()), ex);
    } catch (IOException ex) {
      logger.error(ex.getMessage());
    }
    return bytes;
  }

  private boolean contieneDuplicados(long[] array) {
      Set<Long> set = new HashSet<>();
      for (long i : array) {
        if (set.contains(i)) return true;
        set.add(i);
      }
      return false;
  }

  @Override
  public Producto calcularCantidadEnSucursalesDisponible(Producto producto, long idSucursalSeleccionada) {
    Set<CantidadEnSucursal> cantidadesEnSucursales = new HashSet<>();
    producto.getCantidadProducto().getCantidadEnSucursales()
            .stream()
            .filter(cantidadEnSucursal -> (cantidadEnSucursal.getSucursal().getConfiguracionSucursal().isComparteStock()
                    || cantidadEnSucursal.getSucursal().getIdSucursal() == idSucursalSeleccionada))
            .forEach(cantidadesEnSucursales::add);
    producto.getCantidadProducto().setCantidadEnSucursalesDisponible(cantidadesEnSucursales);
    producto.getCantidadProducto()
            .setCantidadTotalEnSucursalesDisponible(cantidadesEnSucursales.stream()
                    .map(CantidadEnSucursal::getCantidad)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
    return producto;
  }

  @Override
  public Producto guardarProductoFavorito(long idUsuario, long idProducto) {
    Producto producto = this.getProductoNoEliminadoPorId(idProducto);
    if (this.isFavorito(idUsuario, idProducto)) {
        producto.setFavorito(true);
    } else {
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    ProductoFavorito productoFavorito = new ProductoFavorito();
    productoFavorito.setCliente(cliente);
    productoFavorito.setProducto(producto);
    customValidator.validar(productoFavorito);
    producto = productoFavoritoRepository.save(productoFavorito).getProducto();
    producto.setFavorito(true);
    logger.warn(messageSource.getMessage(
            "mensaje_producto_favorito_agregado",
            new Object[] {producto},
            Locale.getDefault()));
    }
    return producto;
  }

  @Override
  public Page<Producto> getPaginaProductosFavoritosDelCliente(long idUsuario, long idSucursal, int pagina) {
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    QProductoFavorito qProductoFavorito = QProductoFavorito.productoFavorito;
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qProductoFavorito.cliente.idCliente.eq(cliente.getIdCliente()));
    Page<ProductoFavorito> pageable =
            productoFavoritoRepository.findAll(
                    builder,
                    PageRequest.of(
                            pagina,
                            TAMANIO_PAGINA_DEFAULT,
                            Sort.by(Sort.Direction.DESC, "idProductoFavorito")));
    List<Producto> productos = new ArrayList<>();
    pageable.stream()
            .forEach(
                    productoFavorito -> {
                      Producto producto = productoFavorito.getProducto();
                      producto.setFavorito(true);
                      this.calcularCantidadEnSucursalesDisponible(producto, idSucursal);
                      productos.add(producto);
                    });
    return new PageImpl<>(productos, pageable.getPageable(), pageable.getTotalElements());
  }

  @Override
  public List<Producto> getProductosFavoritosDelClientePorIdUsuario(long idUsuario) {
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    List<ProductoFavorito> productoFavoritos = productoFavoritoRepository.findAllByCliente(cliente);
    List<Producto> productos = new ArrayList<>();
    productoFavoritos.forEach(productoFavorito -> {
      productoFavorito.getProducto().setFavorito(true);
      productos.add(productoFavorito.getProducto());
    });
    return productos;
  }

  @Override
  @Transactional
  public void quitarProductoDeFavoritos(long idUsuario, long idProducto) {
    Producto producto = this.getProductoNoEliminadoPorId(idProducto);
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    productoFavoritoRepository.deleteByClienteAndProducto(cliente, producto);
    logger.warn(messageSource.getMessage(
            "mensaje_producto_favorito_quitado",
            new Object[] {producto},
            Locale.getDefault()));
  }

  @Override
  @Transactional
  public void quitarProductosDeFavoritos(long idUsuario) {
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    productoFavoritoRepository.deleteAllByCliente(cliente);
    logger.warn(
            messageSource.getMessage("mensaje_producto_favoritos_quitados", null, Locale.getDefault()));
  }

  @Override
  public Long getCantidadDeProductosFavoritos(long idUsuario) {
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    return productoFavoritoRepository.getCantidadDeArticulosEnFavoritos(cliente);
  }

  @Override
  public PrecioProductoEmbeddable construirPrecioProductoEmbeddable(ProductoDTO productoDTO) {
    return PrecioProductoEmbeddable.builder()
            .precioCosto(productoDTO.getPrecioCosto())
            .gananciaPorcentaje(productoDTO.getGananciaPorcentaje())
            .gananciaNeto(productoDTO.getGananciaNeto())
            .precioVentaPublico(productoDTO.getPrecioVentaPublico())
            .ivaPorcentaje(productoDTO.getIvaPorcentaje())
            .ivaNeto(productoDTO.getIvaNeto())
            .precioLista(productoDTO.getPrecioLista())
            .oferta(productoDTO.isOferta())
            .porcentajeBonificacionOferta(productoDTO.getPorcentajeBonificacionOferta())
            .porcentajeBonificacionPrecio(productoDTO.getPorcentajeBonificacionPrecio())
            .precioBonificado(productoDTO.getPrecioBonificado())
            .build();
  }

  @Override
  public CantidadProductoEmbeddable construirCantidadProductoEmbeddable(ProductoDTO productoDTO){
    Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
    productoDTO.getCantidadEnSucursales().forEach(cantidadEnSucursalDTO -> {
      CantidadEnSucursal cantidadEnSucursal = new CantidadEnSucursal();
      cantidadEnSucursal.setCantidad(cantidadEnSucursalDTO.getCantidad());
      cantidadEnSucursal.setSucursal(sucursalService.getSucursalPorId(cantidadEnSucursalDTO.getIdSucursal()));
      cantidadEnSucursal.setIdCantidadEnSucursal(cantidadEnSucursalDTO.getIdCantidadEnSucursal());
      cantidadEnSucursales.add(cantidadEnSucursal);
    });
    return CantidadProductoEmbeddable.builder()
            .cantidadEnSucursales(cantidadEnSucursales)
            .cantidadTotalEnSucursales(productoDTO.getCantidadTotalEnSucursales())
            .cantMinima(productoDTO.getCantMinima())
            .bulto(productoDTO.getBulto())
            .ilimitado(productoDTO.isIlimitado())
            .build();
  }

  @Override
  public Page<Producto> getProductosRelacionados(long idProducto, int pagina) {
    Pageable pageable = PageRequest.of(0, TAMANIO_PAGINA_DEFAULT);
    return productoFavoritoRepository.buscarProductosRelacionadosPorRubro(this.getProductoNoEliminadoPorId(idProducto).getRubro().getIdRubro(), pageable);
  }

  @Override
  public void agregarCantidadReservada(long idProducto, BigDecimal cantidadParaAgregar) {
      productoRepository.actualizarCantidadReservada(idProducto, cantidadParaAgregar);
  }

  @Override
  public void quitarCantidadReservada(long idProducto, BigDecimal cantidadParaQuitar) {
      productoRepository.actualizarCantidadReservada(idProducto, cantidadParaQuitar.negate());
  }
}
