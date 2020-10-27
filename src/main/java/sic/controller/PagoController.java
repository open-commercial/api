package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.service.IAuthService;
import sic.service.IMercadoPagoService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IMercadoPagoService pagoMercadoPagoService;
  private final IAuthService authService;

  @Autowired
  public PagoController(IMercadoPagoService pagoMercadoPagoService,
                        IAuthService authService) {
    this.pagoMercadoPagoService = pagoMercadoPagoService;
    this.authService = authService;
  }

  @PostMapping("/pagos/mercado-pago/notificacion")
  public void crearComprobantePorNotificacion(
      @RequestParam(name = "data.id") String id, @RequestParam String type) {
    if (type.equals("payment")) {
      pagoMercadoPagoService.crearComprobantePorNotificacion(id);
    }
  }

  @PostMapping("/pagos/mercado-pago/preference")
  public MercadoPagoPreferenceDTO getPreferenceSegunItemsDelUsuario(
      HttpServletRequest request, @RequestBody NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO) {
    Claims claims = authService.getClaimsDelJWT(request.getHeader("Authorization"));
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    return pagoMercadoPagoService.crearNuevaPreference(
        idUsuarioLoggedIn, nuevaOrdenDePagoDTO, origin);
  }
}
