package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.dto.NotificacionMercadoPagoDTO;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.IAuthService;
import sic.service.IPagoMercadoPagoService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IPagoMercadoPagoService pagoMercadoPagoService;
  private IUsuarioService usuarioService;
  private IAuthService authService;

  @Autowired
  public PagoController(IPagoMercadoPagoService pagoMercadoPagoService,
                        IAuthService authService,
                        IUsuarioService usuarioService) {
    this.pagoMercadoPagoService = pagoMercadoPagoService;
    this.authService = authService;
    this.usuarioService = usuarioService;
  }

  @PostMapping("/pagos/mercado-pago")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public void crearPago(
      @RequestBody NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    pagoMercadoPagoService.crearNuevoRecibo(
        nuevoPagoMercadoPagoDTO,
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
  }

  @GetMapping("/pagos/mercado-pago/{idPagoMercadoPago}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public PagoMercadoPagoDTO recuperarPago(@PathVariable String idPagoMercadoPago) {
    return pagoMercadoPagoService.recuperarPago(idPagoMercadoPago);
  }

  @PutMapping("/pagos/mercado-pago/{idPagoMercadoPago}/devolucion")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public NuevoPagoMercadoPagoDTO devolverPago(
      @PathVariable String idPagoMercadoPago,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return pagoMercadoPagoService.devolverPago(
        idPagoMercadoPago,
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
  }

  @PostMapping("/pagos/notificacion")
  public void crearReciboPorNotificacion(@RequestBody NotificacionMercadoPagoDTO notificacion) {
    if (notificacion.getType().equals("payment")) {
      pagoMercadoPagoService.crearReciboPorNotificacion(notificacion.getData().getId());
    }
  }
}
