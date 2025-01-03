package org.opencommercial.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.exception.ServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaProductoCriteria;
import org.opencommercial.model.dto.*;
import org.opencommercial.model.embeddable.CantidadProductoEmbeddable;
import org.opencommercial.model.embeddable.PrecioProductoEmbeddable;
import org.opencommercial.repository.ProductoFavoritoRepository;
import org.opencommercial.repository.ProductoRepository;
import org.opencommercial.util.CalculosComprobante;
import org.opencommercial.util.CustomValidator;
import org.opencommercial.util.FormatoReporte;
import org.opencommercial.util.JasperReportsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ProductoServiceImpl implements ProductoService {

  @Value("${EMAIL_DEFAULT_PROVIDER}")
  private String emailDefaultProvider;

  private final ProductoRepository productoRepository;
  private final ProductoFavoritoRepository productoFavoritoRepository;
  private static final BigDecimal CIEN = new BigDecimal("100");
  private static final long TAMANIO_MAXIMO_IMAGEN = 1024000L;
  private final RubroService rubroService;
  private final ProveedorService proveedorService;
  private final MedidaService medidaService;
  private final CarritoCompraService carritoCompraService;
  private final ImageUploaderService imageUploaderService;
  private final SucursalService sucursalService;
  private final TraspasoService traspasoService;
  private final PedidoService pedidoService;
  private final ClienteService clienteService;
  private final UsuarioService usuarioService;
  private final EmailServiceFactory emailServiceFactory;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private static final String MENSAJE_ERROR_ACTUALIZAR_STOCK_PRODUCTO_ELIMINADO = "mensaje_error_actualizar_stock_producto_eliminado";
  private static final String PRODUCTO_SIN_IMAGEN = "/producto_sin_imagen.png";
  private final MessageSource messageSource;
  private final CustomValidator customValidator;
  private final JasperReportsHandler jasperReportsHandler;

  @Autowired
  @Lazy
  public ProductoServiceImpl(
          ProductoRepository productoRepository,
          ProductoFavoritoRepository productoFavoritoRepository,
          RubroService rubroService,
          ProveedorService proveedorService,
          MedidaService medidaService,
          CarritoCompraService carritoCompraService,
          ImageUploaderService imageUploaderService,
          SucursalService sucursalService,
          TraspasoService traspasoService,
          PedidoService pedidoService,
          ClienteService clienteService,
          UsuarioService usuarioService,
          EmailServiceFactory emailServiceFactory,
          MessageSource messageSource,
          CustomValidator customValidator,
          JasperReportsHandler jasperReportsHandler) {
    this.productoRepository = productoRepository;
    this.productoFavoritoRepository = productoFavoritoRepository;
    this.rubroService = rubroService;
    this.proveedorService = proveedorService;
    this.medidaService = medidaService;
    this.carritoCompraService = carritoCompraService;
    this.imageUploaderService = imageUploaderService;
    this.sucursalService = sucursalService;
    this.traspasoService = traspasoService;
    this.pedidoService = pedidoService;
    this.clienteService = clienteService;
    this.usuarioService = usuarioService;
    this.emailServiceFactory = emailServiceFactory;
    this.messageSource = messageSource;
    this.customValidator = customValidator;
    this.jasperReportsHandler = jasperReportsHandler;
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
    if (!producto.getCodigo().isEmpty()) {
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
          && !producto.getCodigo().isEmpty()) {
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
  public Page<Producto> buscarProductosDeCatalogoParaUsuario(BusquedaProductoCriteria criteria, Long idSucursal, Long idUsuario) {
    Usuario usuarioDeConsulta = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    if (usuarioDeConsulta.getRoles().contains(Rol.COMPRADOR)) {
        Cliente clienteDeUsuario = clienteService.getClientePorIdUsuario(idUsuario);
        if (clienteDeUsuario == null) {
          throw new BusinessServiceException(
                  messageSource.getMessage("mensaje_cliente_no_existente", null, Locale.getDefault()));
        } else {
          if (clienteDeUsuario.isPuedeComprarAPlazo()
                  && !usuarioDeConsulta.getRoles().contains(Rol.ADMINISTRADOR)
                  && !usuarioDeConsulta.getRoles().contains(Rol.ENCARGADO)
                  && !usuarioDeConsulta.getRoles().contains(Rol.VENDEDOR)) {
            criteria.setListarSoloParaCatalogo(true);
          }
          return this.buscarProductos(criteria, idSucursal);
        }
    } else {
      return this.buscarProductos(criteria, idSucursal);
    }
  }

  @Override
  public Page<Producto> buscarProductosDeCatalogoParaVenta(BusquedaProductoCriteria criteria, Long idSucursal, Long idUsuario, Long idCliente) {
    Usuario usuarioDeConsulta = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    if (usuarioDeConsulta.getRoles().contains(Rol.COMPRADOR) && usuarioDeConsulta.getRoles().size() == 1) {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
    } else {
      Cliente clienteSeleccionado = clienteService.getClienteNoEliminadoPorId(idCliente);
      if (clienteSeleccionado == null) {
        throw new BusinessServiceException(
                messageSource.getMessage("mensaje_cliente_no_existente", null, Locale.getDefault()));
      }
      if (clienteSeleccionado.isPuedeComprarAPlazo()
              && !usuarioDeConsulta.getRoles().contains(Rol.ADMINISTRADOR)
              && !usuarioDeConsulta.getRoles().contains(Rol.ENCARGADO)
              && !usuarioDeConsulta.getRoles().contains(Rol.VENDEDOR)) {
        criteria.setListarSoloParaCatalogo(true);
      }
      return this.buscarProductos(criteria, idSucursal);
    }
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
  public Pageable getPageable(Integer pagina, List<String> ordenarPor, String sentido, int tamanioPagina) {
    int numeroDePagina = pagina != null ? pagina : 0;
    var orden = new ArrayList<Sort.Order>();
    var existenCriteriosDeOrdenamiento = ordenarPor != null && !ordenarPor.isEmpty();
    if (existenCriteriosDeOrdenamiento && sentido != null) {
      ordenarPor.forEach(nombreOrden -> {
        var criterio = SortingProducto.fromValue(nombreOrden);
        if (criterio.isEmpty()) {
          throw new BusinessServiceException(
                  messageSource.getMessage("mensaje_producto_error_sorting_busqueda", null, Locale.getDefault()));
        }
        try {
          orden.add(new Sort.Order(Sort.Direction.fromString(sentido), criterio.get().getNombre()));
          if (criterio.get() == SortingProducto.FECHA_ALTA || criterio.get() == SortingProducto.FECHA_ULTIMA_MODIFICACION) {
            orden.add(new Sort.Order(Sort.Direction.fromString(sentido), SortingProducto.ID_PRODUCTO.getNombre()));
          }
        } catch (IllegalArgumentException illegalArgumentException) {
          orden.clear();
          orden.add(new Sort.Order(Sort.Direction.DESC, SortingProducto.DESCRIPCION.getNombre()));
        }
      });
    } else {
      orden.add(new Sort.Order(Sort.Direction.ASC, SortingProducto.DESCRIPCION.getNombre()));
    }
    return PageRequest.of(numeroDePagina, tamanioPagina, Sort.by(orden));
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
    if (criteria.getListarSoloParaCatalogo() != null)
      builder.and(Boolean.TRUE.equals(criteria.getListarSoloParaCatalogo()) ? qProducto.paraCatalogo.isTrue() : qProducto.paraCatalogo.isFalse());
    if (criteria.getPublico() != null) {
      if (Boolean.TRUE.equals(criteria.getPublico())) {
        builder.and(qProducto.publico.isTrue());
      } else {
        builder.and(qProducto.publico.isFalse());
      }
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
  public Producto guardar(NuevoProductoDTO nuevoProductoDTO, long idMedida, long idRubro, long idProveedor) {
    customValidator.validar(nuevoProductoDTO);
    if (nuevoProductoDTO.getCodigo() == null) nuevoProductoDTO.setCodigo("");
    var producto = new Producto();
    producto.setMedida(medidaService.getMedidaNoEliminadaPorId(idMedida));
    producto.setRubro(rubroService.getRubroNoEliminadoPorId(idRubro));
    producto.setProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor));
    producto.setCodigo(nuevoProductoDTO.getCodigo());
    producto.setDescripcion(nuevoProductoDTO.getDescripcion());
    var altaCantidadesEnSucursales = new HashSet<CantidadEnSucursal>();
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
    producto.setParaCatalogo(nuevoProductoDTO.isParaCatalogo());
    producto.getCantidadProducto().setCantidadReservada(BigDecimal.ZERO);
    producto = productoRepository.save(producto);
    log.info(messageSource.getMessage("mensaje_producto_guardado", new Object[] {producto}, Locale.getDefault()));
    if (nuevoProductoDTO.getImagen() != null) {
      producto.setUrlImagen(this.subirImagenProducto(producto.getIdProducto(), nuevoProductoDTO.getImagen()));
    }
    return producto;
  }

  @Override
  @Transactional
  public void actualizar(Producto productoPorActualizar, Producto productoPersistido, byte[] imagen) {
    productoPorActualizar.setFechaAlta(productoPersistido.getFechaAlta());
    productoPorActualizar.setFechaUltimaModificacion(LocalDateTime.now());
    productoPorActualizar.getCantidadProducto().setCantidadReservada(productoPersistido.getCantidadProducto().getCantidadReservada());
    customValidator.validar(productoPorActualizar);
    productoPorActualizar.setEliminado(productoPersistido.isEliminado());
    if ((productoPersistido.getUrlImagen() != null && !productoPersistido.getUrlImagen().isEmpty())
            && (imagen != null && imagen.length == 0)) {
      imageUploaderService.borrarImagen(Producto.class.getSimpleName() + productoPersistido.getIdProducto());
    } else {
      productoPorActualizar.setUrlImagen(productoPersistido.getUrlImagen());
    }
    this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, productoPorActualizar);
    this.calcularPrecioBonificado(productoPorActualizar);
    if (productoPersistido.isPublico() && !productoPorActualizar.isPublico()) {
      carritoCompraService.eliminarItem(productoPersistido.getIdProducto());
      this.quitarProductoDeFavoritos(productoPersistido);
    }
    //se setea siempre en false momentaneamente
    productoPorActualizar.getCantidadProducto().setIlimitado(false);
    productoPorActualizar.setVersion(productoPersistido.getVersion());
    productoPorActualizar = productoRepository.save(productoPorActualizar);
    log.info(
        messageSource.getMessage(
            "mensaje_producto_actualizado",
            new Object[] {productoPorActualizar},
            Locale.getDefault()));
    if (imagen != null && imagen.length > 0) this.subirImagenProducto(productoPorActualizar.getIdProducto(), imagen);
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
              log.warn(messageSource.getMessage(MENSAJE_ERROR_ACTUALIZAR_STOCK_PRODUCTO_ELIMINADO,null, Locale.getDefault()));
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
                log.warn(messageSource.getMessage(MENSAJE_ERROR_ACTUALIZAR_STOCK_PRODUCTO_ELIMINADO,null, Locale.getDefault()));
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
            log.warn(messageSource.getMessage(MENSAJE_ERROR_ACTUALIZAR_STOCK_PRODUCTO_ELIMINADO,null, Locale.getDefault()));
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
            log.warn(messageSource.getMessage(MENSAJE_ERROR_ACTUALIZAR_STOCK_PRODUCTO_ELIMINADO,null, Locale.getDefault()));
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
    log.info(messageSource.getMessage(
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
    log.info(messageSource.getMessage(
            "mensaje_producto_quita_stock",
            new Object[] {cantidad, producto},
            Locale.getDefault()));
    return producto;
  }

  @Override
  @Transactional
  public void eliminarMultiplesProductos(Set<Long> idProducto) {
    var productos = productoRepository.findByIdProductoInAndEliminadoFalse(idProducto);
    for (Producto p : productos) {
      carritoCompraService.eliminarItem(p.getIdProducto());
      this.quitarProductoDeFavoritos(p);
      p.setEliminado(true);
      if (p.getUrlImagen() != null && !p.getUrlImagen().isEmpty()) {
        imageUploaderService.borrarImagen(Producto.class.getSimpleName() + p.getIdProducto());
      }
    }
    productoRepository.saveAll(productos);
    log.info("Los productos se eliminaron correctamente. {}", productos);
  }

  @Override
  @Transactional
  public void actualizarMultiples(ProductosParaActualizarDTO productosParaActualizarDTO, Usuario usuarioLogueado) {
    boolean actualizaPrecios = productosParaActualizarDTO.getGananciaPorcentaje() != null
            && productosParaActualizarDTO.getIvaPorcentaje() != null
            && productosParaActualizarDTO.getPrecioCosto() != null;
    boolean aplicaDescuentoRecargoPorcentaje = productosParaActualizarDTO.getDescuentoRecargoPorcentaje() != null;
    if (aplicaDescuentoRecargoPorcentaje && actualizaPrecios) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_modificar_producto_no_permitido", null, Locale.getDefault()));
    }
    var productos = productoRepository.findByIdProductoInAndEliminadoFalse(productosParaActualizarDTO.getIdProducto());
    BigDecimal multiplicador = BigDecimal.ZERO;
    if (aplicaDescuentoRecargoPorcentaje) {
      if (productosParaActualizarDTO.getDescuentoRecargoPorcentaje().compareTo(BigDecimal.ZERO) > 0) {
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
        var rubro = rubroService.getRubroNoEliminadoPorId(productosParaActualizarDTO.getIdRubro());
        p.setRubro(rubro);
      }
      if (productosParaActualizarDTO.getIdProveedor() != null) {
        var proveedor = proveedorService.getProveedorNoEliminadoPorId(productosParaActualizarDTO.getIdProveedor());
        p.setProveedor(proveedor);
      }
      if (usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)
              && productosParaActualizarDTO.getCantidadVentaMinima() != null
              && productosParaActualizarDTO.getCantidadVentaMinima().compareTo(BigDecimal.ZERO) > 0) {
          p.getCantidadProducto().setCantMinima(productosParaActualizarDTO.getCantidadVentaMinima());
      }
      if (usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR) && productosParaActualizarDTO.getParaCatalogo() != null) {
        p.setParaCatalogo(productosParaActualizarDTO.getParaCatalogo());
      }
      if (actualizaPrecios) {
        p.getPrecioProducto().setPrecioCosto(productosParaActualizarDTO.getPrecioCosto());
        p.getPrecioProducto().setGananciaPorcentaje(productosParaActualizarDTO.getGananciaPorcentaje());
        p.getPrecioProducto().setGananciaNeto(
                this.calcularGananciaNeto(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setPrecioVentaPublico(
                this.calcularPVP(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setIvaPorcentaje(productosParaActualizarDTO.getIvaPorcentaje());
        p.getPrecioProducto().setIvaNeto(
                this.calcularIVANeto(p.getPrecioProducto().getPrecioVentaPublico(), p.getPrecioProducto().getIvaPorcentaje()));
        p.getPrecioProducto().setPrecioLista(
                this.calcularPrecioLista(p.getPrecioProducto().getPrecioVentaPublico(), p.getPrecioProducto().getIvaPorcentaje()));
        p.setFechaUltimaModificacion(LocalDateTime.now());
        if (productosParaActualizarDTO.getPorcentajeBonificacionOferta() != null
                && productosParaActualizarDTO.getPorcentajeBonificacionOferta().compareTo(BigDecimal.ZERO) > 0) {
          p.getPrecioProducto().setOferta(true);
          p.getPrecioProducto().setPorcentajeBonificacionOferta(productosParaActualizarDTO.getPorcentajeBonificacionOferta());
        } else {
          p.getPrecioProducto().setOferta(false);
        }
      }
      if (aplicaDescuentoRecargoPorcentaje) {
        p.getPrecioProducto().setPrecioCosto(p.getPrecioProducto().getPrecioCosto().multiply(multiplicador));
        p.getPrecioProducto().setGananciaNeto(
                this.calcularGananciaNeto(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setPrecioVentaPublico(
                this.calcularPVP(p.getPrecioProducto().getPrecioCosto(), p.getPrecioProducto().getGananciaPorcentaje()));
        p.getPrecioProducto().setIvaNeto(
                this.calcularIVANeto(p.getPrecioProducto().getPrecioVentaPublico(), p.getPrecioProducto().getIvaPorcentaje()));
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
        if (Boolean.FALSE.equals(productosParaActualizarDTO.getPublico())) carritoCompraService.eliminarItem(p.getIdProducto());
      }
      if (productosParaActualizarDTO.getPorcentajeBonificacionPrecio() != null
              && productosParaActualizarDTO.getPorcentajeBonificacionPrecio().compareTo(BigDecimal.ZERO) >= 0) {
        p.getPrecioProducto().setPorcentajeBonificacionPrecio(productosParaActualizarDTO.getPorcentajeBonificacionPrecio());
      }
      this.calcularPrecioBonificado(p);
      this.validarReglasDeNegocio(TipoDeOperacion.ACTUALIZACION, p);
    }
    productoRepository.saveAll(productos);
    log.info("Los productos se modificaron correctamente. {}", productos);
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
  public void eliminarCantidadesDeSucursal(Sucursal sucursal) {
    List<Producto> productos =
            productoRepository.findAllByEliminado(false);

    CantidadEnSucursal cantidadAuxiliar = new CantidadEnSucursal();
    cantidadAuxiliar.setSucursal(sucursal);

    productos.forEach(producto -> producto.getCantidadProducto().getCantidadEnSucursales().remove(cantidadAuxiliar));
    this.productoRepository.saveAll(productos);
  }

  @Override
  @Transactional
  public String subirImagenProducto(long idProducto, byte[] imagen) {
    if (imagen.length > TAMANIO_MAXIMO_IMAGEN)
      throw new BusinessServiceException(
          messageSource.getMessage("mensaje_error_tamanio_no_valido", null, Locale.getDefault()));
    var urlImagen = imageUploaderService.subirImagen(Producto.class.getSimpleName() + idProducto, imagen);
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
    return productoRepository.findByCodigoAndEliminado(codigo, false).orElse(null);
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
              messageSource.getMessage("mensaje_producto_consulta_stock_sin_sucursal", null, Locale.getDefault()));
    }
    List<ProductoFaltanteDTO> productosFaltantes = new ArrayList<>();
    int longitudIds = productosParaVerificarStockDTO.getIdProducto().length;
    int longitudCantidades = productosParaVerificarStockDTO.getCantidad().length;
    HashMap<Long, BigDecimal> listaIdsAndCantidades = new HashMap<>();
    this.validarLongitudDeArrays(longitudIds, longitudCantidades);
    if (productosParaVerificarStockDTO.getIdPedido() != null) {
      var renglonesDelPedido = pedidoService.getRenglonesDelPedidoOrdenadorPorIdRenglon(productosParaVerificarStockDTO.getIdPedido());
      if (!renglonesDelPedido.isEmpty()) {
        renglonesDelPedido.forEach(
                renglonPedido -> listaIdsAndCantidades.put(renglonPedido.getIdProductoItem(), renglonPedido.getCantidad()));
      }
    }
    for (int i = 0; i < longitudIds; i++) {
      var producto = this.getProductoNoEliminadoPorId(productosParaVerificarStockDTO.getIdProducto()[i]);
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
    if (longitudIds != longitudCantidades) {
      throw new BusinessServiceException(
              messageSource.getMessage("mensaje_error_logitudes_arrays", null, Locale.getDefault()));
    }
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
  public ProductoFaltanteDTO construirNuevoProductoFaltante(
          Producto producto, BigDecimal cantidadSolicitada, BigDecimal cantidadDisponible, long idSucursal) {
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
  public void procesarReporteListaDePrecios(BusquedaProductoCriteria criteria, long idSucursal, FormatoReporte formato) {
    var productos = this.buscarProductosParaReporte(criteria);
    var reporte = this.getReporteListaDePrecios(productos, formato);
    this.enviarListaDeProductosPorEmail(sucursalService.getSucursalPorId(idSucursal).getEmail(), reporte, formato);
  }

  @Override
  public void enviarListaDeProductosPorEmail(String mailTo, byte[] listaDeProductos, FormatoReporte formato) {
    emailServiceFactory.getEmailService(emailDefaultProvider)
            .enviarEmail(
                    mailTo,
                    "",
                    "Listado de productos",
                    "Adjunto se encuentra el listado de productos solicitado",
                    listaDeProductos,
                    "ListaDeProductos." + formato.toString());
  }

  @Override
  public byte[] getReporteListaDePrecios(List<Producto> productos, FormatoReporte formato) {
    var params = new HashMap<String, Object>();
    var sucursalPredeterminada = sucursalService.getSucursalPredeterminada();
    if (sucursalPredeterminada.getLogo() != null && !sucursalPredeterminada.getLogo().isEmpty()) {
      try {
        params.put("logo", new ImageIcon(ImageIO.read(new URL(sucursalPredeterminada.getLogo()))).getImage());
        params.put("productoSinImagen",
                new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource(PRODUCTO_SIN_IMAGEN))))
                        .getImage());
      } catch (IOException | NullPointerException ex) {
        throw new ServiceException(messageSource.getMessage("mensaje_recurso_no_encontrado", null, Locale.getDefault()), ex);
      }
    }
    return jasperReportsHandler.compilar("report/ListaPreciosProductos.jrxml", params, productos, formato);
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
      log.info(messageSource.getMessage(
              "mensaje_producto_favorito_agregado",
              new Object[]{producto},
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
    log.info(messageSource.getMessage(
            "mensaje_producto_favorito_quitado",
            new Object[] {producto},
            Locale.getDefault()));
  }

  private void quitarProductoDeFavoritos(Producto producto) {
    productoFavoritoRepository.deleteAllByProducto(producto);
    log.info(messageSource.getMessage(
            "mensaje_producto_favorito_quitado",
            new Object[] {producto},
            Locale.getDefault()));
  }

  @Override
  @Transactional
  public void quitarProductosDeFavoritos(long idUsuario) {
    Cliente cliente = clienteService.getClientePorIdUsuario(idUsuario);
    productoFavoritoRepository.deleteAllByCliente(cliente);
    log.info(messageSource.getMessage(
            "mensaje_producto_favoritos_quitados", null, Locale.getDefault()));
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
            .ilimitado(productoDTO.isIlimitado())
            .build();
  }

  @Override
  public Page<Producto> getProductosRelacionados(long idProducto, long idSucursal, int pagina) {
    Pageable pageable = PageRequest.of(pagina, TAMANIO_PAGINA_DEFAULT);
    var productosRelacionados =
            productoRepository.buscarProductosRelacionadosPorRubro(
                    this.getProductoNoEliminadoPorId(idProducto).getRubro().getIdRubro(), idProducto, pageable);
    productosRelacionados.forEach(producto -> this.calcularCantidadEnSucursalesDisponible(producto, idSucursal));
    return productoRepository.buscarProductosRelacionadosPorRubro(
            this.getProductoNoEliminadoPorId(idProducto).getRubro().getIdRubro(), idProducto, pageable);
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
