package sic.controller;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.*;
import sic.service.*;
import sic.util.FormatoReporte;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
public class ProductoController {

  private final ProductoService productoService;
  private final MedidaService medidaService;
  private final RubroService rubroService;
  private final ProveedorService proveedorService;
  private final SucursalService sucursalService;
  private final UsuarioService usuarioService;
  private final AuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public ProductoController(ProductoService productoService,
                            MedidaService medidaService,
                            RubroService rubroService,
                            ProveedorService proveedorService,
                            SucursalService sucursalService,
                            UsuarioService usuarioService,
                            AuthService authService,
                            ModelMapper modelMapper,
                            MessageSource messageSource) {
    this.productoService = productoService;
    this.medidaService = medidaService;
    this.rubroService = rubroService;
    this.proveedorService = proveedorService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.authService = authService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  @GetMapping("/api/v1/productos/{idProducto}/sucursales/{idSucursal}")
  public Producto getProductoPorId(
      @PathVariable long idProducto,
      @PathVariable long idSucursal,
      @RequestParam(required = false) Boolean publicos,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    productoService.calcularCantidadEnSucursalesDisponible(producto, idSucursal);
    if (publicos != null && publicos && !producto.isPublico()) {
      throw new EntityNotFoundException(
              messageSource.getMessage("mensaje_producto_no_existente", null, Locale.getDefault()));
    }
    if (authorizationHeader != null) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
      if (productoService.isFavorito(idUsuarioLoggedIn, idProducto)) producto.setFavorito(true);
    }
    return producto;
  }

  @GetMapping("/api/v1/productos/busqueda/sucursales/{idSucursal}")
  public Producto getProductoPorCodigo(@PathVariable long idSucursal,
                                       @RequestParam String codigo) {
    Producto producto = productoService.getProductoPorCodigo(codigo);
    productoService.calcularCantidadEnSucursalesDisponible(producto, idSucursal);
    return producto;
  }

