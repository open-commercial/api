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
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Rol;
import sic.modelo.dto.ConfiguracionDelSistemaDTO;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.ISucursalService;

@RestController
@RequestMapping("/api/v1")
public class ConfiguracionDelSistemaController {

  private final IConfiguracionDelSistemaService configuracionDelSistemaService;
  private final ISucursalService sucursalService;
  private final ModelMapper modelMapper;

  @Autowired
  public ConfiguracionDelSistemaController(
      IConfiguracionDelSistemaService configuracionDelSistemaService,
      ISucursalService sucursalService,
      ModelMapper modelMapper) {
    this.configuracionDelSistemaService = configuracionDelSistemaService;
    this.sucursalService = sucursalService;
    this.modelMapper = modelMapper;
  }

  @PutMapping("/configuraciones-del-sistema")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void actualizar(@RequestBody ConfiguracionDelSistemaDTO configuracionDelSistemaDTO) {
    ConfiguracionDelSistema configuracionDelSistema =
        modelMapper.map(configuracionDelSistemaDTO, ConfiguracionDelSistema.class);
    ConfiguracionDelSistema cdsRecuperado =
        configuracionDelSistemaService.getConfiguracionDelSistemaPorId(
            configuracionDelSistemaDTO.getId_ConfiguracionDelSistema());
    configuracionDelSistema.setSucursal(cdsRecuperado.getSucursal());
    if (configuracionDelSistema.isFacturaElectronicaHabilitada()) {
      if (configuracionDelSistema.getPasswordCertificadoAfip().equals("")) {
        configuracionDelSistema.setPasswordCertificadoAfip(
            cdsRecuperado.getPasswordCertificadoAfip());
      }
      if (configuracionDelSistema.getCertificadoAfip() == null) {
        configuracionDelSistema.setCertificadoAfip(cdsRecuperado.getCertificadoAfip());
      }
      configuracionDelSistema.setSignTokenWSAA(cdsRecuperado.getSignTokenWSAA());
      configuracionDelSistema.setTokenWSAA(cdsRecuperado.getTokenWSAA());
      configuracionDelSistema.setFechaGeneracionTokenWSAA(
          cdsRecuperado.getFechaGeneracionTokenWSAA());
      configuracionDelSistema.setFechaVencimientoTokenWSAA(
          cdsRecuperado.getFechaVencimientoTokenWSAA());
    }
    if (cdsRecuperado.isEmailSenderHabilitado()
        && (configuracionDelSistema.isEmailSenderHabilitado()
            && configuracionDelSistema.getEmailPassword().equals(""))) {
      configuracionDelSistema.setEmailPassword(cdsRecuperado.getEmailPassword());
    }
    configuracionDelSistemaService.actualizar(configuracionDelSistema);
  }

  @PostMapping("/configuracion-del-sistema")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public ConfiguracionDelSistema guardar(
      @RequestBody ConfiguracionDelSistemaDTO configuracionDelSistemaDTO) {
    ConfiguracionDelSistema configuracionDelSistema =
        modelMapper.map(configuracionDelSistemaDTO, ConfiguracionDelSistema.class);
    return configuracionDelSistemaService.guardar(configuracionDelSistema);
  }

  @GetMapping("/configuraciones-del-sistema/sucursales/{idSucursal}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public ConfiguracionDelSistema getconfiguracionDelSistemaPorSucursal(
      @PathVariable long idSucursal) {
    return configuracionDelSistemaService.getConfiguracionDelSistemaPorSucursal(
        sucursalService.getSucursalPorId(idSucursal));
  }

  @GetMapping("/configuraciones-del-sistema/sucursales/{idSucursal}/cantidad-renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public int getCantidadMaximaDeRenglonesPorIdSucursal(@PathVariable long idSucursal) {
    return configuracionDelSistemaService.getCantidadMaximaDeRenglonesPorIdSucursal(idSucursal);
  }

  @GetMapping("/configuraciones-del-sistema/sucursales/{idSucursal}/factura-electronica-habilitada")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public boolean isFacturaElectronicaHabilitada(@PathVariable long idSucursal) {
    return configuracionDelSistemaService.isFacturaElectronicaHabilitada(idSucursal);
  }
}
