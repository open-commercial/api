package org.opencommercial.controller;

import org.modelmapper.ModelMapper;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.ConfiguracionSucursal;
import org.opencommercial.model.Rol;
import org.opencommercial.model.dto.ConfiguracionSucursalDTO;
import org.opencommercial.service.ConfiguracionSucursalService;
import org.opencommercial.service.SucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConfiguracionSucursalController {

  private final ConfiguracionSucursalService configuracionSucursal;
  private final SucursalService sucursalService;
  private final ModelMapper modelMapper;

  @Autowired
  public ConfiguracionSucursalController(ConfiguracionSucursalService configuracionSucursal,
                                         SucursalService sucursalService,
                                         ModelMapper modelMapper) {
    this.configuracionSucursal = configuracionSucursal;
    this.sucursalService = sucursalService;
    this.modelMapper = modelMapper;
  }

  @PutMapping("/api/v1/configuraciones-sucursal")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void actualizar(@RequestBody ConfiguracionSucursalDTO configuracionSucursalDTO) {
    ConfiguracionSucursal configuracionDeSucursalParaActualizar =
        modelMapper.map(configuracionSucursalDTO, ConfiguracionSucursal.class);
    this.configuracionSucursal.actualizar(configuracionDeSucursalParaActualizar);
  }

  @PostMapping("/api/v1/configuraciones-sucursal")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public ConfiguracionSucursal guardar(@RequestBody ConfiguracionSucursalDTO configuracionSucursalDTO) {
    ConfiguracionSucursal configuracionDeSucursal =
        modelMapper.map(configuracionSucursalDTO, ConfiguracionSucursal.class);
    return this.configuracionSucursal.guardar(configuracionDeSucursal);
  }

  @GetMapping("/api/v1/configuraciones-sucursal/{idSucursal}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public ConfiguracionSucursal getConfiguracionSucursal(@PathVariable long idSucursal) {
    return sucursalService.getSucursalPorId(idSucursal).getConfiguracionSucursal();
  }

  @GetMapping("/api/v1/configuraciones-sucursal/{idSucursal}/factura-electronica-habilitada")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public boolean isFacturaElectronicaHabilitada(@PathVariable long idSucursal) {
    return configuracionSucursal.isFacturaElectronicaHabilitada(idSucursal);
  }
}
