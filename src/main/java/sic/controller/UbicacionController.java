package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Localidad;
import sic.modelo.Provincia;
import sic.modelo.Rol;
import sic.modelo.Ubicacion;
import sic.modelo.dto.UbicacionDTO;
import sic.service.IClienteService;
import sic.service.IUbicacionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UbicacionController {

  private final IUbicacionService ubicacionService;
  private final IClienteService clienteService;
  private final ModelMapper modelMapper;

  @Autowired
  public UbicacionController(IUbicacionService ubicacionService,
                             IClienteService clienteService,
                             ModelMapper modelMapper) {
    this.ubicacionService = ubicacionService;
    this.clienteService = clienteService;
    this.modelMapper = modelMapper;
  }

  @PutMapping("/ubicaciones/envio/clientes/{idCliente}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public void actualizarEnvio(
      @RequestBody UbicacionDTO ubicacionDTO, @PathVariable long idCliente) {
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    if (ubicacionDTO.getIdLocalidad() != null) {
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacionDTO.getIdLocalidad()));
    }
    ubicacionService.actualizarUbicacionEnvio(ubicacion, clienteService.getClientePorId(idCliente));
  }

  @PutMapping("/ubicaciones/facturacion/clientes/{idCliente}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public void actualizarFacturacion(
      @RequestBody UbicacionDTO ubicacionDTO, @PathVariable long idCliente) {
    Ubicacion ubicacion = modelMapper.map(ubicacionDTO, Ubicacion.class);
    if (ubicacionDTO.getIdLocalidad() != null) {
      ubicacion.setLocalidad(ubicacionService.getLocalidadPorId(ubicacionDTO.getIdLocalidad()));
    }
    ubicacionService.actualizarUbicacionEnvio(ubicacion, clienteService.getClientePorId(idCliente));
  }

  @GetMapping("/ubicaciones/localidades/{idLocalidad}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
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
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Provincia getProvinciaPorId(@PathVariable long idProvincia) {
    return ubicacionService.getProvinciaPorId(idProvincia);
  }

  @GetMapping("/ubicaciones/provincias")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.COMPRADOR, Rol.VIAJANTE})
  public List<Provincia> getProvincias() {
    return ubicacionService.getProvincias();
  }

}
