package sic.controller;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.NuevoProductoDTO;
import sic.modelo.dto.ProductoDTO;
import sic.service.*;

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

  @Autowired
  public ProductoController(
      IProductoService productoService,
      IMedidaService medidaService,
      IRubroService rubroService,
      IProveedorService proveedorService,
      IEmpresaService empresaService,
      IClienteService clienteService,
      IAuthService authService,
      ModelMapper modelMapper) {
    this.productoService = productoService;
    this.medidaService = medidaService;
    this.rubroService = rubroService;
    this.proveedorService = proveedorService;
    this.empresaService = empresaService;
    this.clienteService = clienteService;
    this.authService = authService;
    this.modelMapper = modelMapper;
  }

  @JsonView(Views.Public.class)
  @GetMapping("/public/productos/{idProducto}")
  public Producto getProductoPorIdPublic(
    @PathVariable long idProducto,
    @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Producto producto = productoService.getProductoPorId(idProducto);
    if (authorizationHeader != null) {
      authService.validarToken(authorizationHeader);
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

  @JsonView(Views.Public.class)
  @GetMapping("/public/productos/busqueda/criteria")
  public Page<Producto> buscarProductosPublic(
      @RequestParam long idEmpresa,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Boolean destacados,
      @RequestParam(required = false) Integer pagina,
      @RequestHeader(required = false, name = "Authorization") String authorizationHeader) {
    Page<Producto> productos =
      this.buscar(
        idEmpresa,
        codigo,
        descripcion,
        null,
        null,
        false,
        true,
        destacados,
        pagina,
        null,
        null,
        null);
    if (authorizationHeader != null) {
      authService.validarToken(authorizationHeader);
      Claims claims = authService.getClaimsDelToken(authorizationHeader);
      Cliente cliente =
        clienteService.getClientePorIdUsuarioYidEmpresa((int) claims.get("idUsuario"), idEmpresa);
      if (cliente != null) {
        return productoService.getProductosConPrecioBonificado(productos, cliente);
      } else {
        return productos;
      }
    } else {
      return productos;
    }
  }

  @GetMapping("/productos/{idProducto}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Producto getProductoPorId(
      @PathVariable long idProducto,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Producto producto = productoService.getProductoPorId(idProducto);
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
      @RequestParam long idEmpresa,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) boolean soloFantantes,
      @RequestParam(required = false) Boolean publicos,
      @RequestParam(required = false) Boolean destacados,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Cliente cliente =
        clienteService.getClientePorIdUsuarioYidEmpresa((int) claims.get("idUsuario"), idEmpresa);
    Page<Producto> productos =
        this.buscar(
            idEmpresa,
            codigo,
            descripcion,
            idRubro,
            idProveedor,
            soloFantantes,
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
  public Producto getProductoPorCodigo(@RequestParam long idEmpresa, @RequestParam String codigo) {
    return productoService.getProductoPorCodigo(codigo, idEmpresa);
  }

  private Page<Producto> buscar(
      long idEmpresa,
      String codigo,
      String descripcion,
      Long idRubro,
      Long idProveedor,
      boolean soloFantantes,
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
      pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, ordenDefault));
    } else {
      switch (sentido) {
        case "ASC":
          pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.DESC, ordenDefault));
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
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Long idEmpresa) {
    Producto productoPorActualizar = modelMapper.map(productoDTO, Producto.class);
    Producto productoPersistido =
        productoService.getProductoPorId(productoPorActualizar.getIdProducto());
    if (productoPersistido != null) {
      if (idMedida != null) productoPorActualizar.setMedida(medidaService.getMedidaPorId(idMedida));
      else productoPorActualizar.setMedida(productoPersistido.getMedida());
      if (idRubro != null) productoPorActualizar.setRubro(rubroService.getRubroPorId(idRubro));
      else productoPorActualizar.setRubro(productoPersistido.getRubro());
      if (idProveedor != null) productoPorActualizar.setProveedor(proveedorService.getProveedorPorId(idProveedor));
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
    producto.setMedida(medidaService.getMedidaPorId(idMedida));
    producto.setRubro(rubroService.getRubroPorId(idRubro));
    producto.setProveedor(proveedorService.getProveedorPorId(idProveedor));
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
      @RequestParam long[] idProducto,
      @RequestParam(required = false) BigDecimal descuentoRecargoPorcentaje,
      @RequestParam(required = false) Long idMedida,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) BigDecimal gananciaNeto,
      @RequestParam(required = false) BigDecimal gananciaPorcentaje,
      @RequestParam(required = false) BigDecimal IVANeto,
      @RequestParam(required = false) BigDecimal IVAPorcentaje,
      @RequestParam(required = false) BigDecimal precioCosto,
      @RequestParam(required = false) BigDecimal precioLista,
      @RequestParam(required = false) BigDecimal precioVentaPublico,
      @RequestParam(required = false) Boolean publico) {
    boolean actualizaPrecios = false;
    if (gananciaNeto != null
        && gananciaPorcentaje != null
        && IVANeto != null
        && IVAPorcentaje != null
        && precioCosto != null
        && precioLista != null
        && precioVentaPublico != null) {
      actualizaPrecios = true;
    }
    boolean aplicaDescuentoRecargoPorcentaje = false;
    if (descuentoRecargoPorcentaje != null) aplicaDescuentoRecargoPorcentaje = true;
    if (aplicaDescuentoRecargoPorcentaje && actualizaPrecios) {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes")
              .getString("mensaje_modificar_producto_no_permitido"));
    }
    productoService.actualizarMultiples(
        idProducto,
        actualizaPrecios,
        aplicaDescuentoRecargoPorcentaje,
        descuentoRecargoPorcentaje,
        gananciaNeto,
        gananciaPorcentaje,
        IVANeto,
        IVAPorcentaje,
        precioCosto,
        precioLista,
        precioVentaPublico,
        (idMedida != null),
        idMedida,
        (idRubro != null),
        idRubro,
        (idProveedor != null),
        idProveedor,
        (publico != null),
        publico);
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
            .buscaPorVisibilidad(publicos != null)
            .publico(publicos)
            .buscaPorDestacado(destacados != null)
            .destacado(destacados)
            .build();
    return productoService.calcularValorStock(criteria);
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
        throw new BusinessServiceException(
            ResourceBundle.getBundle("Mensajes").getString("mensaje_formato_no_valido"));
    }
  }
}
