package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.exception.BusinessServiceException;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.dto.NuevaFacturaVentaDTO;
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class FacturaVentaController {

  private final IFacturaVentaService facturaVentaService;
  private final IFacturaService facturaService;
  private final IReciboService reciboService;
  private final IAuthService authService;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public FacturaVentaController(
      IFacturaVentaService facturaVentaService,
      IFacturaService facturaService,
      IReciboService reciboService,
      IAuthService authService,
      MessageSource messageSource) {
    this.facturaVentaService = facturaVentaService;
    this.facturaService = facturaService;
    this.reciboService = reciboService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @PostMapping("/facturas/ventas/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<FacturaVenta> guardarFacturaVenta(
      @RequestBody NuevaFacturaVentaDTO nuevaFacturaVentaDTO,
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
    FacturaVenta fv =
        facturaVentaService.construirFacuraVenta(
            nuevaFacturaVentaDTO, idPedido, ((Integer) claims.get(CLAIM_ID_USUARIO)).longValue());
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
    List<TipoDeComprobante> tiposAutorizables =
        Arrays.asList(
            TipoDeComprobante.FACTURA_A, TipoDeComprobante.FACTURA_B, TipoDeComprobante.FACTURA_C);
    facturasGuardadas.stream()
        .filter(facturaVenta -> tiposAutorizables.contains(facturaVenta.getTipoComprobante()))
        .forEach(facturaVentaService::autorizarFacturaVenta);
    return facturasGuardadas;
  }

  @PostMapping("/facturas/ventas/{idFactura}/autorizacion")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public FacturaVenta autorizarFactura(@PathVariable long idFactura) {
    return facturaVentaService.autorizarFacturaVenta(
        (FacturaVenta) facturaService.getFacturaNoEliminadaPorId(idFactura));
  }

  @PostMapping("/facturas/ventas/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<FacturaVenta> buscarFacturaVenta(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaVentaService.buscarFacturaVenta(criteria, (int) claims.get(CLAIM_ID_USUARIO));
  }

  @GetMapping("/facturas/ventas/tipos/sucursales/{idSucursal}/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public TipoDeComprobante[] getTipoFacturaVenta(
      @PathVariable long idSucursal,
      @PathVariable long idCliente,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuario = (int) claims.get(CLAIM_ID_USUARIO);
    return facturaVentaService.getTiposDeComprobanteVenta(idSucursal, idCliente, idUsuario);
  }

  @GetMapping("/facturas/ventas/{idFactura}/reporte")
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

  @GetMapping("/facturas/ventas/renglones/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> getRenglonesPedidoParaFacturar(
      @PathVariable long idPedido, @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaVentaService.getRenglonesPedidoParaFacturar(idPedido, tipoDeComprobante);
  }

  @PostMapping("/facturas/ventas/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> calcularRenglonesVenta(
      @RequestBody List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO,
      @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaService.calcularRenglones(
        tipoDeComprobante, Movimiento.VENTA, nuevosRenglonesFacturaDTO);
  }

  @PostMapping("/facturas/ventas/total-facturado/criteria")
  public BigDecimal calcularTotalFacturadoVenta(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaVentaService.calcularTotalFacturadoVenta(
        criteria, (int) claims.get(CLAIM_ID_USUARIO));
  }

  @PostMapping("/facturas/ventas/total-iva/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularIvaVenta(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaVentaService.calcularIvaVenta(criteria, (int) claims.get(CLAIM_ID_USUARIO));
  }

  @PostMapping("/facturas/ventas/ganancia-total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularGananciaTotal(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaVentaService.calcularGananciaTotal(criteria, (int) claims.get(CLAIM_ID_USUARIO));
  }

  @GetMapping("/facturas/ventas/email/{idFactura}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public void enviarFacturaVentaPorEmail(@PathVariable long idFactura) {
    facturaVentaService.enviarFacturaVentaPorEmail(idFactura);
  }
}
