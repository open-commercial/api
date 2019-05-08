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
import sic.service.IEmpresaService;

@RestController
@RequestMapping("/api/v1")
public class ConfiguracionDelSistemaController {
    
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final IEmpresaService empresaService;
    private final ModelMapper modelMapper;
    
    @Autowired
    public ConfiguracionDelSistemaController(IConfiguracionDelSistemaService configuracionDelSistemaService,
                                             IEmpresaService empresaService,
                                             ModelMapper modelMapper) {
        this.configuracionDelSistemaService = configuracionDelSistemaService;
        this.empresaService = empresaService;
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
    configuracionDelSistema.setEmpresa(cdsRecuperado.getEmpresa());
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

    @GetMapping("/configuraciones-del-sistema/empresas/{idEmpresa}")
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public ConfiguracionDelSistema getconfiguracionDelSistemaPorEmpresa(@PathVariable long idEmpresa) {
        return configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    }

    @GetMapping("/configuraciones-del-sistema/empresas/{idEmpresa}/cantidad-renglones")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public int getCantidadMaximaDeRenglonesPorIdEmpresa(@PathVariable long idEmpresa) {
        return configuracionDelSistemaService.getCantidadMaximaDeRenglonesPorIdEmpresa(idEmpresa);
    }

    @GetMapping("/configuraciones-del-sistema/empresas/{idEmpresa}/factura-electronica-habilitada")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public boolean isFacturaElectronicaHabilitada(@PathVariable long idEmpresa) {
        return configuracionDelSistemaService.isFacturaElectronicaHabilitada(idEmpresa);
    }

}
