package sic.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDePagoDTO;
import sic.service.AuthService;
import sic.service.PaymentService;

@RestController
public class PagoController {

  private final PaymentService paymentService;
  private final AuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public PagoController(PaymentService paymentService,
                        AuthService authService) {
    this.paymentService = paymentService;
    this.authService = authService;
  }

  @PostMapping("/api/v1/pagos/mercado-pago/notificacion")
  public void crearComprobantePorNotificacion(@RequestParam long id,
                                              @RequestParam String topic) {
    if (topic.equals("payment")) {
      paymentService.crearComprobantePorNotificacion(id);
    }
  }

  @PostMapping("/api/v1/pagos/mercado-pago/preference")
  public MercadoPagoPreferenceDTO getPreferenceSegunItemsDelUsuario(
      HttpServletRequest request,
      @RequestBody NuevaOrdenDePagoDTO nuevaOrdenDePagoDTO) {
    Claims claims = authService.getClaimsDelToken(request.getHeader("Authorization"));
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    var preferenceParams = paymentService.getNuevaPreferenceParams(idUsuarioLoggedIn, nuevaOrdenDePagoDTO, origin);
    return new MercadoPagoPreferenceDTO(preferenceParams.get(0), preferenceParams.get(1));
  }
}
