package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.LocalidadDTO;
import sic.service.*;
import sic.exception.BusinessServiceException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1")
public class UbicacionController {

  private final IUbicacionService ubicacionService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;

  @Autowired
  public UbicacionController(
    IUbicacionService ubicacionService,
    ModelMapper modelMapper,
    MessageSource messageSource) {
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  @GetMapping("/ubicaciones/{idUbicacion}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Ubicacion getUbicacionPorId(@PathVariable Long idUbicacion) {
    return ubicacionService.getUbicacionPorId(idUbicacion);
  }

  @GetMapping("/ubicaciones/localidades/{idLocalidad}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Localidad getLocalidadPorId(@PathVariable long idLocalidad) {
    return ubicacionService.getLocalidadPorId(idLocalidad);
  }

  @GetMapping("/ubicaciones/localidades/provincias/{idProvincia}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Localidad> getLocalidadesDeLaProvincia(@PathVariable long idProvincia) {
    return ubicacionService.getLocalidadesDeLaProvincia(
      ubicacionService.getProvinciaPorId(idProvincia));
  }

  @GetMapping("/ubicaciones/provincias/{idProvincia}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public Provincia getProvinciaPorId(@PathVariable long idProvincia) {
    return ubicacionService.getProvinciaPorId(idProvincia);
  }

  @GetMapping("/ubicaciones/provincias")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.COMPRADOR,
    Rol.VIAJANTE
  })
  public List<Provincia> getProvincias() {
    return ubicacionService.getProvincias();
  }

  @PutMapping("/ubicaciones/localidades")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody LocalidadDTO localidadDTO) {
    Localidad localidadPersistida =
      ubicacionService.getLocalidadPorId(localidadDTO.getIdLocalidad());
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

  @GetMapping("/ubicaciones/localidades/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Localidad> buscarConCriteria(
    @RequestParam(required = false) String nombreLocalidad,
    @RequestParam(required = false) String codigoPostal,
    @RequestParam(required = false) String nombreProvincia,
    @RequestParam(required = false) Boolean envioGratuito,
    @RequestParam(required = false) Integer pagina,
    @RequestParam(required = false) String ordenarPor,
    @RequestParam(required = false) String sentido) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
        PageRequest.of(
          pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombre"));
    } else {
      switch (sentido) {
        case "ASC":
          pageable =
            PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC":
          pageable =
            PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default:
          pageable =
            PageRequest.of(
              pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombre"));
          break;
      }
    }
    BusquedaLocalidadCriteria criteria = BusquedaLocalidadCriteria.builder()
      .buscaPorNombre(nombreLocalidad != null)
      .nombre(nombreLocalidad)
      .buscaPorCodigoPostal(codigoPostal != null)
      .codigoPostal(codigoPostal)
      .buscaPorNombreProvincia(nombreProvincia != null)
      .nombreProvincia(nombreProvincia)
      .buscaPorEnvio(envioGratuito != null)
      .envioGratuito(envioGratuito)
      .pageable(pageable)
      .build();
    return ubicacionService.buscar(criteria);
  }
}
