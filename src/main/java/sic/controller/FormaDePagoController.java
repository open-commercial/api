package sic.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.FormaDePago;
import sic.modelo.Rol;
import sic.modelo.dto.FormaDePagoDTO;
import sic.service.IEmpresaService;
import sic.service.IFormaDePagoService;

@RestController
@RequestMapping("/api/v1")
public class FormaDePagoController {

  private final IFormaDePagoService formaDePagoService;
  private final IEmpresaService empresaService;
  private final ModelMapper modelMapper;

  @Autowired
  public FormaDePagoController(
      IFormaDePagoService formaDePagoService,
      IEmpresaService empresaService,
      ModelMapper modelMapper) {
    this.formaDePagoService = formaDePagoService;
    this.empresaService = empresaService;
    this.modelMapper = modelMapper;
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
    return formaDePagoService.getFormasDePagoNoEliminadoPorId(idFormaDePago);
  }

  @DeleteMapping("/formas-de-pago/{idFormaDePago}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idFormaDePago) {
    formaDePagoService.eliminar(idFormaDePago);
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

  @PostMapping("/formas-de-pago")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public FormaDePago guardar(@RequestBody FormaDePagoDTO formaDePagoDTO, @RequestParam Long idEmpresa) {
    FormaDePago formaDePago = modelMapper.map(formaDePagoDTO, FormaDePago.class);
    formaDePago.setEmpresa(empresaService.getEmpresaPorId(idEmpresa));
    return formaDePagoService.guardar(formaDePago);
  }

  @PutMapping("/formas-de-pago/predeterminada/{idFormaDePago}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void setFormaDePagoPredeterminada(@PathVariable long idFormaDePago) {
    formaDePagoService.setFormaDePagoPredeterminada(
        formaDePagoService.getFormasDePagoNoEliminadoPorId(idFormaDePago));
  }
}
