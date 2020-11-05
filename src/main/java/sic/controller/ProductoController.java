package sic.controller;

import java.math.BigDecimal;
import java.util.*;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  @GetMapping("/productos/{idProducto}")
  public Producto getProductoPorId(
      @PathVariable long idProducto,
      @RequestParam(required = false) Boolean publicos,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
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

  @GetMapping("/productos/busqueda")
  public Producto getProductoPorCodigo(@RequestParam String codigo) {
    return productoService.getProductoPorCodigo(codigo);
  }

  @PostMapping("/productos/busqueda/criteria")
  public Page<Producto> buscarProductos(
      @RequestBody BusquedaProductoCriteria criteria,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Page<Producto> productos = productoService.buscarProductos(criteria);
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

  @PostMapping("/productos/reporte/criteria")
  public ResponseEntity<byte[]> getListaDePrecios(
          @RequestBody BusquedaProductoCriteria criteria,
          @RequestParam(required = false) String formato) {
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    if (formato == null || formato.isEmpty()) {
      formato = "xlsx";
    }
    switch (formato) {
      case "xlsx" -> {
        headers.setContentType(new MediaType("application", "vnd.ms-excel"));
        headers.set("Content-Disposition", "attachment; filename=ListaPrecios.xlsx");
        return new ResponseEntity<>(productoService.getListaDePreciosEnXls(criteria), headers, HttpStatus.OK);
      }
      case "pdf" -> {
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "inline; filename=ListaPrecios.pdf");
        return new ResponseEntity<>(productoService.getListaDePreciosEnPdf(criteria), headers, HttpStatus.OK);
      }
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
      productoPorActualizar.setCantidadEnSucursales(cantidadEnSucursales);
      productoPorActualizar.getCantidadEnSucursales().addAll(productoPersistido.getCantidadEnSucursales());
      productoPorActualizar.setCantidadTotalEnSucursales(
              cantidadEnSucursales
                      .stream()
                      .map(CantidadEnSucursal::getCantidad)
                      .reduce(BigDecimal.ZERO, BigDecimal::add));
      productoPorActualizar.setHayStock(
              productoPorActualizar.getCantidadTotalEnSucursales().compareTo(BigDecimal.ZERO) > 0);
      if (productoPorActualizar.getBulto() == null)
        productoPorActualizar.setBulto(productoPersistido.getBulto());
    } else {
        productoPorActualizar.setCantidadEnSucursales(productoPersistido.getCantidadEnSucursales());
        productoPorActualizar.setBulto(productoPersistido.getBulto());
    }
    if (productoPorActualizar.getPorcentajeBonificacionOferta() == null)
      productoPorActualizar.setPorcentajeBonificacionOferta(
          productoPersistido.getPorcentajeBonificacionOferta());
    if (productoPorActualizar.getPorcentajeBonificacionPrecio() == null)
      productoPorActualizar.setPorcentajeBonificacionPrecio(
          productoPersistido.getPorcentajeBonificacionPrecio());
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

  @GetMapping("/productos/favoritos")
  public Page<Producto> getProductosFavoritosDelCliente(
          @RequestParam int pagina,
          @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get(CLAIM_ID_USUARIO);
    return productoService.getPaginaProductosFavoritosDelCliente(idUsuarioLoggedIn, pagina);
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
}
