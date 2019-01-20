package sic.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Gasto;
import sic.modelo.Rol;
import sic.modelo.dto.GastoDTO;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class GastoController {
    
    private final IGastoService gastoService;
    private final IEmpresaService empresaService;
    private final IFormaDePagoService formaDePagoService;
    private final IUsuarioService usuarioService;
    private final IAuthService authService;
    private final ModelMapper modelMapper;
    
    @Autowired
    public GastoController(IGastoService gastoService, ModelMapper modelMapper,
                           IEmpresaService empresaService, IFormaDePagoService formaDePagoService,
                           IUsuarioService usuarioService, IAuthService authService) {
        this.gastoService = gastoService;
        this.empresaService = empresaService;
        this.formaDePagoService = formaDePagoService;
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/gastos/{idGasto}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Gasto getGastoPorId(@PathVariable long idGasto) {
        return gastoService.getGastoPorId(idGasto);
    }
    
    @DeleteMapping("/gastos/{idGasto}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public void eliminar(@PathVariable long idGasto) {
        gastoService.eliminar(idGasto);
    }

    @PostMapping("/gastos")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Gasto guardar(@RequestBody GastoDTO gastoDTO,
                         @RequestParam Long idEmpresa,
                         @RequestParam Long idFormaDePago,
                         @RequestHeader(name = "Authorization") String authorizationHeader) {
        Gasto gasto = modelMapper.map(gastoDTO, Gasto.class);
        gasto.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
        gasto.setFormaDePago(formaDePagoService.getFormasDePagoPorId(idFormaDePago));
        Integer idUsuario = (int) authService.getClaimsDelToken(authorizationHeader).get("idUsuario");
        gasto.setUsuario(usuarioService.getUsuarioPorId(idUsuario.longValue()));
        return gastoService.guardar(gasto);
    }

}
