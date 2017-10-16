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
import sic.modelo.Medida;
import sic.service.IEmpresaService;
import sic.service.IMedidaService;

@RestController
@RequestMapping("/api/v1")
public class MedidaController {
    
    private final IMedidaService medidaService;
    private final IEmpresaService empresaService;
    
    @Autowired
    public MedidaController(IMedidaService medidaService, IEmpresaService empresaService) {
        this.medidaService = medidaService;
        this.empresaService = empresaService;
    }
    
    @GetMapping("/medidas/{idMedida}")
    @ResponseStatus(HttpStatus.OK)
    public Medida getMedidaPorId(@PathVariable long idMedida) {
        return medidaService.getMedidaPorId(idMedida);
    }
    
    @PutMapping("/medidas")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Medida medida) {
        if (medidaService.getMedidaPorId(medida.getId_Medida()) != null) {
            medidaService.actualizar(medida);
        }
    }
    
    @DeleteMapping("/medidas/{idMedida}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idMedida) {
        medidaService.eliminar(idMedida);
    }
    
    @PostMapping("/medidas")
    @ResponseStatus(HttpStatus.OK)
    public Medida guardar(@RequestBody Medida medida) {
        return medidaService.guardar(medida);
    }
    
    @GetMapping("/medidas/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public List<Medida> getMedidas(@PathVariable long idEmpresa) {
        return medidaService.getUnidadMedidas(empresaService.getEmpresaPorId(idEmpresa));
    }
}
