package sic.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.FormaDePago;
import sic.modelo.Rol;
import sic.service.ISucursalService;
import sic.service.IFormaDePagoService;

@RestController
@RequestMapping("/api/v1")
public class FormaDePagoController {

  private final IFormaDePagoService formaDePagoService;
  private final ISucursalService sucursalService;

  @Autowired
  public FormaDePagoController(
      IFormaDePagoService formaDePagoService,
      ISucursalService sucursalService) {
    this.formaDePagoService = formaDePagoService;
    this.sucursalService = sucursalService;
  }

  @GetMapping("/formas-de-pago/{idFormaDePago}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public FormaDePago getFormaDePagoPorId(@PathVariable long idFormaDePago) {
    return formaDePagoService.getFormasDePagoPorId(idFormaDePago);
  }

  @GetMapping("/formas-de-pago/predeterminada/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public FormaDePago getFormaDePagoPredeterminada(@PathVariable long idSucursal) {
    return formaDePagoService.getFormaDePagoPredeterminada(
        sucursalService.getSucursalPorId(idSucursal));
  }

  @GetMapping("/formas-de-pago/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<FormaDePago> getFormasDePago(@PathVariable long idSucursal) {
    return formaDePagoService.getFormasDePagoNoEliminadas(sucursalService.getSucursalPorId(idSucursal));
  }

  @PutMapping("/formas-de-pago/predeterminada/{idFormaDePago}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void setFormaDePagoPredeterminada(@PathVariable long idFormaDePago) {
    formaDePagoService.setFormaDePagoPredeterminada(
        formaDePagoService.getFormasDePagoNoEliminadoPorId(idFormaDePago));
  }
}
