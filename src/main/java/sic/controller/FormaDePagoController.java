package sic.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.FormaDePago;
import sic.modelo.Rol;
import sic.service.IFormaDePagoService;

@RestController
public class FormaDePagoController {

  private final IFormaDePagoService formaDePagoService;

  @Autowired
  public FormaDePagoController(IFormaDePagoService formaDePagoService) {
    this.formaDePagoService = formaDePagoService;
  }

  @GetMapping("/api/v1/formas-de-pago/{idFormaDePago}")
  public FormaDePago getFormaDePagoPorId(@PathVariable long idFormaDePago) {
    return formaDePagoService.getFormasDePagoPorId(idFormaDePago);
  }

  @GetMapping("/api/v1/formas-de-pago/predeterminada")
  public FormaDePago getFormaDePagoPredeterminada() {
    return formaDePagoService.getFormaDePagoPredeterminada();
  }

  @GetMapping("/api/v1/formas-de-pago")
  public List<FormaDePago> getFormasDePago() {
    return formaDePagoService.getFormasDePagoNoEliminadas();
  }

  @PutMapping("/api/v1/formas-de-pago/predeterminada/{idFormaDePago}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void setFormaDePagoPredeterminada(@PathVariable long idFormaDePago) {
    formaDePagoService.setFormaDePagoPredeterminada(
        formaDePagoService.getFormasDePagoNoEliminadoPorId(idFormaDePago));
  }
}
