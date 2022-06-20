package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.service.IAuthService;
import sic.service.IPagoService;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IPagoService pagoService;
  private final IAuthService authService;

  @Autowired
  public PagoController(IPagoService pagoService,
                        IAuthService authService) {
    this.pagoService = pagoService;
    this.authService = authService;
  }

  @PostMapping("/pagos/mercado-pago/notificacion")
  public void crearComprobantePorNotificacion(
      @RequestParam(name = "data.id") String id, @RequestParam String type) {
    if (type.equals("payment")) {
      pagoService.crearComprobantePorNotificacion(id);
    }
  }

  @PostMapping("/pagos/mercado-pago/preference")
  public MercadoPagoPreferenceDTO getPreferenceSegunItemsDelUsuario(
      HttpServletRequest request, @RequestBody NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO) {
    Claims claims = authService.getClaimsDelToken(request.getHeader("Authorization"));
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    var preferenceParams = pagoService.getNuevaPreferenceParams(
            idUsuarioLoggedIn, nuevaOrdenDePagoDTO, origin);
    return new MercadoPagoPreferenceDTO(preferenceParams.get(0), preferenceParams.get(1));
  }
}
