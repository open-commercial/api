package sic.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.calculos.NuevosResultadosComprobanteDTO;
import sic.modelo.calculos.Resultados;
import sic.service.*;

@RestController
@RequestMapping("/api/v1")
public class FacturaController {

  private final IFacturaService facturaService;
  private final ISucursalService sucursalService;

  @Autowired
  public FacturaController(IFacturaService facturaService, ISucursalService sucursalService) {
    this.facturaService = facturaService;
    this.sucursalService = sucursalService;
  }

  @GetMapping("/facturas/{idFactura}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Factura getFacturaPorId(@PathVariable long idFactura) {
    return facturaService.getFacturaNoEliminadaPorId(idFactura);
  }

  @DeleteMapping("/facturas/{idFactura}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idFactura) {
    facturaService.eliminarFactura(idFactura);
  }

  @GetMapping("/facturas/{idFactura}/renglones")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public List<RenglonFactura> getRenglonesDeLaFactura(@PathVariable long idFactura) {
    return facturaService.getRenglonesDeLaFactura(idFactura);
  }

  @GetMapping("/facturas/{idFactura}/renglones/notas/credito")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(
      @PathVariable long idFactura) {
    return facturaService.getRenglonesDeLaFacturaModificadosParaCredito(idFactura);
  }

  @GetMapping("/facturas/tipos/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public TipoDeComprobante[] getTiposFacturaSegunSucursal(@PathVariable long idSucursal) {
    return facturaService.getTiposFacturaSegunSucursal(
        sucursalService.getSucursalPorId(idSucursal));
  }

  @PostMapping("/facturas/calculo-factura")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Resultados calcularResultadosFactura(
      @RequestBody NuevosResultadosComprobanteDTO nuevosResultadosComprobanteDTO) {
    return facturaService.calcularResultadosFactura(nuevosResultadosComprobanteDTO);
  }
}
