package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.entity.criteria.BusquedaGastoCriteria;
import sic.entity.Gasto;
import sic.domain.Rol;
import sic.dto.NuevoGastoDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
public class GastoController {
    
    private final IGastoService gastoService;
    private final ISucursalService sucursalService;
    private final IFormaDePagoService formaDePagoService;
    private final IUsuarioService usuarioService;
    private final IAuthService authService;
    
    @Autowired
    public GastoController(IGastoService gastoService, ISucursalService sucursalService,
                           IFormaDePagoService formaDePagoService, IUsuarioService usuarioService,
                           IAuthService authService) {
        this.gastoService = gastoService;
        this.sucursalService = sucursalService;
        this.formaDePagoService = formaDePagoService;
        this.usuarioService = usuarioService;
        this.authService = authService;
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
      @RequestBody NuevoGastoDTO nuevoGastoDTO,
      @RequestHeader(name = "Authorization") String authorizationHeader) {
    Gasto gasto = new Gasto();
    gasto.setSucursal(sucursalService.getSucursalPorId(nuevoGastoDTO.getIdSucursal()));
    gasto.setFormaDePago(formaDePagoService.getFormasDePagoNoEliminadoPorId(nuevoGastoDTO.getIdFormaDePago()));
    gasto.setConcepto(nuevoGastoDTO.getConcepto());
    gasto.setMonto(nuevoGastoDTO.getMonto());
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    gasto.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn));
    gasto.setFecha(LocalDateTime.now());
    return gastoService.guardar(gasto);
  }
}
