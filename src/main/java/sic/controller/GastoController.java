package sic.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.criteria.BusquedaGastoCriteria;
import sic.modelo.Gasto;
import sic.modelo.Rol;
import sic.modelo.dto.GastoDTO;
import sic.service.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
public class GastoController {
    
    private final IGastoService gastoService;
    private final ISucursalService sucursalService;
    private final IFormaDePagoService formaDePagoService;
    private final IUsuarioService usuarioService;
    private final IAuthService authService;
    private final ModelMapper modelMapper;
    
    @Autowired
    public GastoController(IGastoService gastoService, ModelMapper modelMapper,
                           ISucursalService sucursalService, IFormaDePagoService formaDePagoService,
                           IUsuarioService usuarioService, IAuthService authService) {
        this.gastoService = gastoService;
        this.sucursalService = sucursalService;
        this.formaDePagoService = formaDePagoService;
        this.usuarioService = usuarioService;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }
    
    @GetMapping("/gastos/{idGasto}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
    public Gasto getGastoPorId(@PathVariable long idGasto) {
        return gastoService.getGastoNoEliminadoPorId(idGasto);
    }

  @PostMapping("/gastos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Gasto> buscarConCriteria(@RequestBody BusquedaGastoCriteria criteria) {
    return gastoService.buscarGastos(criteria);
  }

  @PostMapping("/gastos/total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getTotalGastos(@RequestBody BusquedaGastoCriteria criteria) {
    return gastoService.getTotalGastos(criteria);
  }

  @DeleteMapping("/gastos/{idGasto}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminar(@PathVariable long idGasto) {
    gastoService.eliminar(idGasto);
  }

  @PostMapping("/gastos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Gasto guardar(
      @RequestBody GastoDTO gastoDTO,
      @RequestParam Long idSucursal,
      @RequestParam Long idFormaDePago,
      @RequestHeader(name = "Authorization") String authorizationHeader) {
    Gasto gasto = modelMapper.map(gastoDTO, Gasto.class);
    gasto.setSucursal(sucursalService.getSucursalPorId(idSucursal));
    gasto.setFormaDePago(formaDePagoService.getFormasDePagoNoEliminadoPorId(idFormaDePago));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    gasto.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn));
    return gastoService.guardar(gasto);
  }
}
