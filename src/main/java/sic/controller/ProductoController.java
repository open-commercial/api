package sic.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class ProductoController {

  private final IProductoService productoService;

  @Autowired
  public ProductoController(IProductoService productoService) {
    this.productoService = productoService;
  }

  @GetMapping("/productos/{idProducto}")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Producto getProductoPorId(@PathVariable long idProducto) {
    return productoService.getProductoPorId(idProducto);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/public/productos/{idProducto}")
  @ResponseStatus(HttpStatus.OK)
  public Producto getProductoPorIdPublic(@PathVariable long idProducto) {
    Producto producto = productoService.getProductoPorId(idProducto);
    if (producto.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
      producto.setHayStock(true);
    } else {
      producto.setHayStock(false);
    }
    return producto;
  }

  @GetMapping("/productos/busqueda")
  @ResponseStatus(HttpStatus.OK)
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

  @GetMapping("/productos/busqueda/criteria")
  @ResponseStatus(HttpStatus.OK)
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
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido) {
    return this.buscar(
        idEmpresa,
        codigo,
        descripcion,
        idRubro,
        idProveedor,
        soloFantantes,
        publicos,
        pagina,
        tamanio,
        ordenarPor,
        sentido);
  }

  @JsonView(Views.Public.class)
  @GetMapping("/public/productos/busqueda/criteria")
  @ResponseStatus(HttpStatus.OK)
  public Page<Producto> buscarProductosPublic(
      @RequestParam long idEmpresa,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) boolean soloFantantes,
      @RequestParam(required = false) Boolean publicos,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido) {
    return this.buscar(
        idEmpresa,
        codigo,
        descripcion,
        idRubro,
        idProveedor,
        soloFantantes,
        publicos,
        pagina,
        tamanio,
        ordenarPor,
        sentido);
  }

  private Page<Producto> buscar(
      long idEmpresa,
      String codigo,
      String descripcion,
      Long idRubro,
      Long idProveedor,
      boolean soloFantantes,
      Boolean publicos,
      Integer pagina,
      Integer tamanio,
      String ordenarPor,
      String sentido) {
    final int TAMANIO_PAGINA_DEFAULT = 50;
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
            .pageable(pageable)
            .build();
    return productoService.buscarProductos(criteria);
  }

  @DeleteMapping("/productos")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public void eliminarMultiplesProductos(@RequestParam long[] idProducto) {
    productoService.eliminarMultiplesProductos(idProducto);
  }

  @PutMapping("/productos")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(
      @RequestBody Producto producto,
      @RequestParam Long idMedida,
      @RequestParam Long idRubro,
      @RequestParam Long idProveedor,
      @RequestParam Long idEmpresa) {
    if (productoService.getProductoPorId(producto.getId_Producto()) != null) {
      productoService.actualizar(producto, idMedida, idRubro, idProveedor, idEmpresa);
    }
  }

  @PostMapping("/productos")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Producto guardar(
      @RequestBody Producto producto,
      @RequestParam Long idMedida,
      @RequestParam Long idRubro,
      @RequestParam Long idProveedor,
      @RequestParam Long idEmpresa) {
    return productoService.guardar(producto, idMedida, idRubro, idProveedor, idEmpresa);
  }

  @PutMapping("/productos/multiples")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizarMultiplesProductos(
      @RequestParam long[] idProducto,
      @RequestParam(required = false) BigDecimal descuentoRecargoPorcentaje,
      @RequestParam(required = false) Long idMedida,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) BigDecimal gananciaNeto,
      @RequestParam(required = false) BigDecimal gananciaPorcentaje,
      @RequestParam(defaultValue = "0", required = false) BigDecimal impuestoInternoNeto,
      @RequestParam(defaultValue = "0", required = false) BigDecimal impuestoInternoPorcentaje,
      @RequestParam(required = false) BigDecimal IVANeto,
      @RequestParam(required = false) BigDecimal IVAPorcentaje,
      @RequestParam(required = false) BigDecimal precioCosto,
      @RequestParam(required = false) BigDecimal precioLista,
      @RequestParam(required = false) BigDecimal precioVentaPublico,
      @RequestParam(required = false) Boolean publico) {
    boolean actualizaPrecios = false;
    if (gananciaNeto != null
        && gananciaPorcentaje != null
        && impuestoInternoNeto != null
        && impuestoInternoPorcentaje != null
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
        impuestoInternoNeto,
        impuestoInternoPorcentaje,
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
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularValorStock(
      @RequestParam long idEmpresa,
      @RequestParam(required = false) String codigo,
      @RequestParam(required = false) String descripcion,
      @RequestParam(required = false) Long idRubro,
      @RequestParam(required = false) Long idProveedor,
      @RequestParam(required = false) Integer cantidadRegistros,
      @RequestParam(required = false) boolean soloFantantes,
      @RequestParam(required = false) Boolean visibilidad) {
    if (cantidadRegistros == null) cantidadRegistros = 0;
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
            .cantRegistros(cantidadRegistros)
            .listarSoloFaltantes(soloFantantes)
            .buscaPorVisibilidad(visibilidad != null)
            .publico(visibilidad)
            .build();
    return productoService.calcularValorStock(criteria);
  }

  @GetMapping("/productos/disponibilidad-stock")
  @ResponseStatus(HttpStatus.OK)
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
