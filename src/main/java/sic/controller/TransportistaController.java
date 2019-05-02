package sic.controller;

import java.util.List;
import java.util.ResourceBundle;

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
import sic.service.BusinessServiceException;
import sic.service.IEmpresaService;
import sic.service.ITransportistaService;
import sic.service.IUbicacionService;

@RestController
@RequestMapping("/api/v1")
public class TransportistaController {

  private final ITransportistaService transportistaService;
  private final IEmpresaService empresaService;
  private final IUbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public TransportistaController(
    ITransportistaService transportistaService, IEmpresaService empresaService,
    IUbicacionService ubicacionService, ModelMapper modelMapper) {
    this.transportistaService = transportistaService;
    this.empresaService = empresaService;
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/transportistas/{idTransportista}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Transportista getTransportistaPorId(@PathVariable long idTransportista) {
    return transportistaService.getTransportistaPorId(idTransportista);
  }

  @PutMapping("/transportistas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void actualizar(@RequestBody TransportistaDTO transportistaDTO) {
    Transportista transportistaPersistido =
        transportistaService.getTransportistaPorId(transportistaDTO.getId_Transportista());
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
    if (transportistaDTO.getIdEmpresa() == null) {
      transportistaPorActualizar.setEmpresa(transportistaPersistido.getEmpresa());
    } else {
      transportistaPorActualizar.setEmpresa(
          empresaService.getEmpresaPorId(transportistaDTO.getIdEmpresa()));
    }
    if (transportistaService.getTransportistaPorId(transportistaPorActualizar.getId_Transportista())
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
    @RequestParam long idEmpresa,
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
        idEmpresa);
    return transportistaService.buscarTransportistas(criteria);
  }

  @DeleteMapping("/transportistas/{idTransportista}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idTransportista) {
    transportistaService.eliminar(idTransportista);
  }

  @GetMapping("/transportistas/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<Transportista> getTransportistas(@PathVariable long idEmpresa) {
    return transportistaService.getTransportistas(empresaService.getEmpresaPorId(idEmpresa));
  }

  @PostMapping("/transportistas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Transportista guardar(@RequestBody TransportistaDTO transportistaDTO) {
    Transportista transportista = modelMapper.map(transportistaDTO, Transportista.class);
    if (transportistaDTO.getIdEmpresa() != null) {
      transportista.setEmpresa(empresaService.getEmpresaPorId(transportistaDTO.getIdEmpresa()));
    } else {
      throw new BusinessServiceException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_transportista_empresa_vacia"));
    }
    transportista.setUbicacion(null);
    if (transportistaDTO.getUbicacion() != null) {
      transportista.setUbicacion(modelMapper.map(transportistaDTO.getUbicacion(), Ubicacion.class));
    }
    return transportistaService.guardar(transportista);
  }
}
