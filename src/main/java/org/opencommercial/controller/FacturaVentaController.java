package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaFacturaVentaCriteria;
import org.opencommercial.model.dto.NuevaFacturaVentaDTO;
import org.opencommercial.model.dto.NuevoRenglonFacturaDTO;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.FacturaService;
import org.opencommercial.service.FacturaVentaService;
import org.opencommercial.service.ReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
public class FacturaVentaController {

  private final FacturaVentaService facturaVentaService;
  private final FacturaService facturaService;
  private final ReciboService reciboService;
  private final AuthService authService;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public FacturaVentaController(FacturaVentaService facturaVentaService,
                                FacturaService facturaService,
                                ReciboService reciboService,
                                AuthService authService,
                                MessageSource messageSource) {
    this.facturaVentaService = facturaVentaService;
    this.facturaService = facturaService;
    this.reciboService = reciboService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @GetMapping("/api/v1/facturas/ventas")
  public List<FacturaVenta> getFacturaPorId(@RequestParam long[] idFactura) {
    return facturaVentaService.getFacturasVentaPorId(idFactura);
  }

  @PostMapping("/api/v1/facturas/ventas/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<FacturaVenta> guardarFacturaVenta(@RequestBody NuevaFacturaVentaDTO nuevaFacturaVentaDTO,
                                                @PathVariable Long idPedido,
                                                @RequestHeader("Authorization") String authorizationHeader) {
    List<TipoDeComprobante> tiposDeFacturaPermitidos =
        Arrays.asList(
            TipoDeComprobante.FACTURA_A,
            TipoDeComprobante.FACTURA_B,
            TipoDeComprobante.FACTURA_C,
            TipoDeComprobante.FACTURA_X,
            TipoDeComprobante.PRESUPUESTO);
    if (!tiposDeFacturaPermitidos.contains(nuevaFacturaVentaDTO.getTipoDeComprobante())) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
    }
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    FacturaVenta fv = facturaVentaService.construirFacturaVenta(nuevaFacturaVentaDTO,
                                                                idPedido,
                                                                claims.get(CLAIM_ID_USUARIO, Long.class));
    List<FacturaVenta> facturasGuardadas;
    if (nuevaFacturaVentaDTO.getIndices() != null
        && nuevaFacturaVentaDTO.getIndices().length > 0
        && (fv.getTipoComprobante() == TipoDeComprobante.FACTURA_A
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_C)) {
      facturasGuardadas =
          facturaVentaService.guardar(
              facturaVentaService.dividirFactura(fv, nuevaFacturaVentaDTO.getIndices()),
              idPedido,
              reciboService.construirRecibos(
                  nuevaFacturaVentaDTO.getIdsFormaDePago(),
                  nuevaFacturaVentaDTO.getIdSucursal(),
                  fv.getCliente(),
                  fv.getUsuario(),
                  nuevaFacturaVentaDTO.getMontos(),
                  fv.getFecha()));
    } else {
      List<FacturaVenta> facturas = new ArrayList<>();
      facturas.add(fv);
      facturasGuardadas =
          facturaVentaService.guardar(
              facturas,
              idPedido,
              reciboService.construirRecibos(
                  nuevaFacturaVentaDTO.getIdsFormaDePago(),
                  nuevaFacturaVentaDTO.getIdSucursal(),
                  fv.getCliente(),
                  fv.getUsuario(),
                  nuevaFacturaVentaDTO.getMontos(),
                  fv.getFecha()));
    }
    return facturasGuardadas;
  }

  @PostMapping("/api/v1/facturas/ventas/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<FacturaVenta> buscarFacturaVenta(@RequestBody BusquedaFacturaVentaCriteria criteria) {
    return facturaVentaService.buscarFacturaVenta(criteria);
  }

  @GetMapping("/api/v1/facturas/ventas/tipos/sucursales/{idSucursal}/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public TipoDeComprobante[] getTipoFacturaVenta(@PathVariable long idSucursal,
                                                 @PathVariable long idCliente,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuario = claims.get(CLAIM_ID_USUARIO, Long.class);
    return facturaVentaService.getTiposDeComprobanteVenta(idSucursal, idCliente, idUsuario);
  }

  @GetMapping("/api/v1/facturas/ventas/{idFactura}/reporte")
  public ResponseEntity<byte[]> getReporteFacturaVenta(@PathVariable long idFactura) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("content-disposition", "inline; filename=Factura.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF =
        facturaVentaService.getReporteFacturaVenta(
            facturaService.getFacturaNoEliminadaPorId(idFactura));
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }

  @GetMapping("/api/v1/facturas/ventas/renglones/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> getRenglonesPedidoParaFacturar(@PathVariable long idPedido,
                                                             @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaVentaService.getRenglonesPedidoParaFacturar(idPedido, tipoDeComprobante);
  }

  @PostMapping("/api/v1/facturas/ventas/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> calcularRenglonesVenta(
      @RequestBody List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO,
      @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaService.calcularRenglones(tipoDeComprobante, Movimiento.VENTA, nuevosRenglonesFacturaDTO);
  }

  @PostMapping("/api/v1/facturas/ventas/total-facturado/criteria")
  public BigDecimal calcularTotalFacturadoVenta(@RequestBody BusquedaFacturaVentaCriteria criteria) {
    return facturaVentaService.calcularTotalFacturadoVenta(criteria);
  }

  @PostMapping("/api/v1/facturas/ventas/total-iva/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularIvaVenta(@RequestBody BusquedaFacturaVentaCriteria criteria) {
    return facturaVentaService.calcularIvaVenta(criteria);
  }

  @PostMapping("/api/v1/facturas/ventas/ganancia-total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularGananciaTotal(@RequestBody BusquedaFacturaVentaCriteria criteria) {
    return facturaVentaService.calcularGananciaTotal(criteria);
  }

  @GetMapping("/api/v1/facturas/ventas/email/{idFactura}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public void enviarFacturaVentaPorEmail(@PathVariable long idFactura) {
    facturaVentaService.enviarFacturaVentaPorEmail(idFactura);
  }
}
