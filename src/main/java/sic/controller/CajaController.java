package sic.controller;

import java.math.BigDecimal;
import java.util.*;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class CajaController {

  private final ICajaService cajaService;
  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;
  private final IFormaDePagoService formaDePagoService;
  private final IAuthService authService;

  @Autowired
  public CajaController(
      ICajaService cajaService,
      IEmpresaService empresaService,
      IFormaDePagoService formaDePagoService,
      IUsuarioService usuarioService,
      IAuthService authService) {
    this.cajaService = cajaService;
    this.empresaService = empresaService;
    this.formaDePagoService = formaDePagoService;
    this.usuarioService = usuarioService;
    this.authService = authService;
  }

  @GetMapping("/cajas/{idCaja}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Caja getCajaPorId(@PathVariable long idCaja) {
    return cajaService.getCajaPorId(idCaja);
  }

  @PostMapping("/cajas/apertura/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Caja abrirCaja(
      @PathVariable long idEmpresa,
      @RequestParam BigDecimal saldoApertura,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    return cajaService.abrirCaja(
        empresaService.getEmpresaPorId(idEmpresa),
        usuarioService.getUsuarioNoEliminadoPorId(idUsuarioLoggedIn),
        saldoApertura);
  }

  @DeleteMapping("/cajas/{idCaja}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idCaja) {
    cajaService.eliminar(idCaja);
  }

  @PutMapping("/cajas/{idCaja}/cierre")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Caja cerrarCaja(
      @PathVariable long idCaja,
      @RequestParam BigDecimal monto,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    return cajaService.cerrarCaja(idCaja, monto, idUsuarioLoggedIn, false);
  }

  @PostMapping("/cajas/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<Caja> getCajasCriteria(@RequestBody BusquedaCajaCriteria criteria) {
    return cajaService.buscarCajas(criteria);
  }

  @GetMapping("/cajas/{idCaja}/movimientos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<MovimientoCaja> getMovimientosDeCaja(
      @PathVariable long idCaja, @RequestParam long idFormaDePago) {
    Caja caja = cajaService.getCajaPorId(idCaja);
    Date fechaHasta = new Date();
    if (caja.getFechaCierre() != null) fechaHasta = caja.getFechaCierre();
    return cajaService.getMovimientosPorFormaDePagoEntreFechas(
        caja.getEmpresa(),
        formaDePagoService.getFormasDePagoPorId(idFormaDePago),
        caja.getFechaApertura(),
        fechaHasta);
  }

  @GetMapping("/cajas/{idCaja}/saldo-afecta-caja")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoQueAfectaCaja(@PathVariable long idCaja) {
    return cajaService.getSaldoQueAfectaCaja(cajaService.getCajaPorId(idCaja));
  }

  @GetMapping("/cajas/{idCaja}/saldo-sistema")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoSistema(@PathVariable long idCaja) {
    return cajaService.getSaldoSistema(cajaService.getCajaPorId(idCaja));
  }

  @GetMapping("/cajas/empresas/{idEmpresa}/ultima-caja-abierta")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public boolean getEstadoUltimaCaja(@PathVariable long idEmpresa) {
    return cajaService.isUltimaCajaAbierta(idEmpresa);
  }

  @PostMapping("/cajas/saldo-sistema")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoSistemaCajas(@RequestBody BusquedaCajaCriteria criteria) {
    return cajaService.getSaldoSistemaCajas(criteria);
  }

  @PostMapping("/cajas/saldo-real")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal getSaldoRealCajas(@RequestBody BusquedaCajaCriteria criteria) {
    return cajaService.getSaldoRealCajas(criteria);
  }

  @GetMapping("/cajas/{idCaja}/totales-formas-de-pago")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Map<Long, BigDecimal> getTotalesPorFormaDePago(@PathVariable long idCaja) {
    return cajaService.getTotalesDeFormaDePago(idCaja);
  }

  @PutMapping("/cajas/{idCaja}/reapertura")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void reabrirCaja(@PathVariable long idCaja, @RequestParam BigDecimal monto) {
    cajaService.reabrirCaja(idCaja, monto);
  }
}
