package sic.controller;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.domain.Resultados;
import sic.domain.Rol;
import sic.domain.TipoDeComprobante;
import sic.entity.*;
import sic.dto.NuevosResultadosComprobanteDTO;
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
  public Factura getFacturaPorId(@PathVariable long idFactura) {
    return facturaService.getFacturaNoEliminadaPorId(idFactura);
  }

  @GetMapping("/facturas/{idFactura}/renglones")
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
  public TipoDeComprobante[] getTiposFacturaSegunSucursal(@PathVariable long idSucursal) {
    return facturaService.getTiposDeComprobanteSegunSucursal(
        sucursalService.getSucursalPorId(idSucursal));
  }

  @PostMapping("/facturas/calculo-factura")
  public Resultados calcularResultadosFactura(
      @RequestBody NuevosResultadosComprobanteDTO nuevosResultadosComprobanteDTO) {
    Resultados nuevoResultado =
        facturaService.calcularResultadosFactura(nuevosResultadosComprobanteDTO);
    if (nuevosResultadosComprobanteDTO.getTipoDeComprobante() == TipoDeComprobante.FACTURA_B
        || nuevosResultadosComprobanteDTO.getTipoDeComprobante() == TipoDeComprobante.PRESUPUESTO) {
      nuevoResultado.setSubTotalBruto(
          nuevoResultado
              .getSubTotalBruto()
              .add(nuevoResultado.getIva21Neto().add(nuevoResultado.getIva105Neto())));
      nuevoResultado.setIva21Neto(BigDecimal.ZERO);
      nuevoResultado.setIva105Neto(BigDecimal.ZERO);
    }
    return nuevoResultado;
  }
}
