package sic.controller;

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
import sic.modelo.Gasto;
import sic.modelo.Rol;
import sic.service.IGastoService;

@RestController
@RequestMapping("/api/v1")
public class GastoController {
    
    private final IGastoService gastoService;    
    
    @Autowired
    public GastoController(IGastoService gastoService) {
        this.gastoService = gastoService;        
    }
    
    @GetMapping("/gastos/{idGasto}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Gasto getGastoPorId(@PathVariable long idGasto) {
        return gastoService.getGastoPorId(idGasto);
    }
    
    @PutMapping("/gastos")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void actualizar(@RequestBody Gasto gasto) {
        if (gastoService.getGastoPorId(gasto.getId_Gasto()) != null) {
            gastoService.actualizar(gasto);
        }
    }
    
    @DeleteMapping("/gastos/{idGasto}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void eliminar(@PathVariable long idGasto) {
        gastoService.eliminar(idGasto);
    }

    @PostMapping("/gastos")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Gasto guardar(@RequestBody Gasto gasto) {
        return gastoService.guardar(gasto);
    }

}
