package sic.controller;

import java.math.BigDecimal;
import java.util.*;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaProductoCriteria;
import sic.modelo.dto.*;
import sic.service.*;
import sic.exception.BusinessServiceException;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1")
public class ProductoController {

  private final IProductoService productoService;
  private final IMedidaService medidaService;
  private final IRubroService rubroService;
  private final IProveedorService proveedorService;
  private final ISucursalService sucursalService;
  private final IUsuarioService usuarioService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public ProductoController(
    IProductoService productoService,
    IMedidaService medidaService,
    IRubroService rubroService,
    IProveedorService proveedorService,
    ISucursalService sucursalService,
    IUsuarioService usuarioService,
    IAuthService authService,
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

  @GetMapping("/productos/{idProducto}/sucursales/{idSucursal}")
  public Producto getProductoPorId(
      @PathVariable long idProducto,
      @PathVariable long idSucursal,
      @RequestParam(required = false) Boolean publicos,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    productoService.calcularCantidadEnSucursalesDisponible(producto, idSucursal);
    productoService.calcularCantidadReservada(producto, idSucursal);
    if (publicos != null && publicos && !producto.isPublico()) {
      throw new EntityNotFoundException(
              messageSource.getMessage("mensaje_producto_no_existente", null, Locale.getDefault()));
    }
    if (authorizationHeader != null) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
      if (productoService.isFavorito(idUsuarioLoggedIn, idProducto)) producto.setFavorito(true);
    }
    return producto;
  }

  @GetMapping("/productos/busqueda/sucursales/{idSucursal}")
  public Producto getProductoPorCodigo(@PathVariable long idSucursal,
                                       @RequestParam String codigo) {
    Producto producto = productoService.getProductoPorCodigo(codigo);
    productoService.calcularCantidadEnSucursalesDisponible(producto, idSucursal);
    productoService.calcularCantidadReservada(producto, idSucursal);
    return producto;
  }

  @PostMapping("/productos/busqueda/criteria/sucursales/{idSucursal}")
  public Page<Producto> buscarProductos(
      @PathVariable long idSucursal,
      @RequestBody BusquedaProductoCriteria criteria,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Page<Producto> productos = productoService.buscarProductos(criteria, idSucursal);
    if (authorizationHeader != null) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
      productoService.marcarFavoritos(productos, idUsuarioLoggedIn);
    }
    return productos;
  }

  @PostMapping("/productos/valor-stock/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularValorStock(@RequestBody BusquedaProductoCriteria criteria) {
    return productoService.calcularValorStock(criteria);
  }

  @PostMapping("/productos/reporte/criteria/sucursales/{idSucursal}")
  public void getListaDePrecios(
          @RequestBody BusquedaProductoCriteria criteria,
          @PathVariable long idSucursal,
          @RequestParam(required = false) String formato) {
    if (formato == null || formato.isEmpty()) {
      formato = "xlsx";
    }
    switch (formato) {
      case "xlsx" ->
              productoService.getListaDePreciosEnXls(criteria, idSucursal);
      case "pdf" ->
              productoService.getListaDePreciosEnPdf(criteria, idSucursal);
      default -> throw new BusinessServiceException(messageSource.getMessage(
              "mensaje_formato_no_valido", null, Locale.getDefault()));
    }
  }

  @DeleteMapping("/productos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public void eliminarMultiplesProductos(@RequestParam long[] idProducto) {
    productoService.eliminarMultiplesProductos(idProducto);
  }

  @PutMapping("/productos")
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
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(Long.parseLong(claims.get(CLAIM_ID_USUARIO).toString()));
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
      if (productoPorActualizar.getCantidadProducto().getBulto() == null)
        productoPorActualizar.getCantidadProducto().setBulto(productoPersistido.getCantidadProducto().getBulto());
    } else {
        productoPorActualizar.getCantidadProducto().setCantidadEnSucursales(productoPersistido.getCantidadProducto().getCantidadEnSucursales());
        productoPorActualizar.getCantidadProducto().setBulto(productoPersistido.getCantidadProducto().getBulto());
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

  @PostMapping("/productos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Producto guardar(
      @RequestBody NuevoProductoDTO nuevoProductoDTO,
      @RequestParam Long idMedida,
      @RequestParam Long idRubro,
      @RequestParam Long idProveedor) {
    return productoService.guardar(nuevoProductoDTO, idMedida, idRubro, idProveedor);
  }

  @PutMapping("/productos/multiples")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizarMultiplesProductos(
    @RequestBody ProductosParaActualizarDTO productosParaActualizarDTO,
    @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLogueado = usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get(CLAIM_ID_USUARIO)).longValue());
    productoService.actualizarMultiples(productosParaActualizarDTO, usuarioLogueado);
  }

  @PostMapping("/productos/disponibilidad-stock")
  public List<ProductoFaltanteDTO> verificarDisponibilidadStock(
      @RequestBody ProductosParaVerificarStockDTO productosParaVerificarStockDTO) {
    return productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
  }

  @PostMapping("/productos/{idProducto}/favoritos")
  public void marcarComoFavorito(
          @PathVariable long idProducto,
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    productoService.guardarProductoFavorito(idUsuarioLoggedIn, idProducto);
  }

  @GetMapping("/productos/favoritos/sucursales/{idSucursal}")
  public Page<Producto> getProductosFavoritosDelCliente(
          @PathVariable long idSucursal,
          @RequestParam int pagina,
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return productoService.getPaginaProductosFavoritosDelCliente(idUsuarioLoggedIn, idSucursal, pagina);
  }

  @DeleteMapping("/productos/{idProducto}/favoritos")
  public void quitarProductoDeFavoritos(
          @PathVariable long idProducto,
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    productoService.quitarProductoDeFavoritos(idUsuarioLoggedIn, idProducto);
  }

  @DeleteMapping("/productos/favoritos")
  public void quitarProductosDeFavoritos(@RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    productoService.quitarProductosDeFavoritos(idUsuarioLoggedIn);
  }

  @GetMapping("/productos/favoritos/cantidad")
  public Long getCantidadDeProductosFavoritos(@RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return productoService.getCantidadDeProductosFavoritos(idUsuarioLoggedIn);
  }

  @GetMapping("/productos/{idProducto}/recomendados")
  public Page<Producto> getProductosRecomendados(@PathVariable long idProducto, @RequestParam int pagina) {
    return productoService.getProductosRelacionados(idProducto, pagina);
  }
}
