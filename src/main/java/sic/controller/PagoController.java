package sic.controller;

import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.dto.NuevoPagoMercadoPagoDTO;
import sic.service.IMercadoPagoService;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IMercadoPagoService pagoMercadoPagoService;

  @Autowired
  public PagoController(IMercadoPagoService pagoMercadoPagoService) {
    this.pagoMercadoPagoService = pagoMercadoPagoService;
  }

  @PostMapping("/pagos/mercado-pago")
  public String crearPago(@RequestBody NuevoPagoMercadoPagoDTO nuevoPagoMercadoPagoDTO) {
    String idPago = null;
    try {
      idPago = pagoMercadoPagoService.crearNuevoPago(nuevoPagoMercadoPagoDTO);
    } catch (MPException ex) {
      pagoMercadoPagoService.logExceptionMercadoPago(ex);
    }
    return idPago;
  }

  @PostMapping("/pagos/notificacion")
  public void crearComprobantePorNotificacion(
      @RequestParam(name = "data.id") String id, @RequestParam String type) {
    if (type.equals("payment")) {
      pagoMercadoPagoService.crearComprobantePorNotificacion(id);
    }
  }
}
