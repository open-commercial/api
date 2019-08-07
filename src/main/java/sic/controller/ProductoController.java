package sic.controller;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
import sic.modelo.dto.ProductosParaActualizarDTO;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoDTO;
import sic.service.*;
import sic.exception.BusinessServiceException;

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
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
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

  @JsonView(Views.Public.class)
  @GetMapping("/public/productos/{idProducto}/sucursal/{idSucursal}")
  public Producto getProductoPorIdPublic(
      @PathVariable long idProducto,
      @PathVariable long idSucursal,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    if (authorizationHeader != null
        && authService.esAuthorizationHeaderValido(authorizationHeader)) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      Cliente cliente =
          clienteService.getClientePorIdUsuarioYidEmpresa(
              (int) claims.get("idUsuario"), idSucursal);
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

  @JsonView(Views.Public.class)
  @GetMapping("/public/productos/busqueda/criteria")
  public Page<Producto> buscarProductosPublic(
      @RequestParam long idSucursal,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Boolean destacados,
      @RequestParam(required = false) Integer pagina,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Page<Producto> productos =
      this.buscar(
        idSucursal,
        codigo,
        descripcion,
        null,
        null,
        false,
        false,
        true,
        destacados,
        pagina,
        null,
        null,
        null);
    if (authorizationHeader != null && authService.esAuthorizationHeaderValido(authorizationHeader)) {
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      Cliente cliente =
        clienteService.getClientePorIdUsuarioYidEmpresa((int) claims.get("idUsuario"), idSucursal);
      if (cliente != null) {
        return productoService.getProductosConPrecioBonificado(productos, cliente);
      } else {
        return productos;
      }
    } else {
      return productos;
    }
  }

  @GetMapping("/productos/{idProducto}/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Producto getProductoPorId(
      @PathVariable long idProducto,
      @PathVariable long idSucursal,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Producto producto = productoService.getProductoNoEliminadoPorId(idProducto);
    Cliente cliente =
        clienteService.getClientePorIdUsuarioYidEmpresa((int) claims.get("idUsuario"), idSucursal);
    if (cliente != null) {
      Page<Producto> productos =
          productoService.getProductosConPrecioBonificado(
              new PageImpl<>(Collections.singletonList(producto)), cliente);
      return productos.getContent().get(0);
    } else {
      return producto;
    }
  }

  @GetMapping("/productos/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<Producto> buscarProductos(
      @RequestParam long idSucursal,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) boolean soloFantantes,
      @RequestParam(required = false) boolean soloEnStock,
      @RequestParam(required = false) Boolean publicos,
      @RequestParam(required = false) Boolean destacados,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Cliente cliente =
        clienteService.getClientePorIdUsuarioYidEmpresa((int) claims.get("idUsuario"), idSucursal);
    Page<Producto> productos =
        this.buscar(
            idSucursal,
            codigo,
            descripcion,
            idRubro,
            idProveedor,
            soloFantantes,
            soloEnStock,
            publicos,
            destacados,
            pagina,
            null,
            ordenarPor,
            sentido);
    if (cliente != null) {
      return productoService.getProductosConPrecioBonificado(productos, cliente);
    } else {
     return productos;
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
  public Producto getProductoPorCodigo(@RequestParam String codigo) {
    return productoService.getProductoPorCodigo(codigo);
  }

  private Page<Producto> buscar(
      long idEmpresa,
      String codigo,
      String descripcion,
      Long idRubro,
      Long idProveedor,
      boolean soloFantantes,
      boolean soloEnStock,
      Boolean publicos,
      Boolean destacados,
      Integer pagina,
      Integer tamanio,
      String ordenarPor,
      String sentido) {
    if (tamanio == null || tamanio <= 0) tamanio = TAMANIO_PAGINA_DEFAULT;
    if (pagina == null || pagina < 0) pagina = 0;
    String ordenDefault = "descripcion";
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.ASC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable = PageRequest.of(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenDefault));
          break;
      }
    }
    BusquedaProductoCriteria criteria =
        BusquedaProductoCriteria.builder()
            .buscarPorCodigo((codigo != null && !codigo.isEmpty()))
            .codigo(codigo)
            .buscarPorDescripcion(descripcion != null && !descripcion.isEmpty())
            .descripcion(descripcion)
            .buscarPorRubro(idRubro != null)
            .idRubro(idRubro)
            .buscarPorProveedor(idProveedor != null)
            .idProveedor(idProveedor)
            .idEmpresa(idEmpresa)
            .listarSoloFaltantes(soloFantantes)
            .listarSoloEnStock(soloEnStock)
            .buscaPorVisibilidad(publicos != null)
            .publico(publicos)
            .buscaPorDestacado(destacados != null)
            .destacado(destacados)
            .pageable(pageable)
            .build();
    return productoService.buscarProductos(criteria);
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
    List<CantidadEnSucursal> cantidadEnSucursales = new ArrayList<>();
    productoDTO
        .getCantidadSucursales()
        .forEach(
            cantidadEnSucursalDTO -> {
              cantidadEnSucursales.add(
                  modelMapper.map(cantidadEnSucursalDTO, CantidadEnSucursal.class));
              cantidadEnSucursales
                  .get(cantidadEnSucursales.size() - 1)
                  .setEmpresa(
                      empresaService.getEmpresaPorId(cantidadEnSucursalDTO.getIdSucursal()));
            });
    productoPorActualizar.setCantidadSucursales(cantidadEnSucursales);
    productoService.actualizar(productoPorActualizar, productoPersistido);
  }

  @PostMapping("/productos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Producto guardar(
    @RequestBody NuevoProductoDTO nuevoProductoDTO,
    @RequestParam Long idMedida,
    @RequestParam Long idRubro,
    @RequestParam Long idProveedor,
    @RequestParam Long idSucursal) {
    Producto producto = new Producto();
    producto.setMedida(medidaService.getMedidaNoEliminadaPorId(idMedida));
    producto.setRubro(rubroService.getRubroNoEliminadoPorId(idRubro));
    producto.setProveedor(proveedorService.getProveedorNoEliminadoPorId(idProveedor));
    producto.setCodigo(nuevoProductoDTO.getCodigo());
    producto.setDescripcion(nuevoProductoDTO.getDescripcion());

    List<CantidadEnSucursal> cantidadEnSucursal = new ArrayList<>();
    CantidadEnSucursal cantidad = new CantidadEnSucursal();
    cantidad.setCantidad(nuevoProductoDTO.getCantidad());
    cantidad.setEmpresa(empresaService.getEmpresaPorId(idSucursal));
    cantidad.setEstante(nuevoProductoDTO.getEstante());
    cantidad.setEstanteria(nuevoProductoDTO.getEstanteria());
    cantidadEnSucursal.add(cantidad);
    producto.setCantidadSucursales(cantidadEnSucursal);

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

  @GetMapping("/productos/valor-stock/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularValorStock(
      @RequestParam long idEmpresa,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) boolean soloFantantes,
      @RequestParam(required = false) boolean soloEnStock,
      @RequestParam(required = false) Boolean publicos,
      @RequestParam(required = false) Boolean destacados) {
    BusquedaProductoCriteria criteria =
        BusquedaProductoCriteria.builder()
            .buscarPorCodigo((codigo != null))
            .codigo(codigo)
            .buscarPorDescripcion(descripcion != null)
            .descripcion(descripcion)
            .buscarPorRubro(idRubro != null)
            .idRubro(idRubro)
            .buscarPorProveedor(idProveedor != null)
            .idProveedor(idProveedor)
            .idEmpresa(idEmpresa)
            .listarSoloFaltantes(soloFantantes)
            .listarSoloEnStock(soloEnStock)
            .buscaPorVisibilidad(publicos != null)
            .publico(publicos)
            .buscaPorDestacado(destacados != null)
            .destacado(destacados)
            .build();
    return productoService.calcularValorStock(criteria);
  }

  @GetMapping("/productos/disponibilidad-stock/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Map<Long, BigDecimal> verificarDisponibilidadStock(
      long[] idProducto, BigDecimal[] cantidad, @PathVariable Long idSucursal) {
    return productoService.getProductosSinStockDisponible(idSucursal, idProducto, cantidad);
  }

  @GetMapping("/productos/reporte/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public ResponseEntity<byte[]> getListaDePrecios(
      @RequestParam long idEmpresa,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) boolean soloFantantes,
      @RequestParam(required = false) boolean soloEnStock,
      @RequestParam(required = false) Boolean publicos,
      @RequestParam(required = false) Boolean destacados,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestParam(required = false) String formato) {
    HttpHeaders headers = new HttpHeaders();
    List<Producto> productos;
    switch (formato) {
      case "xlsx":
        headers.setContentType(new MediaType("application", "vnd.ms-excel"));
        headers.set("Content-Disposition", "attachment; filename=ListaPrecios.xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        productos =
            this.buscar(
                    idEmpresa,
                    codigo,
                    descripcion,
                    idRubro,
                    idProveedor,
                    soloFantantes,
                    soloEnStock,
                    publicos,
                    destacados,
                    0,
                    Integer.MAX_VALUE,
                    ordenarPor,
                    sentido)
                .getContent();
        byte[] reporteXls =
            productoService.getListaDePreciosPorEmpresa(productos, idEmpresa, formato);
        headers.setContentLength(reporteXls.length);
        return new ResponseEntity<>(reporteXls, headers, HttpStatus.OK);
      case "pdf":
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("content-disposition", "inline; filename=ListaPrecios.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        productos =
            this.buscar(
                    idEmpresa,
                    codigo,
                    descripcion,
                    idRubro,
                    idProveedor,
                    soloFantantes,
                    soloEnStock,
                    publicos,
                    destacados,
                    0,
                    Integer.MAX_VALUE,
                    ordenarPor,
                    sentido)
                .getContent();
        byte[] reportePDF =
            productoService.getListaDePreciosPorEmpresa(productos, idEmpresa, formato);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
      default:
        throw new BusinessServiceException(messageSource.getMessage(
          "mensaje_formato_no_valido", null, Locale.getDefault()));
    }
  }
}
