package sic.controller;

import java.math.BigDecimal;
import java.util.*;
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
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;

  @Autowired
  public ProductoController(
    IProductoService productoService,
    IMedidaService medidaService,
    IRubroService rubroService,
    IProveedorService proveedorService,
    ISucursalService sucursalService,
    IAuthService authService,
    ModelMapper modelMapper,
    MessageSource messageSource) {
    this.productoService = productoService;
    this.medidaService = medidaService;
    this.rubroService = rubroService;
    this.proveedorService = proveedorService;
    this.sucursalService = sucursalService;
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
    if (!producto.isOferta()
        && authorizationHeader != null
        && authService.esAuthorizationHeaderValido(authorizationHeader)) {
      Page<Producto> productos =
          productoService.getProductosConPrecioBonificado(
              new PageImpl<>(Collections.singletonList(producto)));
      producto = productos.getContent().get(0);
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
    if (authorizationHeader != null
        && authService.esAuthorizationHeaderValido(authorizationHeader)) {
      return productoService.getProductosConPrecioBonificado(productos);
    } else {
      return productos;
    }
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
        return productoService.getListaDePreciosEnXls(criteria);
      }
      case "pdf" -> {
        return productoService.getListaDePreciosEnPdf(criteria);
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
      @RequestParam(required = false) Long idProveedor) {
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
    if (productoPorActualizar.getPorcentajeBonificacionOferta() == null)
      productoPorActualizar.setPorcentajeBonificacionOferta(
          productoPersistido.getPorcentajeBonificacionOferta());
    if (productoPorActualizar.getPorcentajeBonificacionPrecio() == null)
      productoPorActualizar.setPorcentajeBonificacionPrecio(
          productoPersistido.getPorcentajeBonificacionPrecio());
    if (productoPorActualizar.getBulto() == null)
      productoPorActualizar.setBulto(productoPersistido.getBulto());
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
    @RequestBody ProductosParaActualizarDTO productosParaActualizarDTO) {
    productoService.actualizarMultiples(productosParaActualizarDTO);
  }

  @PostMapping("/productos/disponibilidad-stock")
  public List<ProductoFaltanteDTO> verificarDisponibilidadStock(
      @RequestBody ProductosParaVerificarStockDTO productosParaVerificarStockDTO) {
    return productoService.getProductosSinStockDisponible(productosParaVerificarStockDTO);
  }
}
