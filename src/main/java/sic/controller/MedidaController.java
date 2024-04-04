package sic.controller;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Medida;
import sic.modelo.Rol;
import sic.modelo.dto.MedidaDTO;
import sic.service.IMedidaService;

@RestController
public class MedidaController {

  private final IMedidaService medidaService;
  private final ModelMapper modelMapper;

  @Autowired
  public MedidaController(IMedidaService medidaService,
                          ModelMapper modelMapper) {
    this.medidaService = medidaService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/api/v1/medidas/{idMedida}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Medida getMedidaPorId(@PathVariable long idMedida) {
    return medidaService.getMedidaNoEliminadaPorId(idMedida);
  }

  @PutMapping("/api/v1/medidas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody MedidaDTO medidaDTO) {
    Medida medidaPersistida = medidaService.getMedidaNoEliminadaPorId(medidaDTO.getIdMedida());
    Medida medidaPorActualizar = modelMapper.map(medidaDTO, Medida.class);
    if (medidaPorActualizar.getNombre() == null || medidaPorActualizar.getNombre().isEmpty()) {
      medidaPorActualizar.setNombre(medidaPersistida.getNombre());
    }
    medidaService.actualizar(medidaPorActualizar);
  }

  @DeleteMapping("/api/v1/medidas/{idMedida}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public void eliminar(@PathVariable long idMedida) {
    medidaService.eliminar(idMedida);
  }

  @PostMapping("/api/v1/medidas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Medida guardar(@RequestBody MedidaDTO medidaDTO) {
    Medida medida = modelMapper.map(medidaDTO, Medida.class);
    return medidaService.guardar(medida);
  }

  @GetMapping("/api/v1/medidas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<Medida> getMedidas() {
    return medidaService.getUnidadMedidas();
  }
}
