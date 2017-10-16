package sic.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import sic.modelo.BusquedaProveedorCriteria;
import sic.modelo.Localidad;
import sic.modelo.Pais;
import sic.modelo.Proveedor;
import sic.modelo.Provincia;
import sic.service.IEmpresaService;
import sic.service.ILocalidadService;
import sic.service.IPaisService;
import sic.service.IProveedorService;
import sic.service.IProvinciaService;

@RestController
@RequestMapping("/api/v1")
public class ProveedorController {
    
    private final IProveedorService proveedorService;
    private final IPaisService paisService;
    private final IProvinciaService provinciaService;
    private final ILocalidadService localidadService;
    private final IEmpresaService empresaService;
    
    @Autowired
    public ProveedorController(IProveedorService proveedorService, IPaisService paisService,
            IProvinciaService provinciaService, ILocalidadService localidadService, IEmpresaService empresaService) {
        this.proveedorService = proveedorService;
        this.paisService = paisService;
        this.provinciaService = provinciaService;
        this.localidadService = localidadService;
        this.empresaService = empresaService;
    }
    
    @GetMapping("/proveedores/{idProveedor}")
    @ResponseStatus(HttpStatus.OK)
    public Proveedor getProveedorPorId(@PathVariable long idProveedor) {
        return this.proveedorService.getProveedorPorId(idProveedor);
    }
    
    @PostMapping("/proveedores")
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor guardar(@RequestBody Proveedor proveedor) {
        return proveedorService.guardar(proveedor);
    }    
    
    @PutMapping("/proveedores")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Proveedor proveedor) {
        if (proveedorService.getProveedorPorId(proveedor.getId_Proveedor()) != null) {
            proveedorService.actualizar(proveedor);
        }        
    }
    
    @GetMapping("/proveedores/busqueda/criteria")
    @ResponseStatus(HttpStatus.OK)
    public List<Proveedor> buscarProveedores(@RequestParam(value = "codigo", required = false) String codigo,
                                             @RequestParam(value = "razonSocial", required = false) String razonSocial,
                                             @RequestParam(value = "idFiscal", required = false) String idFiscal,
                                             @RequestParam(value = "idPais", required = false) Long idPais,
                                             @RequestParam(value = "idProvincia", required = false) Long idProvincia,
                                             @RequestParam(value = "idLocalidad", required = false) Long idLocalidad,
                                             @RequestParam(value = "idEmpresa") long idEmpresa) {
        Pais pais = null;
        if (idPais != null) {
            pais = paisService.getPaisPorId(idPais);
        }
        Provincia provincia = null;
        if (idProvincia != null) {
            provincia = provinciaService.getProvinciaPorId(idProvincia);
        }       
        Localidad localidad = null;
        if (idLocalidad != null) {
            localidad = localidadService.getLocalidadPorId(idLocalidad);
        }
        BusquedaProveedorCriteria criteria = new BusquedaProveedorCriteria(
                                                 (codigo != null), codigo,
                                                 (razonSocial != null), razonSocial,
                                                 (idFiscal != null), idFiscal,
                                                 (idPais != null), pais,
                                                 (idProvincia != null), provincia,
                                                 (idLocalidad != null), localidad,
                                                  empresaService.getEmpresaPorId(idEmpresa), 0);
        return proveedorService.buscarProveedores(criteria);
    }
    
    @DeleteMapping("/proveedores/{idProveedor}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idProveedor) {
        proveedorService.eliminar(idProveedor);
    }
    
    @GetMapping("/proveedores/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public List<Proveedor> getProveedores(@PathVariable long idEmpresa) {
        return proveedorService.getProveedores(empresaService.getEmpresaPorId(idEmpresa));
    }
    
}
