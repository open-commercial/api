package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.ConfiguracionSucursal;
import sic.modelo.Rol;
import sic.modelo.dto.ConfiguracionSucursalDTO;
import sic.service.IConfiguracionSucursalService;
import sic.service.ISucursalService;

@RestController
@RequestMapping("/api/v1")
public class ConfiguracionSucursalController {

  private final IConfiguracionSucursalService configuracionSucursal;
  private final ISucursalService sucursalService;
  private final ModelMapper modelMapper;

  @Autowired
  public ConfiguracionSucursalController(
      IConfiguracionSucursalService configuracionSucursal,
      ISucursalService sucursalService,
      ModelMapper modelMapper) {
    this.configuracionSucursal = configuracionSucursal;
    this.sucursalService = sucursalService;
    this.modelMapper = modelMapper;
  }

  @PutMapping("/configuraciones-sucursal")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void actualizar(@RequestBody ConfiguracionSucursalDTO configuracionSucursalDTO) {
    ConfiguracionSucursal configuracionDeSucursalParaActualizar =
        modelMapper.map(configuracionSucursalDTO, ConfiguracionSucursal.class);
    this.configuracionSucursal.actualizar(configuracionDeSucursalParaActualizar);
  }

  @PostMapping("/configuraciones-sucursal")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public ConfiguracionSucursal guardar(
      @RequestBody ConfiguracionSucursalDTO configuracionSucursalDTO) {
    ConfiguracionSucursal configuracionDeSucursal =
        modelMapper.map(configuracionSucursalDTO, ConfiguracionSucursal.class);
    return this.configuracionSucursal.guardar(configuracionDeSucursal);
  }

  @GetMapping("/configuraciones-sucursal/{idSucursal}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public ConfiguracionSucursal getConfiguracionSucursal(
      @PathVariable long idSucursal) {
    return sucursalService.getSucursalPorId(idSucursal).getConfiguracionSucursal();
  }

  @GetMapping("/configuraciones-sucursal/{idSucursal}/cantidad-renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public int getCantidadMaximaDeRenglonesPorIdSucursal(@PathVariable long idSucursal) {
    return configuracionSucursal.getCantidadMaximaDeRenglonesPorIdSucursal(idSucursal);
  }

  @GetMapping("/configuraciones-sucursal/{idSucursal}/factura-electronica-habilitada")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public boolean isFacturaElectronicaHabilitada(@PathVariable long idSucursal) {
    return configuracionSucursal.isFacturaElectronicaHabilitada(idSucursal);
  }
}
