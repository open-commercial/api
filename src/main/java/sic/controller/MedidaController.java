package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Medida;
import sic.modelo.Rol;
import sic.modelo.dto.MedidaDTO;
import sic.service.IEmpresaService;
import sic.service.IMedidaService;

@RestController
@RequestMapping("/api/v1")
public class MedidaController {
    
    private final IMedidaService medidaService;
    private final IEmpresaService empresaService;
    private final ModelMapper modelMapper;
    
    @Autowired
    public MedidaController(IMedidaService medidaService, IEmpresaService empresaService, ModelMapper modelMapper) {
        this.medidaService = medidaService;
        this.empresaService = empresaService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/medidas/{idMedida}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Medida getMedidaPorId(@PathVariable long idMedida) {
        return medidaService.getMedidaNoEliminadaPorId(idMedida);
    }
    
    @PutMapping("/medidas")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void actualizar(@RequestBody MedidaDTO medidaDTO, @RequestParam(required = false) Long idEmpresa) {
        Medida medidaPersistida = medidaService.getMedidaNoEliminadaPorId(medidaDTO.getId_Medida());
        Medida medidaPorActualizar = modelMapper.map(medidaDTO, Medida.class);
        if (medidaPorActualizar.getNombre() == null || medidaPorActualizar.getNombre().isEmpty()) {
            medidaPorActualizar.setNombre(medidaPersistida.getNombre());
        }
        if (idEmpresa != null) {
            medidaPorActualizar.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
        } else {
            medidaPorActualizar.setEmpresa(medidaPersistida.getEmpresa());
        }
        medidaService.actualizar(medidaPorActualizar);
    }
    
    @DeleteMapping("/medidas/{idMedida}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
    public void eliminar(@PathVariable long idMedida) {
        medidaService.eliminar(idMedida);
    }
    
    @PostMapping("/medidas")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Medida guardar(@RequestBody MedidaDTO medidaDTO, @RequestParam Long idEmpresa) {
        Medida medida = modelMapper.map(medidaDTO, Medida.class);
        medida.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
        return medidaService.guardar(medida);
    }
    
    @GetMapping("/medidas/empresas/{idEmpresa}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public List<Medida> getMedidas(@PathVariable long idEmpresa) {
        return medidaService.getUnidadMedidas(empresaService.getEmpresaPorId(idEmpresa));
    }
}
