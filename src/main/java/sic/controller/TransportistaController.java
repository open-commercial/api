package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.domain.Rol;
import sic.entity.Transportista;
import sic.entity.Ubicacion;
import sic.entity.criteria.BusquedaTransportistaCriteria;
import sic.dto.TransportistaDTO;
import sic.service.ITransportistaService;
import sic.service.IUbicacionService;

@RestController
@RequestMapping("/api/v1")
public class TransportistaController {

  private final ITransportistaService transportistaService;
  private final IUbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public TransportistaController(
      ITransportistaService transportistaService,
      IUbicacionService ubicacionService,
      ModelMapper modelMapper) {
    this.transportistaService = transportistaService;
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/transportistas/{idTransportista}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Transportista getTransportistaPorId(@PathVariable long idTransportista) {
    return transportistaService.getTransportistaNoEliminadoPorId(idTransportista);
  }

  @PutMapping("/transportistas")
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

  @PostMapping("/transportistas/busqueda/criteria")
  public Page<Transportista> buscarTransportistas(
      @RequestBody BusquedaTransportistaCriteria criteria) {
    return transportistaService.buscarTransportistas(criteria);
  }

  @DeleteMapping("/transportistas/{idTransportista}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idTransportista) {
    transportistaService.eliminar(idTransportista);
  }

  @GetMapping("/transportistas")
  public List<Transportista> getTransportistas() {
    return transportistaService.getTransportistas();
  }

  @PostMapping("/transportistas")
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
