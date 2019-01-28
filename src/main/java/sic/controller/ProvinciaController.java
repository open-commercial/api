package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Provincia;
import sic.modelo.Rol;
import sic.modelo.dto.ProvinciaDTO;
import sic.service.IProvinciaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProvinciaController {

    private final IProvinciaService provinciaService;
    private final ModelMapper modelMapper;

    @Autowired
    public ProvinciaController(IProvinciaService provinciaService, ModelMapper modelMapper) {
        this.provinciaService = provinciaService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/provincias/{idProvincia}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Provincia getProvinciaPorId(@PathVariable long idProvincia) {
        return provinciaService.getProvinciaPorId(idProvincia);
    }

    @GetMapping("/provincias")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.COMPRADOR, Rol.VIAJANTE})
    public List<Provincia> getProvincias() {
        return provinciaService.getProvincias();
    }
    
    @PutMapping("/provincias")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void actualizar(@RequestBody ProvinciaDTO provinciaDTO) {
        Provincia provinciaPersistida = provinciaService.getProvinciaPorId(provinciaDTO.getId_Provincia());
        Provincia provinciaPorActualizar = modelMapper.map(provinciaDTO, Provincia.class);
        if (provinciaPorActualizar.getNombre() == null
          || provinciaPorActualizar.getNombre().isEmpty()) {
            provinciaPorActualizar.setNombre(provinciaPersistida.getNombre());
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
    
    @PostMapping("/provincias")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Provincia guardar(@RequestBody ProvinciaDTO provinciaDTO) {
        Provincia provincia = modelMapper.map(provinciaDTO, Provincia.class);
        return provinciaService.guardar(provincia);
    }
}
