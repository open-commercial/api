package sic.controller;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.Rubro;
import sic.modelo.dto.RubroDTO;
import sic.service.RubroService;

@RestController
public class RubroController {

  private final RubroService rubroService;
  private final ModelMapper modelMapper;

  @Autowired
  public RubroController(RubroService rubroService,
                         ModelMapper modelMapper) {
    this.rubroService = rubroService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/api/v1/rubros/{idRubro}")
  public Rubro getRubroPorId(@PathVariable long idRubro) {
    return rubroService.getRubroNoEliminadoPorId(idRubro);
  }

  @GetMapping("/api/v1/rubros")
  public List<Rubro> getRubros() {
    return rubroService.getRubros();
  }

  @PutMapping("/api/v1/rubros")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody RubroDTO rubroDTO) {
    Rubro rubroPersistido = rubroService.getRubroNoEliminadoPorId(rubroDTO.getIdRubro());
    Rubro rubroPorActualizar = modelMapper.map(rubroDTO, Rubro.class);
    if (rubroPorActualizar.getNombre() == null || rubroPorActualizar.getNombre().isEmpty()) {
      rubroPorActualizar.setNombre(rubroPersistido.getNombre());
    }
    rubroService.actualizar(rubroPorActualizar);
  }

  @DeleteMapping("/api/v1/rubros/{idRubro}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idRubro) {
    rubroService.eliminar(idRubro);
  }

  @PostMapping("/api/v1/rubros")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Rubro guardar(@RequestBody RubroDTO rubroDTO) {
    Rubro rubro = modelMapper.map(rubroDTO, Rubro.class);
    return rubroService.guardar(rubro);
  }
}
