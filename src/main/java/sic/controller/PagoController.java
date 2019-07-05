package sic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Recibo;
import sic.modelo.Rol;
import sic.modelo.dto.PagoMercadoPagoDTO;
import sic.service.IPagoMercadoPagoService;

@RestController
@RequestMapping("/api/v1")
public class PagoController {

  private final IPagoMercadoPagoService pagoMercadoPagoService;

  @Autowired
  public PagoController(IPagoMercadoPagoService pagoMercadoPagoService) {
    this.pagoMercadoPagoService = pagoMercadoPagoService;
  }

  @PostMapping("/pagos/mercado-pago")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Recibo agregarOrModificarItem(@RequestBody PagoMercadoPagoDTO pagoMercadoPagoDTO) {
    return pagoMercadoPagoService.crearNuevoPago(pagoMercadoPagoDTO);
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
}
