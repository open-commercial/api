package sic.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Provincia;
import sic.modelo.Rol;
import sic.service.IPaisService;
import sic.service.IProvinciaService;

@RestController
@RequestMapping("/api/v1")
public class ProvinciaController {
    
    private final IProvinciaService provinciaService;
    private final IPaisService paisService;
    
    @Autowired
    public ProvinciaController(IProvinciaService provinciaService, IPaisService paisService) {  
            this.provinciaService = provinciaService;
            this.paisService = paisService;
    }
    
    @GetMapping("/provincias/{idProvincia}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Provincia getProvinciaPorId(@PathVariable long idProvincia) {
        return provinciaService.getProvinciaPorId(idProvincia);
    }
    
    @PutMapping("/provincias")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void actualizar(@RequestBody Provincia provincia) { 
        if (provinciaService.getProvinciaPorId(provincia.getId_Provincia()) != null) {
            provinciaService.actualizar(provincia);
        }
    }
    
    @DeleteMapping("/provincias/{idProvincia}")
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public void eliminar(@PathVariable long idProvincia) {
        provinciaService.eliminar(idProvincia);        
    }
    
    @GetMapping("/provincias/paises/{idPais}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<Provincia> getProvinciasDelPais(@PathVariable long idPais) {
        return provinciaService.getProvinciasDelPais(paisService.getPaisPorId(idPais));
    }
    
    @PostMapping("/provincias")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Provincia guardar(@RequestBody Provincia provincia) {
        return provinciaService.guardar(provincia);
    }
}
