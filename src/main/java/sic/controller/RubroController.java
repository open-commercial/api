package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.Rubro;
import sic.modelo.dto.RubroDTO;
import sic.service.IEmpresaService;
import sic.service.IRubroService;

@RestController
@RequestMapping("/api/v1")
public class RubroController {

  private final IRubroService rubroService;
  private final IEmpresaService empresaService;
  private final ModelMapper modelMapper;

  @Autowired
  public RubroController(
      IRubroService rubroService, IEmpresaService empresaService, ModelMapper modelMapper) {
    this.rubroService = rubroService;
    this.empresaService = empresaService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/rubros/{idRubro}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Rubro getRubroPorId(@PathVariable long idRubro) {
    return rubroService.getRubroNoEliminadoPorId(idRubro);
  }

  @PutMapping("/rubros")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(
      @RequestBody RubroDTO rubroDTO, @RequestParam(required = false) Long idEmpresa) {
    Rubro rubroPersistido = rubroService.getRubroNoEliminadoPorId(rubroDTO.getIdRubro());
    Rubro rubroPorActualizar = modelMapper.map(rubroDTO, Rubro.class);
    if (rubroPorActualizar.getNombre() == null || rubroPorActualizar.getNombre().isEmpty()) {
      rubroPorActualizar.setNombre(rubroPersistido.getNombre());
    }
    if (idEmpresa != null) {
      rubroPorActualizar.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    } else {
      rubroPorActualizar.setEmpresa(rubroPersistido.getEmpresa());
    }
    rubroService.actualizar(rubroPorActualizar);
  }

  @DeleteMapping("/rubros/{idRubro}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idRubro) {
    rubroService.eliminar(idRubro);
  }

  @PostMapping("/rubros")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Rubro guardar(@RequestBody RubroDTO rubroDTO, @RequestParam Long idEmpresa) {
    Rubro rubro = modelMapper.map(rubroDTO, Rubro.class);
    rubro.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    return rubroService.guardar(rubro);
  }

  @GetMapping("/rubros/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Rubro> getRubros(@PathVariable long idEmpresa) {
    return rubroService.getRubros(empresaService.getEmpresaPorId(idEmpresa));
  }
}
