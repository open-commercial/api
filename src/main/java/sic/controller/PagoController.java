package sic.controller;

import com.mercadopago.exceptions.MPException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.service.IAuthService;
import sic.service.IMercadoPagoService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IMercadoPagoService pagoMercadoPagoService;
  private IUsuarioService usuarioService;
  private IAuthService authService;

  @Autowired
  public PagoController(IMercadoPagoService pagoMercadoPagoService,
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
    try {
      pagoMercadoPagoService.crearNuevoPago(
          nuevoPagoMercadoPagoDTO,
          usuarioService.getUsuarioNoEliminadoPorId(
              ((Integer) claims.get("idUsuario")).longValue()));
    } catch (MPException ex) {
      pagoMercadoPagoService.logExceptionMercadoPago(ex);
    }
  }

  @PostMapping("/pagos/notificacion")
  public void crearComprobantePorNotificacion(
      @RequestParam(name = "data.id") String id, @RequestParam String type) {
    if (type.equals("payment")) {
      pagoMercadoPagoService.crearComprobantePorNotificacion(id);
    }
  }
}
