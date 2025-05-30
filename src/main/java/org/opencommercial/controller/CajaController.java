package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.Caja;
import org.opencommercial.model.MovimientoCaja;
import org.opencommercial.model.Rol;
import org.opencommercial.model.criteria.BusquedaCajaCriteria;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class CajaController {

  private final CajaService cajaService;
  private final SucursalService sucursalService;
  private final UsuarioService usuarioService;
  private final FormaDePagoService formaDePagoService;
  private final AuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public CajaController(CajaService cajaService,
                        SucursalService sucursalService,
                        FormaDePagoService formaDePagoService,
                        UsuarioService usuarioService,
                        AuthService authService) {
    this.cajaService = cajaService;
    this.sucursalService = sucursalService;
    this.formaDePagoService = formaDePagoService;
    this.usuarioService = usuarioService;
    this.authService = authService;
  }

  @GetMapping("/api/v1/cajas/{idCaja}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Caja getCajaPorId(@PathVariable long idCaja) {
    return cajaService.getCajaPorId(idCaja);
  }

  @PostMapping("/api/v1/cajas/apertura/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Caja abrirCaja(@PathVariable long idSucursal,
                        @RequestParam BigDecimal saldoApertura,
                        @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (claims.get(CLAIM_ID_USUARIO, Long.class));
    return cajaService.abrirCaja(
        sucursalService.getSucursalPorId(idSucursal),
        usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn),
        saldoApertura);
  }

  @DeleteMapping("/api/v1/cajas/{idCaja}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idCaja) {
    cajaService.eliminar(idCaja);
  }

  @PutMapping("/api/v1/cajas/{idCaja}/cierre")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Caja cerrarCaja(@PathVariable long idCaja,
                         @RequestParam BigDecimal monto,
                         @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return cajaService.cerrarCaja(idCaja, monto, idUsuarioLoggedIn, false);
  }

  @PostMapping("/api/v1/cajas/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Caja> getCajasCriteria(@RequestBody BusquedaCajaCriteria criteria) {
    return cajaService.buscarCajas(criteria);
  }

  @PostMapping("/api/v1/cajas/saldo-sistema")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoSistemaCajas(@RequestBody BusquedaCajaCriteria criteria) {
    return cajaService.getSaldoSistemaCajas(criteria);
  }

  @PostMapping("/api/v1/cajas/saldo-real")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoRealCajas(@RequestBody BusquedaCajaCriteria criteria) {
    return cajaService.getSaldoRealCajas(criteria);
  }

  @GetMapping("/api/v1/cajas/{idCaja}/movimientos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<MovimientoCaja> getMovimientosDeCaja(@PathVariable long idCaja,
                                                   @RequestParam long idFormaDePago) {
    Caja caja = cajaService.getCajaPorId(idCaja);
    LocalDateTime fechaHasta = LocalDateTime.now();
    if (caja.getFechaCierre() != null) fechaHasta = caja.getFechaCierre();
    return cajaService.getMovimientosPorFormaDePagoEntreFechas(
        caja.getSucursal(),
        formaDePagoService.getFormasDePagoPorId(idFormaDePago),
        caja.getFechaApertura(),
        fechaHasta);
  }

  @GetMapping("/api/v1/cajas/{idCaja}/saldo-afecta-caja")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoQueAfectaCaja(@PathVariable long idCaja) {
    return cajaService.getSaldoQueAfectaCaja(cajaService.getCajaPorId(idCaja));
  }

  @GetMapping("/api/v1/cajas/{idCaja}/saldo-sistema")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoSistema(@PathVariable long idCaja) {
    return cajaService.getSaldoSistema(cajaService.getCajaPorId(idCaja));
  }

  @GetMapping("/api/v1/cajas/sucursales/{idSucursal}/ultima-caja-abierta")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public boolean getEstadoUltimaCaja(@PathVariable long idSucursal) {
    return cajaService.isUltimaCajaAbierta(idSucursal);
  }

  @GetMapping("/api/v1/cajas/{idCaja}/totales-formas-de-pago")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Map<Long, BigDecimal> getIdsFormasDePagoAndMontos(@PathVariable long idCaja) {
    return cajaService.getIdsFormasDePagoAndMontos(idCaja);
  }

  @PutMapping("/api/v1/cajas/{idCaja}/reapertura")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void reabrirCaja(@PathVariable long idCaja, @RequestParam BigDecimal monto) {
    cajaService.reabrirCaja(idCaja, monto);
  }
}
