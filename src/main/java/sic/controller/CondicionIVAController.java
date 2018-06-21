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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.CondicionIVA;
import sic.modelo.Rol;
import sic.service.ICondicionIVAService;

@RestController
@RequestMapping("/api/v1")
public class CondicionIVAController {
    
    private final ICondicionIVAService condicionIVAService;
    
    @Autowired
    public CondicionIVAController(ICondicionIVAService condicionIVAService) {
        this.condicionIVAService = condicionIVAService;
    }

    @GetMapping("/condiciones-iva")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<CondicionIVA> getCondicionesIVA() {
        return condicionIVAService.getCondicionesIVA();
    }
    
    @PutMapping("/condiciones-iva")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void actualizar(@RequestBody CondicionIVA condicionIVA) {
        if(condicionIVAService.getCondicionIVAPorId(condicionIVA.getId_CondicionIVA()) != null) {
            condicionIVAService.actualizar(condicionIVA);
        }
    }
    
    @DeleteMapping("/condiciones-iva/{idCondicionIva}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void eliminar(@PathVariable long idCondicionIva) {
        condicionIVAService.eliminar(idCondicionIva);
    }
    
    @PostMapping("/condiciones-iva")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public CondicionIVA guardar(@RequestBody CondicionIVA condicionIVA) {
        return condicionIVAService.guardar(condicionIVA);
    }
    
}
