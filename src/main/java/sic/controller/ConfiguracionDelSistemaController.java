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
import sic.modelo.ConfiguracionDelSistema;
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
    
    @GetMapping("/configuraciones-del-sistema/{idConfiguracionDelSistema}")
    @ResponseStatus(HttpStatus.OK)
    public ConfiguracionDelSistema getConfiguracionDelSistemaPorId(@PathVariable long idConfiguracionDelSistema) {
        return configuracionDelSistemaService.getConfiguracionDelSistemaPorId(idConfiguracionDelSistema);
    }
    
    @PutMapping("/configuraciones-del-sistema")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody ConfiguracionDelSistema configuracionDelSistema) {
        if(configuracionDelSistemaService.getConfiguracionDelSistemaPorId(configuracionDelSistema.getId_ConfiguracionDelSistema()) != null) {
            configuracionDelSistemaService.actualizar(configuracionDelSistema);
        }
    }
    
    @PostMapping("/configuracion-del-sistema")
    @ResponseStatus(HttpStatus.CREATED)
    public ConfiguracionDelSistema guardar(@RequestBody ConfiguracionDelSistema configuracionDelSistema) {
        return configuracionDelSistemaService.guardar(configuracionDelSistema);
    }
    
    @GetMapping("/configuraciones-del-sistema/empresas/{idEmpresa}")
    @ResponseStatus(HttpStatus.OK)
    public ConfiguracionDelSistema getconfiguracionDelSistemaPorEmpresa(@PathVariable long idEmpresa) {
        return configuracionDelSistemaService.getConfiguracionDelSistemaPorEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    }
}
