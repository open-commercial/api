package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.Gasto;
import org.opencommercial.model.Rol;
import org.opencommercial.model.criteria.BusquedaGastoCriteria;
import org.opencommercial.model.dto.NuevoGastoDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
public class GastoController {

  private final GastoService gastoService;
  private final SucursalService sucursalService;
  private final FormaDePagoService formaDePagoService;
  private final UsuarioService usuarioService;
  private final AuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public GastoController(GastoService gastoService,
                         SucursalService sucursalService,
                         FormaDePagoService formaDePagoService,
                         UsuarioService usuarioService,
                         AuthService authService) {
    this.gastoService = gastoService;
    this.sucursalService = sucursalService;
    this.formaDePagoService = formaDePagoService;
    this.usuarioService = usuarioService;
    this.authService = authService;
  }

  @GetMapping("/api/v1/gastos/{idGasto}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Gasto getGastoPorId(@PathVariable long idGasto) {
    return gastoService.getGastoNoEliminadoPorId(idGasto);
  }

  @PostMapping("/api/v1/gastos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Gasto> buscarConCriteria(@RequestBody BusquedaGastoCriteria criteria) {
    return gastoService.buscarGastos(criteria);
  }

  @PostMapping("/api/v1/gastos/total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getTotalGastos(@RequestBody BusquedaGastoCriteria criteria) {
    return gastoService.getTotalGastos(criteria);
  }

  @DeleteMapping("/api/v1/gastos/{idGasto}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminar(@PathVariable long idGasto) {
    gastoService.eliminar(idGasto);
  }

  @PostMapping("/api/v1/gastos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Gasto guardar(@RequestBody NuevoGastoDTO nuevoGastoDTO,
                       @RequestHeader(name = "Authorization") String authorizationHeader) {
    Gasto gasto = new Gasto();
    gasto.setSucursal(sucursalService.getSucursalPorId(nuevoGastoDTO.getIdSucursal()));
    gasto.setFormaDePago(formaDePagoService.getFormasDePagoNoEliminadoPorId(nuevoGastoDTO.getIdFormaDePago()));
    gasto.setConcepto(nuevoGastoDTO.getConcepto());
    gasto.setMonto(nuevoGastoDTO.getMonto());
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    gasto.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn));
    gasto.setFecha(LocalDateTime.now());
    return gastoService.guardar(gasto);
  }
}
