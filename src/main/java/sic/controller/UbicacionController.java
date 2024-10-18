package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.exception.BusinessServiceException;
import sic.modelo.Localidad;
import sic.modelo.Provincia;
import sic.modelo.Rol;
import sic.modelo.Ubicacion;
import sic.modelo.criteria.BusquedaLocalidadCriteria;
import sic.modelo.dto.LocalidadDTO;
import sic.modelo.dto.LocalidadesParaActualizarDTO;
import sic.service.UbicacionService;

import java.util.List;
import java.util.Locale;

@RestController
public class UbicacionController {

  private final UbicacionService ubicacionService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;

  @Autowired
  public UbicacionController(UbicacionService ubicacionService,
                             ModelMapper modelMapper,
                             MessageSource messageSource) {
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  @GetMapping("/api/v1/ubicaciones/{idUbicacion}")
  public Ubicacion getUbicacionPorId(@PathVariable Long idUbicacion) {
    return ubicacionService.getUbicacionPorId(idUbicacion);
  }

  @GetMapping("/api/v1/ubicaciones/localidades/{idLocalidad}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Localidad getLocalidadPorId(@PathVariable long idLocalidad) {
    return ubicacionService.getLocalidadPorId(idLocalidad);
  }

  @GetMapping("/api/v1/ubicaciones/localidades/provincias/{idProvincia}")
  public List<Localidad> getLocalidadesDeLaProvincia(@PathVariable long idProvincia) {
    return ubicacionService.getLocalidadesDeLaProvincia(
      ubicacionService.getProvinciaPorId(idProvincia));
  }

  @GetMapping("/api/v1/ubicaciones/provincias/{idProvincia}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Provincia getProvinciaPorId(@PathVariable long idProvincia) {
    return ubicacionService.getProvinciaPorId(idProvincia);
  }

  @GetMapping("/api/v1/ubicaciones/provincias")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
  public List<Provincia> getProvincias() {
    return ubicacionService.getProvincias();
  }

  @PutMapping("/api/v1/ubicaciones/localidades")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody LocalidadDTO localidadDTO) {
    Localidad localidadPersistida = ubicacionService.getLocalidadPorId(localidadDTO.getIdLocalidad());
    Localidad localidadPorActualizar = modelMapper.map(localidadDTO, Localidad.class);
    if (localidadPorActualizar.getNombre() != null
      && !localidadPorActualizar.getNombre().equals(localidadPersistida.getNombre())) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_localidad_cambio_nombre", null, Locale.getDefault()));
    }
    if (localidadPorActualizar.getCodigoPostal() == null) {
      localidadPorActualizar.setCodigoPostal(localidadPersistida.getCodigoPostal());
    }
    localidadPorActualizar.setNombre(localidadPersistida.getNombre());
    localidadPorActualizar.setProvincia(localidadPersistida.getProvincia());
    if (ubicacionService.getLocalidadPorId(localidadPorActualizar.getIdLocalidad()) != null) {
      ubicacionService.actualizarLocalidad(localidadPorActualizar);
    }
  }

  @PostMapping("/api/v1/ubicaciones/localidades/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Localidad> buscarLocalidades(@RequestBody BusquedaLocalidadCriteria criteria) {
    return ubicacionService.buscarLocalidades(criteria);
  }

  @PutMapping("/api/v1/ubicaciones/multiples")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizarMultiplesUbicaciones(@RequestBody LocalidadesParaActualizarDTO localidadesParaActualizarDTO) {
    ubicacionService.actualizarMultiplesLocalidades(localidadesParaActualizarDTO);
  }
}
