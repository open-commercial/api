package sic.controller;

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
import sic.modelo.BusquedaProductoCriteria;
import sic.modelo.Empresa;
import sic.modelo.Medida;
import sic.modelo.Producto;
import sic.modelo.Proveedor;
import sic.modelo.Rubro;
import sic.service.IEmpresaService;
import sic.service.IMedidaService;
import sic.service.IProductoService;
import sic.service.IProveedorService;
import sic.service.IRubroService;

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
    public Producto getProductoPorId(@PathVariable long idProducto) {
        return productoService.getProductoPorId(idProducto);
    }
    
    @GetMapping("/productos/busqueda")
    @ResponseStatus(HttpStatus.OK)
    public Producto getProductoPorCodigo(@RequestParam long idEmpresa,
                                         @RequestParam String codigo) {
        return productoService.getProductoPorCodigo(codigo, empresaService.getEmpresaPorId(idEmpresa));
    }
    
    @GetMapping("/productos/valor-stock/criteria")
    @ResponseStatus(HttpStatus.OK)
    public double calcularValorStock(@RequestParam long idEmpresa,
                                     @RequestParam(required = false) String codigo,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam(required = false) Long idRubro,
                                     @RequestParam(required = false) Long idProveedor,                                          
                                     @RequestParam(required = false) Integer cantidadRegistros,
                                     @RequestParam(required = false) boolean soloFantantes) {
        Rubro rubro = null;
        if (idRubro != null) {
            rubro = rubroService.getRubroPorId(idRubro);
        }
        Proveedor proveedor = null;
        if (idProveedor != null) {
            proveedor = proveedorService.getProveedorPorId(idProveedor);
        }
        if (cantidadRegistros == null) {
            cantidadRegistros = 0;
        }
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
                                            .build();
        return productoService.calcularValorStock(criteria);
    }
    
    @GetMapping("/productos/busqueda/criteria") 
    @ResponseStatus(HttpStatus.OK)
    public Page<Producto> buscarProductos(@RequestParam long idEmpresa,
                                          @RequestParam(required = false) String codigo,
                                          @RequestParam(required = false) String descripcion,
                                          @RequestParam(required = false) Long idRubro,
                                          @RequestParam(required = false) Long idProveedor,
                                          @RequestParam(required = false) boolean soloFantantes,
                                          @RequestParam(required = false) Integer pagina,
                                          @RequestParam(required = false) Integer tamanio) {
        Rubro rubro = null;
        if (idRubro != null) {
            rubro = rubroService.getRubroPorId(idRubro);
        }
        Proveedor proveedor = null;
        if (idProveedor != null) {
            proveedor = proveedorService.getProveedorPorId(idProveedor);
        }
        if (tamanio == null || tamanio <= 0) {
            tamanio = TAMANIO_PAGINA_DEFAULT;
        }
        if (pagina == null || pagina < 0) {
            pagina = 0;
        }
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
                                            .pageable(pageable)
                                            .build();
        return productoService.buscarProductos(criteria);
    }
        
    @DeleteMapping("/productos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMultiplesProductos(@RequestParam long[] idProducto) {        
        productoService.eliminarMultiplesProductos(idProducto);
    }
    
    @PutMapping("/productos")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Producto producto,
                           @RequestParam long idMedida,
                           @RequestParam long idRubro, 
                           @RequestParam long idProveedor,
                           @RequestParam long idEmpresa) {
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
    public Producto guardar(@RequestBody Producto producto,
                            @RequestParam long idMedida,
                            @RequestParam long idRubro, 
                            @RequestParam long idProveedor,
                            @RequestParam long idEmpresa) {
        producto.setMedida(medidaService.getMedidaPorId(idMedida));
        producto.setRubro(rubroService.getRubroPorId(idRubro));
        producto.setProveedor(proveedorService.getProveedorPorId(idProveedor));
        producto.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
        return productoService.guardar(producto);
    }
    
    @GetMapping("/productos/{idProducto}/stock/disponibilidad")
    @ResponseStatus(HttpStatus.OK)
    public boolean existeStockDisponible(@PathVariable long idProducto,
                                         @RequestParam double cantidad) {
        return productoService.existeStockDisponible(idProducto, cantidad);
    }
    
    @GetMapping("/productos/ganancia-neto")
    @ResponseStatus(HttpStatus.OK)
    public double calcularGanancia_Neto(@RequestParam double precioCosto, 
                                        @RequestParam double gananciaPorcentaje) {
        return productoService.calcularGanancia_Neto(precioCosto, gananciaPorcentaje);
    }    
    
    @GetMapping("/productos/ganancia-porcentaje")
    @ResponseStatus(HttpStatus.OK)
    public double calcularGanancia_Porcentaje(@RequestParam(required = false) boolean ascendente,
                                              @RequestParam double precioCosto,
                                              @RequestParam double pvp, 
                                              @RequestParam(required = false) Double ivaPorcentaje, 
                                              @RequestParam(defaultValue = "0", required = false) Double impInternoPorcentaje,                                              
                                              @RequestParam(required = false) Double precioDeLista, 
                                              @RequestParam(required = false) Double precioDeListaAnterior){
        return productoService.calcularGanancia_Porcentaje(precioDeLista, precioDeListaAnterior,
                                                           pvp, ivaPorcentaje, impInternoPorcentaje, 
                                                           precioCosto, ascendente);
    }
    
    @GetMapping("/productos/iva-neto")
    @ResponseStatus(HttpStatus.OK)
    public double calcularIVA_Neto(@RequestParam double pvp, 
                                   @RequestParam double ivaPorcentaje) {
        return productoService.calcularIVA_Neto(pvp, ivaPorcentaje);
    }
    
    @GetMapping("/productos/imp-interno-neto")
    @ResponseStatus(HttpStatus.OK)
    public double calcularImpInterno_Neto(@RequestParam double pvp, 
                                          @RequestParam(defaultValue = "0",required = false) double impInternoPorcentaje){
        return productoService.calcularImpInterno_Neto(pvp, impInternoPorcentaje);
    }
    
    @GetMapping("/productos/pvp")
    @ResponseStatus(HttpStatus.OK)
    public double calcularPVP(@RequestParam double precioCosto, 
                              @RequestParam double gananciaPorcentaje) {
        return productoService.calcularPVP(precioCosto, gananciaPorcentaje);
    }
    
    @GetMapping("/productos/precio-lista")
    @ResponseStatus(HttpStatus.OK)
    public double calcularPrecioLista(@RequestParam double pvp, 
                                      @RequestParam double ivaPorcentaje, 
                                      @RequestParam(defaultValue = "0",required = false) double impInternoPorcentaje) {
        return productoService.calcularPrecioLista(pvp, ivaPorcentaje, impInternoPorcentaje);
    }

    @GetMapping("/productos/reporte/criteria")
    public ResponseEntity<byte[]> getReporteListaDePrecios(@RequestParam(value = "idEmpresa") long idEmpresa,
                                                           @RequestParam(value = "codigo", required = false) String codigo,
                                                           @RequestParam(value = "descripcion", required = false) String descripcion,
                                                           @RequestParam(value = "idRubro", required = false) Long idRubro,
                                                           @RequestParam(value = "idProveedor", required = false) Long idProveedor,                                                           
                                                           @RequestParam(value = "soloFaltantes", required = false) boolean soloFantantes) {
        Rubro rubro = null;
        if (idRubro != null) {
            rubro = rubroService.getRubroPorId(idRubro);
        }
        Proveedor proveedor = null;
        if (idProveedor != null) {
            proveedor = proveedorService.getProveedorPorId(idProveedor);
        }
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
        headers.setContentType(MediaType.APPLICATION_PDF);        
        headers.add("content-disposition", "inline; filename=ListaPrecios.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = productoService.getReporteListaDePreciosPorEmpresa(productoService.buscarProductos(criteria).getContent(), empresa);
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }
    
    @PutMapping("/productos/multiples")
    @ResponseStatus(HttpStatus.OK)
    public void modificarMultiplesProductos(@RequestParam long[] idProducto,
                                            @RequestParam(required = false) Long idMedida,
                                            @RequestParam(required = false) Long idRubro,
                                            @RequestParam(required = false) Long idProveedor,
                                            @RequestParam(required = false) Double gananciaNeto,
                                            @RequestParam(required = false) Double gananciaPorcentaje,
                                            @RequestParam(defaultValue = "0",required = false) Double impuestoInternoNeto,
                                            @RequestParam(defaultValue = "0",required = false) Double impuestoInternoPorcentaje,
                                            @RequestParam(required = false) Double IVANeto,
                                            @RequestParam(required = false) Double IVAPorcentaje,
                                            @RequestParam(required = false) Double precioCosto,
                                            @RequestParam(required = false) Double precioLista,
                                            @RequestParam(required = false) Double precioVentaPublico) {
        
        boolean actualizaPrecios = false;
        if (gananciaNeto != null && gananciaPorcentaje != null && impuestoInternoNeto != null && impuestoInternoPorcentaje != null
                && IVANeto != null && IVAPorcentaje != null && precioCosto != null && precioLista != null && precioVentaPublico != null) {
            actualizaPrecios = true;
        }        
        Medida medida = null;
        if (idMedida != null) {
            medida = medidaService.getMedidaPorId(idMedida);
        }
        Rubro rubro = null;
        if (idRubro != null) {
            rubro = rubroService.getRubroPorId(idRubro);
        }
        Proveedor proveedor = null;
        if (idProveedor != null) {
            proveedor = proveedorService.getProveedorPorId(idProveedor);
        }        
        productoService.modificarMultiplesProductos(idProducto,
                actualizaPrecios,
                gananciaNeto,
                gananciaPorcentaje,
                impuestoInternoNeto,
                impuestoInternoPorcentaje,
                IVANeto,
                IVAPorcentaje,
                precioCosto,
                precioLista,
                precioVentaPublico,                                             
                (idMedida != null), medida,
                (idRubro != null), rubro,
                (idProveedor != null), proveedor);
    }
}
