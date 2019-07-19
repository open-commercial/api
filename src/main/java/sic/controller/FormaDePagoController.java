package sic.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.FormaDePago;
import sic.modelo.Rol;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;

@RestController
@RequestMapping("/api/v1")
public class FormaDePagoController {

  private final IFormaDePagoService formaDePagoService;
  private final IEmpresaService empresaService;

  @Autowired
  public FormaDePagoController(
      IFormaDePagoService formaDePagoService,
      IEmpresaService empresaService) {
    this.formaDePagoService = formaDePagoService;
    this.empresaService = empresaService;
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

  @GetMapping("/formas-de-pago/predeterminada/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public FormaDePago getFormaDePagoPredeterminada(@PathVariable long idEmpresa) {
    return formaDePagoService.getFormaDePagoPredeterminada(
        empresaService.getEmpresaPorId(idEmpresa));
  }

  @GetMapping("/formas-de-pago/empresas/{idEmpresa}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<FormaDePago> getFormasDePago(@PathVariable long idEmpresa) {
    return formaDePagoService.getFormasDePagoNoEliminadas(empresaService.getEmpresaPorId(idEmpresa));
  }

  @PutMapping("/formas-de-pago/predeterminada/{idFormaDePago}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void setFormaDePagoPredeterminada(@PathVariable long idFormaDePago) {
    formaDePagoService.setFormaDePagoPredeterminada(
        formaDePagoService.getFormasDePagoNoEliminadoPorId(idFormaDePago));
  }
}
