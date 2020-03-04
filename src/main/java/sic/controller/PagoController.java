package sic.controller;

import com.mercadopago.exceptions.MPException;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.dto.MercadoPagoPreferenceDTO;
import sic.modelo.dto.NuevaOrdenDeCompraDTO;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.service.IAuthService;
import sic.service.ICarritoCompraService;
import sic.service.IMercadoPagoService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IMercadoPagoService pagoMercadoPagoService;
  private final ICarritoCompraService carritoCompraService;
  private final IAuthService authService;

  @Autowired
  public PagoController(IMercadoPagoService pagoMercadoPagoService,
                        ICarritoCompraService carritoCompraService,
                        IAuthService authService) {
    this.pagoMercadoPagoService = pagoMercadoPagoService;
    this.carritoCompraService = carritoCompraService;
    this.authService = authService;
  }

  // @GetMapping("/pagos/mercado-pago/preference")

  @PostMapping("/pagos/mercado-pago")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public String crearPago(
      @RequestBody NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    String idPago = null;
    try {
      idPago = pagoMercadoPagoService.crearNuevoPago(nuevoPagoMercadoPagoDTO, idUsuarioLoggedIn);
    } catch (MPException ex) {
      pagoMercadoPagoService.logExceptionMercadoPago(ex);
    }
    return idPago;
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
      HttpServletRequest request, @RequestBody NuevaOrdenDeCompraDTO nuevaOrdenDeCompra) {
    Claims claims = authService.getClaimsDelToken(request.getHeader("Authorization"));
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    return pagoMercadoPagoService.crearNuevaPreferencia(
        "Producto",
        1,
        carritoCompraService.calcularTotal(idUsuarioLoggedIn).floatValue(),
        idUsuarioLoggedIn,
        nuevaOrdenDeCompra,
        origin);
  }
}
