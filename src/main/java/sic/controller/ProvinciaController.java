package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Provincia;
import sic.modelo.Rol;
import sic.modelo.dto.ProvinciaDTO;
import sic.service.IPaisService;
import sic.service.IProvinciaService;

@RestController
@RequestMapping("/api/v1")
public class ProvinciaController {

    private final IProvinciaService provinciaService;
    private final IPaisService paisService;
    private final ModelMapper modelMapper;

    @Autowired
    public ProvinciaController(IProvinciaService provinciaService, IPaisService paisService, ModelMapper modelMapper) {
        this.provinciaService = provinciaService;
        this.paisService = paisService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/provincias/{idProvincia}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Provincia getProvinciaPorId(@PathVariable long idProvincia) {
        return provinciaService.getProvinciaPorId(idProvincia);
    }
    
    @PutMapping("/provincias")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void actualizar(@RequestBody ProvinciaDTO provinciaDTO, @RequestParam(required = false) Long idPais) {
        Provincia provinciaPersistida = provinciaService.getProvinciaPorId(provinciaDTO.getId_Provincia());
        Provincia provinciaPorActualizar = modelMapper.map(provinciaDTO, Provincia.class);
        if (provinciaPorActualizar.getNombre() == null
          || provinciaPorActualizar.getNombre().isEmpty()) {
            provinciaPorActualizar.setNombre(provinciaPersistida.getNombre());
        }
        if (idPais != null) {
            provinciaPorActualizar.setPais(paisService.getPaisPorId(idPais));
        } else {
            provinciaPorActualizar.setPais(provinciaPersistida.getPais());
        }
        if (provinciaService.getProvinciaPorId(provinciaPorActualizar.getId_Provincia()) != null) {
            provinciaService.actualizar(provinciaPorActualizar);
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
    public Provincia guardar(@RequestBody ProvinciaDTO provinciaDTO, @RequestParam Long idPais) {
        Provincia provincia = modelMapper.map(provinciaDTO, Provincia.class);
        provincia.setPais(paisService.getPaisPorId(idPais));
        return provinciaService.guardar(provincia);
    }
}
