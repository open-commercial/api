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
import sic.modelo.dto.ProductosParaActualizarDTO;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoDTO;
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
  private final IEmpresaService empresaService;
  private final IClienteService clienteService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;

  @Autowired
  public ProductoController(
    IProductoService productoService,
    IMedidaService medidaService,
    IRubroService rubroService,
    IProveedorService proveedorService,
    IEmpresaService empresaService,
    IClienteService clienteService,
    IAuthService authService,
    ModelMapper modelMapper,
    MessageSource messageSource) {
    this.productoService = productoService;
    this.medidaService = medidaService;
    this.rubroService = rubroService;
    this.proveedorService = proveedorService;
    this.empresaService = empresaService;
    this.clienteService = clienteService;
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
      throw new EntityNotFoundException(messageSource.getMessage(
        "mensaje_producto_no_existente", null, Locale.getDefault()));
    }
    if (authorizationHeader != null && authService.esAuthorizationHeaderValido(authorizationHeader)) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      Cliente cliente =
        clienteService.getClientePorIdUsuarioYidEmpresa(
          (int) claims.get("idUsuario"), producto.getEmpresa().getId_Empresa());
      if (cliente != null) {
        Page<Producto> productos =
          productoService.getProductosConPrecioBonificado(
            new PageImpl<>(Collections.singletonList(producto)), cliente);
        return productos.getContent().get(0);
      } else {
        return producto;
      }
    } else {
      return producto;
    }
  }

  @GetMapping("/productos/busqueda")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Producto getProductoPorCodigo(@RequestParam long idEmpresa, @RequestParam String codigo) {
    return productoService.getProductoPorCodigo(codigo, idEmpresa);
  }

  @PostMapping("/productos/busqueda/criteria")
  public Page<Producto> buscarProductos(
      @RequestBody BusquedaProductoCriteria criteria,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Page<Producto> productos = productoService.buscarProductos(criteria);
    if (authorizationHeader != null
        && authService.esAuthorizationHeaderValido(authorizationHeader)) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      Cliente cliente =
          clienteService.getClientePorIdUsuarioYidEmpresa(
              (int) claims.get("idUsuario"), criteria.getIdEmpresa());
      if (cliente != null) {
        return productoService.getProductosConPrecioBonificado(productos, cliente);
      } else {
        return productos;
      }
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
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public ResponseEntity<byte[]> getListaDePrecios(
    @RequestBody BusquedaProductoCriteria criteria,
    @RequestParam(required = false) String formato) {
    HttpHeaders headers = new HttpHeaders();
    List<Producto> productos;
    switch (formato) {
      case "xlsx":
        headers.setContentType(new MediaType("application", "vnd.ms-excel"));
        headers.set("Content-Disposition", "attachment; filename=ListaPrecios.xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        productos = productoService.buscarProductos(criteria).getContent();
        byte[] reporteXls =
          productoService.getListaDePreciosPorEmpresa(productos, criteria.getIdEmpresa(), formato);
        headers.setContentLength(reporteXls.length);
        return new ResponseEntity<>(reporteXls, headers, HttpStatus.OK);
      case "pdf":
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("content-disposition", "inline; filename=ListaPrecios.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        productos = productoService.buscarProductos(criteria).getContent();
        byte[] reportePDF =
          productoService.getListaDePreciosPorEmpresa(productos, criteria.getIdEmpresa(), formato);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
      default:
        throw new BusinessServiceException(messageSource.getMessage(
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
      @RequestParam(required = false) Long idEmpresa) {
    Producto productoPorActualizar = modelMapper.map(productoDTO, Producto.class);
    Producto productoPersistido =
        productoService.getProductoNoEliminadoPorId(productoPorActualizar.getIdProducto());
    if (productoPersistido != null) {
      if (idMedida != null) productoPorActualizar.setMedida(medidaService.getMedidaNoEliminadaPorId(idMedida));
      else productoPorActualizar.setMedida(productoPersistido.getMedida());
      if (idRubro != null) productoPorActualizar.setRubro(rubroService.getRubroNoEliminadoPorId(idRubro));
      else productoPorActualizar.setRubro(productoPersistido.getRubro());
      if (idProveedor != null) productoPorActualizar.setProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor));
      else productoPorActualizar.setProveedor(productoPersistido.getProveedor());
      if (idEmpresa != null)
        productoPorActualizar.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
      else productoPorActualizar.setEmpresa(productoPersistido.getEmpresa());
      productoService.actualizar(productoPorActualizar, productoPersistido);
    }
  }

  @PostMapping("/productos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Producto guardar(
    @RequestBody NuevoProductoDTO nuevoProductoDTO,
    @RequestParam Long idMedida,
    @RequestParam Long idRubro,
    @RequestParam Long idProveedor,
    @RequestParam Long idEmpresa) {
    Producto producto = new Producto();
    producto.setMedida(medidaService.getMedidaNoEliminadaPorId(idMedida));
    producto.setRubro(rubroService.getRubroNoEliminadoPorId(idRubro));
    producto.setProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor));
    producto.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    producto.setCodigo(nuevoProductoDTO.getCodigo());
    producto.setDescripcion(nuevoProductoDTO.getDescripcion());
    producto.setCantidad(nuevoProductoDTO.getCantidad());
    producto.setHayStock(nuevoProductoDTO.isHayStock());
    producto.setPrecioBonificado(nuevoProductoDTO.getPrecioBonificado());
    producto.setCantMinima(nuevoProductoDTO.getCantMinima());
    producto.setBulto(nuevoProductoDTO.getBulto());
    producto.setPrecioCosto(nuevoProductoDTO.getPrecioCosto());
    producto.setGananciaPorcentaje(nuevoProductoDTO.getGananciaPorcentaje());
    producto.setGananciaNeto(nuevoProductoDTO.getGananciaNeto());
    producto.setPrecioVentaPublico(nuevoProductoDTO.getPrecioVentaPublico());
    producto.setIvaPorcentaje(nuevoProductoDTO.getIvaPorcentaje());
    producto.setIvaNeto(nuevoProductoDTO.getIvaNeto());
    producto.setPrecioLista(nuevoProductoDTO.getPrecioLista());
    producto.setIlimitado(nuevoProductoDTO.isIlimitado());
    producto.setPublico(nuevoProductoDTO.isPublico());
    producto.setEstante(nuevoProductoDTO.getEstante());
    producto.setEstanteria(nuevoProductoDTO.getEstanteria());
    producto.setNota(nuevoProductoDTO.getNota());
    producto.setFechaVencimiento(nuevoProductoDTO.getFechaVencimiento());
    return productoService.guardar(producto);
  }

  @PostMapping("/productos/{idProducto}/imagenes")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public String subirImagen(@PathVariable long idProducto, @RequestBody byte[] imagen) {
    return productoService.subirImagenProducto(idProducto, imagen);
  }

  @PutMapping("/productos/multiples")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizarMultiplesProductos(
    @RequestBody ProductosParaActualizarDTO productosParaActualizarDTO) {
    productoService.actualizarMultiples(productosParaActualizarDTO);
  }

  @GetMapping("/productos/disponibilidad-stock")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Map<Long, BigDecimal> verificarDisponibilidadStock(
      long[] idProducto, BigDecimal[] cantidad) {
    return productoService.getProductosSinStockDisponible(idProducto, cantidad);
  }
}
