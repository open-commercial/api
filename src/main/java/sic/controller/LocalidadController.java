package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Localidad;
import sic.modelo.Rol;
import sic.modelo.dto.LocalidadDTO;
import sic.service.ILocalidadService;
import sic.service.IProvinciaService;

@RestController
@RequestMapping("/api/v1")
public class LocalidadController {

  private final ILocalidadService localidadService;
  private final IProvinciaService provinciaService;
  private final ModelMapper modelMapper;

  @Autowired
  public LocalidadController(
      ILocalidadService localidadService,
      IProvinciaService provinciaService,
      ModelMapper modelMapper) {
    this.localidadService = localidadService;
    this.provinciaService = provinciaService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/localidades/{idLocalidad}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Localidad getLocalidadPorId(@PathVariable long idLocalidad) {
    return localidadService.getLocalidadPorId(idLocalidad);
  }

  @PutMapping("/localidades")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(
      @RequestBody LocalidadDTO localidadDTO, @RequestParam(required = false) Long idProvincia) {
    Localidad localidadPersistida =
        localidadService.getLocalidadPorId(localidadDTO.getId_Localidad());
    Localidad localidadPorActualizar = modelMapper.map(localidadDTO, Localidad.class);
    if (localidadPorActualizar.getNombre() == null
        || localidadPorActualizar.getNombre().isEmpty()) {
      localidadPorActualizar.setNombre(localidadPersistida.getNombre());
    }
    if (localidadPorActualizar.getCodigoPostal() == null) {
      localidadPorActualizar.setCodigoPostal(localidadPersistida.getCodigoPostal());
    }
    if (idProvincia != null
        && !idProvincia.equals(localidadPersistida.getProvincia().getId_Provincia())) {
      localidadPorActualizar.setProvincia(provinciaService.getProvinciaPorId(idProvincia));
    }
    if (localidadService.getLocalidadPorId(localidadPorActualizar.getId_Localidad()) != null) {
      localidadService.actualizar(localidadPorActualizar);
    }
  }

  @DeleteMapping("/localidades/{idLocalidad}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idLocalidad) {
    localidadService.eliminar(idLocalidad);
  }

  @PostMapping("/localidades")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Localidad guardar(@RequestBody LocalidadDTO localidadDTO, @RequestParam Long idProvincia) {
      Localidad localidad = modelMapper.map(localidadDTO, Localidad.class);
      localidad.setProvincia(provinciaService.getProvinciaPorId(idProvincia));
      return localidadService.guardar(localidad);
  }

  @GetMapping("/localidades/provincias/{idProvincia}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Localidad> getLocalidadesDeLaProvincia(@PathVariable long idProvincia) {
    return localidadService.getLocalidadesDeLaProvincia(
        provinciaService.getProvinciaPorId(idProvincia));
  }
}
