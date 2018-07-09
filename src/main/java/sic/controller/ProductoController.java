package sic.controller;

import java.math.BigDecimal;
import java.util.Map;
import java.util.ResourceBundle;
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
    private final IEmpresaService empresaService;
    private final IRubroService rubroService;
    private final IProveedorService proveedorService;
    private final IMedidaService medidaService;
    private final int TAMANIO_PAGINA_DEFAULT = 50;

    @Autowired
    public ProductoController(IProductoService productoService, IEmpresaService empresaService,
                              IRubroService rubroService, IProveedorService proveedorService,
                              IMedidaService medidaService) {
        this.productoService = productoService;
        this.empresaService = empresaService;
        this.rubroService = rubroService;
        this.proveedorService = proveedorService;
        this.medidaService = medidaService;
    }

    @GetMapping("/productos/{idProducto}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Producto getProductoPorId(@PathVariable long idProducto) {
        return productoService.getProductoPorId(idProducto);
    }

    @GetMapping("/productos/busqueda")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Producto getProductoPorCodigo(@RequestParam long idEmpresa,
                                         @RequestParam String codigo) {
        return productoService.getProductoPorCodigo(codigo, empresaService.getEmpresaPorId(idEmpresa));
    }

    @GetMapping("/productos/valor-stock/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public BigDecimal calcularValorStock(@RequestParam long idEmpresa,
                                         @RequestParam(required = false) String codigo,
                                         @RequestParam(required = false) String descripcion,
                                         @RequestParam(required = false) Long idRubro,
                                         @RequestParam(required = false) Long idProveedor,
                                         @RequestParam(required = false) Integer cantidadRegistros,
                                         @RequestParam(required = false) boolean soloFantantes,
                                         @RequestParam(required = false) Boolean visibilidad) {
        Rubro rubro = null;
        if (idRubro != null) rubro = rubroService.getRubroPorId(idRubro);
        Proveedor proveedor = null;
        if (idProveedor != null) proveedor = proveedorService.getProveedorPorId(idProveedor);
        if (cantidadRegistros == null) cantidadRegistros = 0;
        BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder()
                .buscarPorCodigo((codigo!=null))
                .codigo(codigo)
                .buscarPorDescripcion(descripcion!=null)
                .descripcion(descripcion)
                .buscarPorRubro(rubro!=null)
                .rubro(rubro)
                .buscarPorProveedor(proveedor!=null)
                .proveedor(proveedor)
                .empresa(empresaService.getEmpresaPorId(idEmpresa))
                .cantRegistros(cantidadRegistros)
                .listarSoloFaltantes(soloFantantes)
                .buscaPorVisibilidad(visibilidad!=null)
                .publicoOPrivado(visibilidad)
                .build();
        return productoService.calcularValorStock(criteria);
    }

    @GetMapping("/productos/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Page<Producto> buscarProductos(@RequestParam long idEmpresa,
                                          @RequestParam(required = false) String codigo,
                                          @RequestParam(required = false) String descripcion,
                                          @RequestParam(required = false) Long idRubro,
                                          @RequestParam(required = false) Long idProveedor,
                                          @RequestParam(required = false) boolean soloFantantes,
                                          @RequestParam(required = false) Boolean publicos,
                                          @RequestParam(required = false) Integer pagina,
                                          @RequestParam(required = false) Integer tamanio) {
        Rubro rubro = null;
        if (idRubro != null) rubro = rubroService.getRubroPorId(idRubro);
        Proveedor proveedor = null;
        if (idProveedor != null) proveedor = proveedorService.getProveedorPorId(idProveedor);
        if (tamanio == null || tamanio <= 0) tamanio = TAMANIO_PAGINA_DEFAULT;
        if (pagina == null || pagina < 0) pagina = 0;
        Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, "descripcion"));
        BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder()
                .buscarPorCodigo((codigo!=null && !codigo.isEmpty()))
                .codigo(codigo)
                .buscarPorDescripcion(descripcion!=null && !descripcion.isEmpty())
                .descripcion(descripcion)
                .buscarPorRubro(rubro!=null)
                .rubro(rubro)
                .buscarPorProveedor(proveedor!=null)
                .proveedor(proveedor)
                .empresa(empresaService.getEmpresaPorId(idEmpresa))
                .listarSoloFaltantes(soloFantantes)
                .buscaPorVisibilidad(publicos!=null)
                .publicoOPrivado(publicos)
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
    public void actualizar(@RequestBody Producto producto,
                           @RequestParam Long idMedida,
                           @RequestParam Long idRubro,
                           @RequestParam Long idProveedor,
                           @RequestParam Long idEmpresa) {
        if (productoService.getProductoPorId(producto.getId_Producto()) != null) {
            producto.setMedida(medidaService.getMedidaPorId(idMedida));
            producto.setRubro(rubroService.getRubroPorId(idRubro));
            producto.setProveedor(proveedorService.getProveedorPorId(idProveedor));
            producto.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
            productoService.actualizar(producto);
        }
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Producto guardar(@RequestBody Producto producto,
                            @RequestParam Long idMedida,
                            @RequestParam Long idRubro,
                            @RequestParam Long idProveedor,
                            @RequestParam Long idEmpresa) {
        producto.setMedida(medidaService.getMedidaPorId(idMedida));
        producto.setRubro(rubroService.getRubroPorId(idRubro));
        producto.setProveedor(proveedorService.getProveedorPorId(idProveedor));
        producto.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
        return productoService.guardar(producto);
    }

    @PutMapping("/productos/multiples")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void actualizarMultiplesProductos(@RequestParam long[] idProducto,
                                             @RequestParam(required = false) Long idMedida,
                                             @RequestParam(required = false) Long idRubro,
                                             @RequestParam(required = false) Long idProveedor,
                                             @RequestParam(required = false) BigDecimal gananciaNeto,
                                             @RequestParam(required = false) BigDecimal gananciaPorcentaje,
                                             @RequestParam(defaultValue = "0",required = false) BigDecimal impuestoInternoNeto,
                                             @RequestParam(defaultValue = "0",required = false) BigDecimal impuestoInternoPorcentaje,
                                             @RequestParam(required = false) BigDecimal IVANeto,
                                             @RequestParam(required = false) BigDecimal IVAPorcentaje,
                                             @RequestParam(required = false) BigDecimal precioCosto,
                                             @RequestParam(required = false) BigDecimal precioLista,
                                             @RequestParam(required = false) BigDecimal precioVentaPublico) {
        boolean actualizaPrecios = false;
        if (gananciaNeto != null && gananciaPorcentaje != null && impuestoInternoNeto != null && impuestoInternoPorcentaje != null
                && IVANeto != null && IVAPorcentaje != null && precioCosto != null && precioLista != null && precioVentaPublico != null) {
            actualizaPrecios = true;
        }
        Medida medida = null;
        if (idMedida != null) medida = medidaService.getMedidaPorId(idMedida);
        Rubro rubro = null;
        if (idRubro != null) rubro = rubroService.getRubroPorId(idRubro);
        Proveedor proveedor = null;
        if (idProveedor != null) proveedor = proveedorService.getProveedorPorId(idProveedor);
        productoService.actualizarMultiples(idProducto, actualizaPrecios, gananciaNeto, gananciaPorcentaje,
                impuestoInternoNeto, impuestoInternoPorcentaje, IVANeto, IVAPorcentaje,
                precioCosto, precioLista, precioVentaPublico, (idMedida != null), medida,
                (idRubro != null), rubro, (idProveedor != null), proveedor);
    }

    @GetMapping("/productos/disponibilidad-stock")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Map<Long, BigDecimal> verificarDisponibilidadStock(long[] idProducto, BigDecimal[] cantidad) {
        return productoService.getProductosSinStockDisponible(idProducto, cantidad);
    }

    @GetMapping("/productos/cantidad-venta-minima")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    @ResponseStatus(HttpStatus.OK)
    public Map<Long, BigDecimal> verificarCantidadVentaMinima(long[] idProducto, BigDecimal[] cantidad) {
        return productoService.getProductosNoCumplenCantidadVentaMinima(idProducto, cantidad);
    }

    @GetMapping("/productos/reporte/criteria")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public ResponseEntity<byte[]> getListaDePrecios(@RequestParam(value = "idEmpresa") long idEmpresa,
                                                    @RequestParam(value = "codigo", required = false) String codigo,
                                                    @RequestParam(value = "descripcion", required = false) String descripcion,
                                                    @RequestParam(value = "idRubro", required = false) Long idRubro,
                                                    @RequestParam(value = "idProveedor", required = false) Long idProveedor,
                                                    @RequestParam(value = "soloFaltantes", required = false) boolean soloFantantes,
                                                    @RequestParam(required = false) String formato) {
        Rubro rubro = null;
        if (idRubro != null) rubro = rubroService.getRubroPorId(idRubro);
        Proveedor proveedor = null;
        if (idProveedor != null) proveedor = proveedorService.getProveedorPorId(idProveedor);
        Empresa empresa = empresaService.getEmpresaPorId(idEmpresa);
        BusquedaProductoCriteria criteria = BusquedaProductoCriteria.builder()
                .buscarPorCodigo((codigo != null))
                .codigo(codigo)
                .buscarPorDescripcion(descripcion != null)
                .descripcion(descripcion)
                .buscarPorRubro(idRubro != null)
                .rubro(rubro)
                .buscarPorProveedor(proveedor != null)
                .proveedor(proveedor)
                .empresa(empresa)
                .cantRegistros(0)
                .listarSoloFaltantes(soloFantantes)
                .pageable(null)
                .build();
        HttpHeaders headers = new HttpHeaders();
        switch (formato) {
            case "xlsx":
                headers.setContentType(new MediaType("application", "vnd.ms-excel"));
                headers.set("Content-Disposition", "attachment; filename=ListaPrecios.xlsx");
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
                byte[] reporteXls = productoService.getListaDePreciosPorEmpresa(productoService.buscarProductos(criteria).getContent(), empresa, formato);
                headers.setContentLength(reporteXls.length);
                return new ResponseEntity<>(reporteXls, headers, HttpStatus.OK);
            case "pdf":
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.add("content-disposition", "inline; filename=ListaPrecios.pdf");
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
                byte[] reportePDF = productoService.getListaDePreciosPorEmpresa(productoService.buscarProductos(criteria).getContent(), empresa, formato);
                return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
            default:
                throw new BusinessServiceException(ResourceBundle.getBundle("Mensajes")
                        .getString("mensaje_formato_no_valido"));
        }

    }

}
