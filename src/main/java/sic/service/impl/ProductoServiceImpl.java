package sic.service.impl;

import com.querydsl.core.BooleanBuilder;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sic.modelo.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import javax.swing.ImageIcon;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
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
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoFaltanteDTO;
import sic.modelo.dto.ProductosParaActualizarDTO;
import sic.modelo.dto.ProductosParaVerificarStockDTO;
import sic.repository.ProductoFavoritoRepository;
import sic.repository.ProductoRepository;
import sic.service.*;
import sic.util.CalculosComprobante;
import sic.util.CustomValidator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
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
    this.messageSource = messageSource;
    this.customValidator = customValidator;
  }

  @Override
  public void validarReglasDeNegocio(TipoDeOperacion operacion, Producto producto) {
    if (producto.isOferta()
        && producto.getPorcentajeBonificacionOferta().compareTo(BigDecimal.ZERO) <= 0) {
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
    if (!Arrays.asList(iva).contains(producto.getIvaPorcentaje().doubleValue())) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_error_iva_no_valido",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getGananciaNeto()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularGananciaNeto(
                        producto.getPrecioCosto(), producto.getGananciaPorcentaje())
                    .setScale(3, RoundingMode.HALF_UP))
        != 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_ganancia_neta_incorrecta",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getPrecioVentaPublico()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularPVP(producto.getPrecioCosto(), producto.getGananciaPorcentaje())
                    .setScale(3, RoundingMode.HALF_UP))
        != 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_precio_venta_publico_incorrecto",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getIvaNeto()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularIVANeto(producto.getPrecioVentaPublico(), producto.getIvaPorcentaje())
                    .setScale(3, RoundingMode.HALF_UP))
        != 0) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_producto_iva_neto_incorrecto",
              new Object[] {producto.getDescripcion()},
              Locale.getDefault()));
    }
    if (producto
            .getPrecioLista()
            .setScale(3, RoundingMode.HALF_UP)
            .compareTo(
                this.calcularPrecioLista(
                        producto.getPrecioVentaPublico(), producto.getIvaPorcentaje())
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
            producto -> {
              this.calcularCantidadEnSucursalesDisponible(producto, idSucursal);
              this.calcularCantidadReservada(producto);
            });
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
          .and(qProducto.cantidadEnSucursales.any().cantidad.loe(qProducto.cantMinima))
          .and(qProducto.ilimitado.eq(false));
    if (criteria.isListarSoloEnStock())
      builder
          .and(qProducto.cantidadEnSucursales.any().cantidad.gt(BigDecimal.ZERO))
          .and(qProducto.ilimitado.eq(false));
    if (criteria.getPublico() != null) {
      if (criteria.getPublico()) builder.and(qProducto.publico.isTrue());
      else builder.and(qProducto.publico.isFalse());
    }
    if (criteria.getOferta() != null && criteria.getOferta())
      builder.and(qProducto.oferta.isTrue());
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
    producto.setCantidadEnSucursales(altaCantidadesEnSucursales);
    producto
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
    producto.setCantidadTotalEnSucursales(
        producto.getCantidadEnSucursales().stream()
            .map(CantidadEnSucursal::getCantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    producto.setHayStock(producto.getCantidadTotalEnSucursales().compareTo(BigDecimal.ZERO) > 0);
    producto.setCantMinima(nuevoProductoDTO.getCantMinima());
    producto.setBulto(nuevoProductoDTO.getBulto());
    producto.setPrecioCosto(nuevoProductoDTO.getPrecioCosto());
    producto.setGananciaPorcentaje(nuevoProductoDTO.getGananciaPorcentaje());
    producto.setGananciaNeto(nuevoProductoDTO.getGananciaNeto());
    producto.setPrecioVentaPublico(nuevoProductoDTO.getPrecioVentaPublico());
    producto.setIvaPorcentaje(nuevoProductoDTO.getIvaPorcentaje());
    producto.setIvaNeto(nuevoProductoDTO.getIvaNeto());
    producto.setPrecioLista(nuevoProductoDTO.getPrecioLista());
    producto.setOferta(nuevoProductoDTO.isOferta());
    producto.setPorcentajeBonificacionOferta(
        nuevoProductoDTO.getPorcentajeBonificacionOferta() != null
            ? nuevoProductoDTO.getPorcentajeBonificacionOferta()
            : BigDecimal.ZERO);
    producto.setPorcentajeBonificacionPrecio(
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
    producto.setIlimitado(false);
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
    productoPorActualizar.setIlimitado(false);
    productoPorActualizar.setVersion(productoPersistido.getVersion());
    productoPorActualizar = productoRepository.save(productoPorActualizar);
    logger.warn(
        messageSource.getMessage(
            "mensaje_producto_actualizado",
            new Object[] {productoPorActualizar},
            Locale.getDefault()));
    if (imagen != null) this.subirImagenProducto(productoPorActualizar.getIdProducto(), imagen);
  }

  private void calcularPrecioBonificado(Producto producto) {
    producto.setPrecioBonificado(producto.getPrecioLista());
    if (producto.isOferta()
        && producto.getPorcentajeBonificacionOferta() != null
        && producto.getPorcentajeBonificacionOferta().compareTo(BigDecimal.ZERO) > 0) {
      producto.setPrecioBonificado(
          producto
              .getPrecioLista()
              .subtract(
                  CalculosComprobante.calcularProporcion(
                      producto.getPrecioLista(), producto.getPorcentajeBonificacionOferta())));
    } else if (producto.getPorcentajeBonificacionPrecio() != null
        && producto.getPorcentajeBonificacionPrecio().compareTo(BigDecimal.ZERO) > 0) {
      producto.setOferta(false);
      producto.setPrecioBonificado(
          producto
              .getPrecioLista()
              .subtract(
                  CalculosComprobante.calcularProporcion(
                      producto.getPrecioLista(), producto.getPorcentajeBonificacionPrecio())));
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
            if (productoAnterior.isPresent() && !productoAnterior.get().isIlimitado()) {
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
              if (producto.isPresent() && !producto.get().isIlimitado()) {
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
          if (producto.isPresent() && !producto.get().isIlimitado()) {
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
          if (producto.isPresent() && !producto.get().isIlimitado()) {
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
                        Producto producto =
                                this.getProductoNoEliminadoPorId(renglonTraspaso.getIdProducto());
                        this.quitarStock(
                                producto,
                                traspaso.getSucursalDestino().getIdSucursal(),
                                renglonTraspaso.getCantidadProducto());
                        this.agregarStock(
                                producto,
                                traspaso.getSucursalOrigen().getIdSucursal(),
                                renglonTraspaso.getCantidadProducto());
                      });
      default -> throw new BusinessServiceException(
              messageSource.getMessage(
                      "mensaje_operacion_no_soportada", null, Locale.getDefault()));
    }
  }

  private Producto agregarStock(Producto producto, long idSucursal, BigDecimal cantidad) {
    producto
        .getCantidadEnSucursales()
        .forEach(
            cantidadEnSucursal -> {
              if (cantidadEnSucursal.getSucursal().getIdSucursal() == idSucursal) {
                cantidadEnSucursal.setCantidad(cantidadEnSucursal.getCantidad().add(cantidad));
              }
            });
    producto.setCantidadTotalEnSucursales(
        producto.getCantidadEnSucursales().stream()
            .map(CantidadEnSucursal::getCantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    producto.setHayStock(producto.getCantidadTotalEnSucursales().compareTo(BigDecimal.ZERO) > 0);
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
        .getCantidadEnSucursales()
        .forEach(
            cantidadEnSucursal -> {
              if (cantidadEnSucursal.getSucursal().getIdSucursal() == idSucursal) {
                cantidadEnSucursal.setCantidad(cantidadEnSucursal.getCantidad().subtract(cantidad));
              }
            });
    producto.setCantidadTotalEnSucursales(
        producto.getCantidadEnSucursales().stream()
            .map(CantidadEnSucursal::getCantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    producto.setHayStock(producto.getCantidadTotalEnSucursales().compareTo(BigDecimal.ZERO) > 0);
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
          p.setBulto(productosParaActualizarDTO.getCantidadVentaMinima());
      }
      if (actualizaPrecios) {
        p.setPrecioCosto(productosParaActualizarDTO.getPrecioCosto());
        p.setGananciaPorcentaje(productosParaActualizarDTO.getGananciaPorcentaje());
        p.setGananciaNeto(this.calcularGananciaNeto(p.getPrecioCosto(), p.getGananciaPorcentaje()));
        p.setPrecioVentaPublico(this.calcularPVP(p.getPrecioCosto(), p.getGananciaPorcentaje()));
        p.setIvaPorcentaje(productosParaActualizarDTO.getIvaPorcentaje());
        p.setIvaNeto(this.calcularIVANeto(p.getPrecioVentaPublico(), p.getIvaPorcentaje()));
        p.setPrecioLista(
                this.calcularPrecioLista(p.getPrecioVentaPublico(), p.getIvaPorcentaje()));
        p.setFechaUltimaModificacion(LocalDateTime.now());
        if (productosParaActualizarDTO.getPorcentajeBonificacionOferta() != null
                && productosParaActualizarDTO.getPorcentajeBonificacionOferta().compareTo(BigDecimal.ZERO)
                >= 0) {
          p.setOferta(true);
          p.setPorcentajeBonificacionOferta(
                  productosParaActualizarDTO.getPorcentajeBonificacionOferta());
        } else {
          p.setOferta(false);
        }
      }
      if (aplicaDescuentoRecargoPorcentaje) {
        p.setPrecioCosto(p.getPrecioCosto().multiply(multiplicador));
        p.setGananciaNeto(this.calcularGananciaNeto(p.getPrecioCosto(), p.getGananciaPorcentaje()));
        p.setPrecioVentaPublico(this.calcularPVP(p.getPrecioCosto(), p.getGananciaPorcentaje()));
        p.setIvaNeto(this.calcularIVANeto(p.getPrecioVentaPublico(), p.getIvaPorcentaje()));
        p.setPrecioLista(
            this.calcularPrecioLista(p.getPrecioVentaPublico(), p.getIvaPorcentaje()));
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
        p.setPorcentajeBonificacionPrecio(
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
    productos.forEach(producto -> producto.getCantidadEnSucursales().add(cantidadNueva));
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
    List<ProductoFaltanteDTO> productosFaltantes = new ArrayList<>();
    int longitudIds = productosParaVerificarStockDTO.getIdProducto().length;
    int longitudCantidades = productosParaVerificarStockDTO.getCantidad().length;
    HashMap<Long, BigDecimal> listaIdsAndCantidades = new HashMap<>();
    if (longitudIds == longitudCantidades) {
      if (productosParaVerificarStockDTO.getIdPedido() != null) {
        List<RenglonPedido> renglonesDelPedido = pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(productosParaVerificarStockDTO.getIdPedido());
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
        producto.getCantidadEnSucursalesDisponible().stream()
              .filter(cantidadEnSucursal -> (cantidadEnSucursal.getSucursal().getConfiguracionSucursal().isComparteStock()
                      || cantidadEnSucursal.getSucursal().getIdSucursal() == productosParaVerificarStockDTO.getIdSucursal()))
              .forEach(
                  cantidadEnSucursal -> {
                    if (!producto.isIlimitado()
                        && producto.getCantidadEnSucursalesDisponible()
                            .stream()
                            .map(CantidadEnSucursal::getCantidad)
                            .reduce(BigDecimal.ZERO,BigDecimal::add)
                            .compareTo(cantidadSolicitada) < 0
                    && cantidadSolicitada.compareTo(BigDecimal.ZERO) > 0) {
                      ProductoFaltanteDTO productoFaltanteDTO = new ProductoFaltanteDTO();
                      productoFaltanteDTO.setIdProducto(producto.getIdProducto());
                      productoFaltanteDTO.setCodigo(producto.getCodigo());
                      productoFaltanteDTO.setDescripcion(producto.getDescripcion());
                      productoFaltanteDTO.setCantidadSolicitada(cantidadSolicitada);
                      productoFaltanteDTO.setCantidadDisponible(cantidadEnSucursal.getCantidad());
                      productosFaltantes.add(productoFaltanteDTO);
                    }
              });
      }
    } else {
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_error_logitudes_arrays", null, Locale.getDefault()));
    }
    return productosFaltantes;
  }

  @Override
  public List<ProductoFaltanteDTO> getProductosSinStockDisponibleParaTraspaso(
          ProductosParaVerificarStockDTO productosParaVerificarStockDTO) {
    List<ProductoFaltanteDTO> productosFaltantes = new ArrayList<>();
    int longitudIds = productosParaVerificarStockDTO.getIdProducto().length;
    int longitudCantidades = productosParaVerificarStockDTO.getCantidad().length;
    if (longitudIds == longitudCantidades) {
      for (int i = 0; i < longitudIds; i++) {
        Producto producto =
                this.getProductoNoEliminadoPorId(productosParaVerificarStockDTO.getIdProducto()[i]);
        BigDecimal cantidadSolicitada = productosParaVerificarStockDTO.getCantidad()[i];
        producto.getCantidadEnSucursales().stream()
                .filter(
                        cantidadEnSucursal ->
                                cantidadEnSucursal
                                        .getIdSucursal()
                                        .equals(productosParaVerificarStockDTO.getIdSucursal()))
                .forEach(
                        cantidadEnSucursal -> {
                          if (!producto.isIlimitado()
                                  && cantidadEnSucursal.getCantidad().compareTo(cantidadSolicitada) < 0
                                  && cantidadSolicitada.compareTo(BigDecimal.ZERO) > 0) {
                            ProductoFaltanteDTO productoFaltanteDTO = new ProductoFaltanteDTO();
                            productoFaltanteDTO.setIdProducto(producto.getIdProducto());
                            productoFaltanteDTO.setCodigo(producto.getCodigo());
                            productoFaltanteDTO.setDescripcion(producto.getDescripcion());
                            productoFaltanteDTO.setCantidadSolicitada(cantidadSolicitada);
                            productoFaltanteDTO.setCantidadDisponible(cantidadEnSucursal.getCantidad());
                            productosFaltantes.add(productoFaltanteDTO);
                          }
                        });
      }
    } else {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_error_logitudes_arrays", null, Locale.getDefault()));
    }
    return productosFaltantes;
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
  public byte[] getListaDePreciosEnXls(BusquedaProductoCriteria criteria) {
    List<Producto> productos = this.buscarProductosParaReporte(criteria);
    return this.getListaDePrecios(productos, "xlsx");
  }

  @Override
  public byte[] getListaDePreciosEnPdf(BusquedaProductoCriteria criteria) {
    List<Producto> productos = this.buscarProductosParaReporte(criteria);
    return this.getListaDePrecios(productos, "pdf");
  }

  private byte[] getListaDePrecios(List<Producto> productos, String formato) {
    ClassLoader classLoader = FacturaServiceImpl.class.getClassLoader();
    InputStream isFileReport =
            classLoader.getResourceAsStream("sic/vista/reportes/ListaPreciosProductos.jasper");
    Map<String, Object> params = new HashMap<>();
    Sucursal sucursalPredeterminada =  sucursalService.getSucursalPredeterminada();
    if (sucursalPredeterminada.getLogo() != null && !sucursalPredeterminada.getLogo().isEmpty()) {
      try {
        params.put(
                "logo", new ImageIcon(ImageIO.read(new URL(sucursalPredeterminada.getLogo()))).getImage());
      } catch (IOException ex) {
        throw new ServiceException(messageSource.getMessage(
                "mensaje_sucursal_404_logo", null, Locale.getDefault()), ex);
      }
    }
    JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(productos);
    switch (formato) {
      case "xlsx":
        try {
          return xlsReportToArray(JasperFillManager.fillReport(isFileReport, params, ds));
        } catch (JRException ex) {
          throw new ServiceException(messageSource.getMessage(
                  "mensaje_error_reporte", null, Locale.getDefault()), ex);
        }
      case "pdf":
        try {
          return JasperExportManager.exportReportToPdf(
                  JasperFillManager.fillReport(isFileReport, params, ds));
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
    producto.getCantidadEnSucursales()
            .stream()
            .filter(cantidadEnSucursal -> (cantidadEnSucursal.getSucursal().getConfiguracionSucursal().isComparteStock()
                    || cantidadEnSucursal.getSucursal().getIdSucursal() == idSucursalSeleccionada))
            .forEach(cantidadesEnSucursales::add);
    producto.setCantidadEnSucursalesDisponible(cantidadesEnSucursales);
    return producto;
  }

  @Override
  public Producto calcularCantidadReservada(Producto producto) {
    producto.setCantidadReservada(pedidoService.getCantidadReservadaDeProducto(producto.getIdProducto()));
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
  public Page<Producto> getPaginaProductosFavoritosDelCliente(long idUsuario, int pagina) {
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
}