  @PostMapping("/api/v1/productos/busqueda/criteria/sucursales/{idSucursal}")
  public Page<Producto> buscarProductos(
      @PathVariable long idSucursal,
      @RequestBody BusquedaProductoCriteria criteria,
      @RequestParam(required = false) Long idCliente,
      @RequestParam(required = false) Movimiento movimiento,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Page<Producto> productos;
    boolean esAutogestion = authorizationHeader != null &&
            movimiento != null &&
            movimiento.equals(Movimiento.COMPRA)
            && idCliente == null;
    boolean esGestionAdministrativa = authorizationHeader != null &&
            movimiento != null &&
            movimiento.equals(Movimiento.VENTA)
            && idCliente != null;
    if (esAutogestion) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
      productos = productoService.buscarProductosDeCatalogoParaUsuario(criteria, idSucursal, idUsuarioLoggedIn);
      productoService.marcarFavoritos(productos, idUsuarioLoggedIn);
    } else if (esGestionAdministrativa) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
      productos = productoService.buscarProductosDeCatalogoParaVenta(criteria, idSucursal, idUsuarioLoggedIn, idCliente);
      productoService.marcarFavoritos(productos, idUsuarioLoggedIn);
    } else {
      productos = productoService.buscarProductos(criteria, idSucursal);
    }
    return productos;
  }

  @PostMapping("/api/v1/productos/valor-stock/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularValorStock(@RequestBody BusquedaProductoCriteria criteria) {
    return productoService.calcularValorStock(criteria);
  }

  @PostMapping("/api/v1/productos/reporte/criteria/sucursales/{idSucursal}")
  public void getListaDePrecios(@RequestBody BusquedaProductoCriteria criteria,
                                @PathVariable long idSucursal,
                                @RequestParam(required = false) String formato) {
    if (formato == null || formato.isEmpty()) formato = "pdf";
    switch (formato) {
      case "xlsx" -> productoService.procesarReporteListaDePrecios(criteria, idSucursal, FormatoReporte.XLSX);
      case "pdf" -> productoService.procesarReporteListaDePrecios(criteria, idSucursal, FormatoReporte.PDF);
      default -> throw new BusinessServiceException(
              messageSource.getMessage("mensaje_formato_no_valido", null, Locale.getDefault()));
    }
  }

  @DeleteMapping("/api/v1/productos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public void eliminarMultiplesProductos(@RequestParam Set<Long> idProducto) {
    productoService.eliminarMultiplesProductos(idProducto);
  }

  @PutMapping("/api/v1/productos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(
      @RequestBody ProductoDTO productoDTO,
      @RequestParam(required = false) Long idMedida,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestHeader("Authorization") String authorizationHeader) {
    Producto productoPorActualizar = modelMapper.map(productoDTO, Producto.class);
    productoPorActualizar.setCantidadProducto(productoService.construirCantidadProductoEmbeddable(productoDTO));
    productoPorActualizar.setPrecioProducto(productoService.construirPrecioProductoEmbeddable(productoDTO));
    Producto productoPersistido =
        productoService.getProductoNoEliminadoPorId(productoPorActualizar.getIdProducto());
      if (idMedida != null) productoPorActualizar.setMedida(medidaService.getMedidaNoEliminadaPorId(idMedida));
      else productoPorActualizar.setMedida(productoPersistido.getMedida());
      if (idRubro != null) productoPorActualizar.setRubro(rubroService.getRubroNoEliminadoPorId(idRubro));
      else productoPorActualizar.setRubro(productoPersistido.getRubro());
    if (idProveedor != null)
      productoPorActualizar.setProveedor(
          proveedorService.getProveedorNoEliminadoPorId(idProveedor));
    else productoPorActualizar.setProveedor(productoPersistido.getProveedor());
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class));
    if (usuarioLogueado.getRoles().contains(Rol.ADMINISTRADOR)) {
      Set<CantidadEnSucursal> cantidadEnSucursales = new HashSet<>();
      productoDTO
              .getCantidadEnSucursales()
              .forEach(
                      cantidadEnSucursalDTO -> {
                        CantidadEnSucursal cantidadEnSucursal =
                                modelMapper.map(cantidadEnSucursalDTO, CantidadEnSucursal.class);
                        cantidadEnSucursal.setSucursal(
                                sucursalService.getSucursalPorId(cantidadEnSucursalDTO.getIdSucursal()));
                        cantidadEnSucursales.add(cantidadEnSucursal);
                      });
      productoPorActualizar.getCantidadProducto().setCantidadEnSucursales(cantidadEnSucursales);
      productoPorActualizar.getCantidadProducto().getCantidadEnSucursales().addAll(productoPersistido.getCantidadProducto().getCantidadEnSucursales());
      productoPorActualizar.getCantidadProducto().setCantidadTotalEnSucursales(
              cantidadEnSucursales
                      .stream()
                      .map(CantidadEnSucursal::getCantidad)
                      .reduce(BigDecimal.ZERO, BigDecimal::add));
      if (productoPorActualizar.getCantidadProducto().getCantMinima() == null)
        productoPorActualizar.getCantidadProducto().setCantMinima(productoPersistido.getCantidadProducto().getCantMinima());
    } else {
        productoPorActualizar.getCantidadProducto().setCantidadEnSucursales(productoPersistido.getCantidadProducto().getCantidadEnSucursales());
        productoPorActualizar.getCantidadProducto().setCantMinima(productoPersistido.getCantidadProducto().getCantMinima());
        productoPorActualizar.getCantidadProducto().setCantidadTotalEnSucursales(productoPersistido.getCantidadProducto().getCantidadTotalEnSucursales());
    }
    if (productoPorActualizar.getPrecioProducto().getPorcentajeBonificacionOferta() == null)
      productoPorActualizar.getPrecioProducto().setPorcentajeBonificacionOferta(
          productoPersistido.getPrecioProducto().getPorcentajeBonificacionOferta());
    if (productoPorActualizar.getPrecioProducto().getPorcentajeBonificacionPrecio() == null)
      productoPorActualizar.getPrecioProducto().setPorcentajeBonificacionPrecio(
          productoPersistido.getPrecioProducto().getPorcentajeBonificacionPrecio());
    if (productoPorActualizar.getDescripcion() == null)
      productoPorActualizar.setDescripcion(productoPersistido.getDescripcion());
    if (productoPorActualizar.getCodigo() == null)
      productoPorActualizar.setCodigo(productoPersistido.getCodigo());
    productoService.actualizar(productoPorActualizar, productoPersistido, productoDTO.getImagen());
  }

  @PostMapping("/api/v1/productos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Producto guardar(
      @RequestBody NuevoProductoDTO nuevoProductoDTO,
      @RequestParam Long idMedida,
      @RequestParam Long idRubro,
      @RequestParam Long idProveedor) {
    return productoService.guardar(nuevoProductoDTO, idMedida, idRubro, idProveedor);
  }

  @PutMapping("/api/v1/productos/multiples")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizarMultiplesProductos(
    @RequestBody ProductosParaActualizarDTO productosParaActualizarDTO,
    @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class));
    productoService.actualizarMultiples(productosParaActualizarDTO, usuarioLogueado);
  }

  @PostMapping("/api/v1/productos/disponibilidad-stock")
  public List<ProductoFaltanteDTO> verificarDisponibilidadStock(
      @RequestBody ProductosParaVerificarStockDTO productosParaVerificarStockDTO) {
    return productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
  }

  @PostMapping("/api/v1/productos/{idProducto}/favoritos")
  public void marcarComoFavorito(
          @PathVariable long idProducto,
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    productoService.guardarProductoFavorito(idUsuarioLoggedIn, idProducto);
  }

  @GetMapping("/api/v1/productos/favoritos/sucursales/{idSucursal}")
  public Page<Producto> getProductosFavoritosDelCliente(
          @PathVariable long idSucursal,
          @RequestParam int pagina,
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return productoService.getPaginaProductosFavoritosDelCliente(idUsuarioLoggedIn, idSucursal, pagina);
  }

  @DeleteMapping("/api/v1/productos/{idProducto}/favoritos")
  public void quitarProductoDeFavoritos(
          @PathVariable long idProducto,
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    productoService.quitarProductoDeFavoritos(idUsuarioLoggedIn, idProducto);
  }

  @DeleteMapping("/api/v1/productos/favoritos")
  public void quitarProductosDeFavoritos(
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    productoService.quitarProductosDeFavoritos(idUsuarioLoggedIn);
  }

  @GetMapping("/api/v1/productos/favoritos/cantidad")
  public Long getCantidadDeProductosFavoritos(
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return productoService.getCantidadDeProductosFavoritos(idUsuarioLoggedIn);
  }

  @GetMapping("/api/v1/productos/{idProducto}/sucursales/{idSucursal}/recomendados")
  public Page<Producto> getProductosRecomendados(@PathVariable long idProducto,
                                                 @PathVariable long idSucursal,
                                                 @RequestParam int pagina) {
    return productoService.getProductosRelacionados(idProducto, idSucursal, pagina);
  }
}
