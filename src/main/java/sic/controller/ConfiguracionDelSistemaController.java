package sic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.ConfiguracionDelSistema;
import sic.modelo.Rol;
import sic.service.IConfiguracionDelSistemaService;
import sic.service.IEmpresaService;

@RestController
@RequestMapping("/api/v1")
public class ConfiguracionDelSistemaController {
    
    private final IConfiguracionDelSistemaService configuracionDelSistemaService;
    private final IEmpresaService empresaService;
    
    @Autowired
    public ConfiguracionDelSistemaController(IConfiguracionDelSistemaService configuracionDelSistemaService,
                                             IEmpresaService empresaService) {
        this.configuracionDelSistemaService = configuracionDelSistemaService;
        this.empresaService = empresaService;
    }
    
    @PutMapping("/configuraciones-del-sistema")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public void actualizar(@RequestBody ConfiguracionDelSistema configuracionDelSistema) {
        if(configuracionDelSistemaService.getConfiguracionDelSistemaPorId(configuracionDelSistema.getId_ConfiguracionDelSistema()) != null) {
            configuracionDelSistemaService.actualizar(configuracionDelSistema);
        }
    }
    
    @PostMapping("/configuracion-del-sistema")
    @ResponseStatus(HttpStatus.CREATED)
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public ConfiguracionDelSistema guardar(@RequestBody ConfiguracionDelSistema configuracionDelSistema) {
        return configuracionDelSistemaService.guardar(configuracionDelSistema);
    }
    
    @GetMapping("/configuraciones-del-sistema/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
    public ConfiguracionDelSistema getconfiguracionDelSistemaPorEmpresa(@PathVariable long idEmpresa) {
        return configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    }

    @GetMapping("/configuraciones-del-sistema/empresas/{idEmpresa}/cantidad-renglones")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public int getCantidadMaximaDeRenglonesPorIdEmpresa(@PathVariable long idEmpresa) {
        return configuracionDelSistemaService.getCantidadMaximaDeRenglonesPorIdEmpresa(idEmpresa);
    }

    @GetMapping("/configuraciones-del-sistema/empresas/{idEmpresa}/factura-electronica-habilitada")
    @ResponseStatus(HttpStatus.OK)
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public boolean isFacturaElectronicaHabilitada(@PathVariable long idEmpresa) {
        return configuracionDelSistemaService.isFacturaElectronicaHabilitada(idEmpresa);
    }

}
