package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.Transportista;
import sic.modelo.Ubicacion;
import sic.modelo.criteria.BusquedaTransportistaCriteria;
import sic.modelo.dto.TransportistaDTO;
import sic.service.TransportistaService;
import sic.service.UbicacionService;

import java.util.List;

@RestController
public class TransportistaController {

  private final TransportistaService transportistaService;
  private final UbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public TransportistaController(TransportistaService transportistaService,
                                 UbicacionService ubicacionService,
                                 ModelMapper modelMapper) {
    this.transportistaService = transportistaService;
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/api/v1/transportistas/{idTransportista}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Transportista getTransportistaPorId(@PathVariable long idTransportista) {
    return transportistaService.getTransportistaNoEliminadoPorId(idTransportista);
  }

  @PutMapping("/api/v1/transportistas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody TransportistaDTO transportistaDTO) {
    Transportista transportistaPersistido =
        transportistaService.getTransportistaNoEliminadoPorId(transportistaDTO.getIdTransportista());
    Transportista transportistaPorActualizar =
        modelMapper.map(transportistaDTO, Transportista.class);
    if (transportistaPorActualizar.getNombre() == null
        || transportistaPorActualizar.getNombre().isEmpty()) {
      transportistaPorActualizar.setNombre(transportistaPersistido.getNombre());
    }
    Ubicacion ubicacion;
    if (transportistaDTO.getUbicacion() != null) {
      ubicacion = modelMapper.map(transportistaDTO.getUbicacion(), Ubicacion.class);
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacion.getIdLocalidad()));
      transportistaPorActualizar.setUbicacion(ubicacion);
    } else {
      transportistaPorActualizar.setUbicacion(transportistaPersistido.getUbicacion());
    }
    if (transportistaPorActualizar.getWeb() == null) {
      transportistaPorActualizar.setWeb(transportistaPersistido.getWeb());
    }
    if (transportistaPorActualizar.getTelefono() == null) {
      transportistaPorActualizar.setTelefono(transportistaPersistido.getTelefono());
    }
    if (transportistaService.getTransportistaNoEliminadoPorId(transportistaPorActualizar.getIdTransportista())
        != null) {
      transportistaService.actualizar(transportistaPorActualizar);
    }
  }

  @PostMapping("/api/v1/transportistas/busqueda/criteria")
  public Page<Transportista> buscarTransportistas(@RequestBody BusquedaTransportistaCriteria criteria) {
    return transportistaService.buscarTransportistas(criteria);
  }

  @DeleteMapping("/api/v1/transportistas/{idTransportista}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idTransportista) {
    transportistaService.eliminar(idTransportista);
  }

  @GetMapping("/api/v1/transportistas")
  public List<Transportista> getTransportistas() {
    return transportistaService.getTransportistas();
  }

  @PostMapping("/api/v1/transportistas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Transportista guardar(@RequestBody TransportistaDTO transportistaDTO) {
    Transportista transportista = modelMapper.map(transportistaDTO, Transportista.class);
    transportista.setUbicacion(null);
    if (transportistaDTO.getUbicacion() != null) {
      transportista.setUbicacion(modelMapper.map(transportistaDTO.getUbicacion(), Ubicacion.class));
    }
    return transportistaService.guardar(transportista);
  }
}
