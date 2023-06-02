package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.service.IAuthService;
import sic.service.IPaymentService;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IPaymentService paymentService;
  private final IAuthService authService;

  @Autowired
  public PagoController(IPaymentService paymentService,
                        IAuthService authService) {
    this.paymentService = paymentService;
    this.authService = authService;
  }

  @PostMapping("/pagos/mercado-pago/notificacion")
  public void crearComprobantePorNotificacion(@RequestParam long id, @RequestParam String topic) {
    if (topic.equals("payment")) {
      paymentService.crearComprobantePorNotificacion(id);
    }
  }

  @PostMapping("/pagos/mercado-pago/preference")
  public MercadoPagoPreferenceDTO getPreferenceSegunItemsDelUsuario(
      HttpServletRequest request, @RequestBody NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO) {
    Claims claims = authService.getClaimsDelToken(request.getHeader("Authorization"));
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    var preferenceParams = paymentService.getNuevaPreferenceParams(idUsuarioLoggedIn, nuevaOrdenDePagoDTO, origin);
    return new MercadoPagoPreferenceDTO(preferenceParams.get(0), preferenceParams.get(1));
  }
}
