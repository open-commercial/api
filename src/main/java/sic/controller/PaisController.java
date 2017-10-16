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
import sic.modelo.Pais;
import sic.service.IPaisService;


@RestController
@RequestMapping("/api/v1")
public class PaisController {
    
    private final IPaisService paisService;
    
    @Autowired
    public PaisController(IPaisService paisService) {
        this.paisService = paisService;
    }
    
    @GetMapping("/paises/{idPais}")
    @ResponseStatus(HttpStatus.OK)
    public Pais getPaisPorId(@PathVariable long idPais) {
        return paisService.getPaisPorId(idPais);
    }
    
    @PutMapping("/paises")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Pais pais) {
        if (paisService.getPaisPorId(pais.getId_Pais()) != null) {
            paisService.actualizar(pais);
        }
    }
    
    @DeleteMapping("/paises/{idPais}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idPais) {
        paisService.eliminar(idPais);
    }
    
    @PostMapping("/paises")
    @ResponseStatus(HttpStatus.CREATED)
    public Pais guardar(@RequestBody Pais pais) {
        return paisService.guardar(pais);
    }
    
    @GetMapping("/paises")
    @ResponseStatus(HttpStatus.OK)
    public List<Pais> getPaises() {
        return paisService.getPaises();
    }
    
}
