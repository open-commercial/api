package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.dto.TransportistaDTO;
import sic.service.ISucursalService;
import sic.service.ITransportistaService;
import sic.service.IUbicacionService;

@RestController
@RequestMapping("/api/v1")
public class TransportistaController {

  private final ITransportistaService transportistaService;
  private final ISucursalService sucursalService;
  private final IUbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public TransportistaController(
    ITransportistaService transportistaService, ISucursalService sucursalService,
    IUbicacionService ubicacionService, ModelMapper modelMapper) {
    this.transportistaService = transportistaService;
    this.sucursalService = sucursalService;
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
        transportistaService.getTransportistaNoEliminadoPorId(transportistaDTO.getId_Transportista());
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
    if (transportistaDTO.getIdSucursal() == null) {
      transportistaPorActualizar.setSucursal(transportistaPersistido.getSucursal());
    } else {
      transportistaPorActualizar.setSucursal(
          sucursalService.getSucursalPorId(transportistaDTO.getIdSucursal()));
    }
    if (transportistaService.getTransportistaNoEliminadoPorId(transportistaPorActualizar.getId_Transportista())
        != null) {
      transportistaService.actualizar(transportistaPorActualizar);
    }
  }

  @GetMapping("/transportistas/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Transportista> buscarTransportista(
    @RequestParam long idSucursal,
    @RequestParam(required = false) String nombre,
    @RequestParam(required = false) Long idProvincia,
    @RequestParam(required = false) Long idLocalidad) {
    BusquedaTransportistaCriteria criteria =
      new BusquedaTransportistaCriteria(
        (nombre != null),
        nombre,
        (idProvincia != null),
        idProvincia,
        (idLocalidad != null),
        idLocalidad,
        idSucursal);
    return transportistaService.buscarTransportistas(criteria);
  }

  @DeleteMapping("/transportistas/{idTransportista}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idTransportista) {
    transportistaService.eliminar(idTransportista);
  }

  @GetMapping("/transportistas/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Transportista> getTransportistas(@PathVariable long idSucursal) {
    return transportistaService.getTransportistas(sucursalService.getSucursalPorId(idSucursal));
  }

  @PostMapping("/transportistas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Transportista guardar(@RequestBody TransportistaDTO transportistaDTO) {
    Transportista transportista = modelMapper.map(transportistaDTO, Transportista.class);
    transportista.setSucursal(sucursalService.getSucursalPorId(transportistaDTO.getIdSucursal()));
    transportista.setUbicacion(null);
    if (transportistaDTO.getUbicacion() != null) {
      transportista.setUbicacion(modelMapper.map(transportistaDTO.getUbicacion(), Ubicacion.class));
    }
    return transportistaService.guardar(transportista);
  }
}
