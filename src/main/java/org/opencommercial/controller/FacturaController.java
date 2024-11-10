package org.opencommercial.controller;

import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.*;
import org.opencommercial.model.dto.NuevosResultadosComprobanteDTO;
import org.opencommercial.service.FacturaService;
import org.opencommercial.service.SucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class FacturaController {

  private final FacturaService facturaService;
  private final SucursalService sucursalService;

  @Autowired
  public FacturaController(FacturaService facturaService,
                           SucursalService sucursalService) {
    this.facturaService = facturaService;
    this.sucursalService = sucursalService;
  }

  @GetMapping("/api/v1/facturas/{idFactura}")
  public Factura getFacturaPorId(@PathVariable long idFactura) {
    return facturaService.getFacturaNoEliminadaPorId(idFactura);
  }

  @GetMapping("/api/v1/facturas/{idFactura}/renglones")
  public List<RenglonFactura> getRenglonesDeLaFactura(@PathVariable long idFactura) {
    return facturaService.getRenglonesDeLaFactura(idFactura);
  }

  @GetMapping("/api/v1/facturas/{idFactura}/renglones/notas/credito")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(@PathVariable long idFactura) {
    return facturaService.getRenglonesDeLaFacturaModificadosParaCredito(idFactura);
  }

  @GetMapping("/api/v1/facturas/tipos/sucursales/{idSucursal}")
  public TipoDeComprobante[] getTiposFacturaSegunSucursal(@PathVariable long idSucursal) {
    return facturaService.getTiposDeComprobanteSegunSucursal(
        sucursalService.getSucursalPorId(idSucursal));
  }

  @PostMapping("/api/v1/facturas/calculo-factura")
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
