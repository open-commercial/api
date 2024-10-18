package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.Sucursal;
import sic.modelo.Ubicacion;
import sic.modelo.dto.NuevaSucursalDTO;
import sic.modelo.dto.SucursalDTO;
import sic.service.SucursalService;
import sic.service.UbicacionService;

import java.util.List;

@RestController
public class SucursalController {

  public final SucursalService sucursalService;
  public final UbicacionService ubicacionService;
  private final ModelMapper modelMapper;

  @Autowired
  public SucursalController(SucursalService sucursalService,
                            UbicacionService ubicacionService,
                            ModelMapper modelMapper) {
    this.sucursalService = sucursalService;
    this.ubicacionService = ubicacionService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/api/v1/sucursales")
  public List<Sucursal> getSucursales(@RequestParam(required = false) boolean puntoDeRetiro) {
    return sucursalService.getSucusales(puntoDeRetiro);
  }

  @GetMapping("/api/v1/sucursales/{idSucursal}")
  public Sucursal getSucursalPorId(@PathVariable long idSucursal) {
    return sucursalService.getSucursalPorId(idSucursal);
  }

  @PostMapping("/api/v1/sucursales")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public Sucursal guardar(@RequestBody NuevaSucursalDTO nuevaSucursal) {
    Ubicacion ubicacion = new Ubicacion();
    if (nuevaSucursal.getUbicacion() != null) {
      ubicacion = modelMapper.map(nuevaSucursal.getUbicacion(), Ubicacion.class);
    }
    return sucursalService.guardar(nuevaSucursal, ubicacion, nuevaSucursal.getImagen());
  }

  @PutMapping("/api/v1/sucursales")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void actualizar(@RequestBody SucursalDTO sucursalDTO) {
    Sucursal sucursalParaActualizar = modelMapper.map(sucursalDTO, Sucursal.class);
    Sucursal sucursalPersistida =
        sucursalService.getSucursalPorId(sucursalParaActualizar.getIdSucursal());
    if (sucursalParaActualizar.getNombre() == null || sucursalParaActualizar.getNombre().isEmpty()) {
      sucursalParaActualizar.setNombre(sucursalPersistida.getNombre());
    }
    if (sucursalParaActualizar.getCategoriaIVA() == null) {
      sucursalParaActualizar.setCategoriaIVA(sucursalPersistida.getCategoriaIVA());
    }
    Ubicacion ubicacion;
    if (sucursalDTO.getUbicacion() != null) {
      ubicacion = modelMapper.map(sucursalDTO.getUbicacion(), Ubicacion.class);
      ubicacion.setLocalidad(
          ubicacionService.getLocalidadPorId(
              modelMapper.map(sucursalDTO.getUbicacion(), Ubicacion.class).getIdLocalidad()));
      sucursalParaActualizar.setUbicacion(ubicacion);
    } else {
      sucursalParaActualizar.setUbicacion(sucursalPersistida.getUbicacion());
    }
    if (sucursalPersistida.getLogo() != null) sucursalParaActualizar.setLogo(sucursalPersistida.getLogo());
    sucursalService.actualizar(sucursalParaActualizar, sucursalPersistida, sucursalDTO.getImagen());
  }

  @DeleteMapping("/api/v1/sucursales/{idSucursales}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idSucursales) {
    sucursalService.eliminar(idSucursales);
  }

}
