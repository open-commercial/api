package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.exception.BusinessServiceException;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaFacturaCompraCriteria;
import org.opencommercial.model.criteria.BusquedaFacturaVentaCriteria;
import org.opencommercial.model.dto.NuevaFacturaCompraDTO;
import org.opencommercial.model.dto.NuevaFacturaVentaDTO;
import org.opencommercial.model.dto.NuevoRenglonFacturaDTO;
import org.opencommercial.model.dto.NuevosResultadosComprobanteDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
public class FacturaController {

  private final FacturaService facturaService;
  private final SucursalService sucursalService;
  private final FacturaCompraService facturaCompraService;
  private final ProveedorService proveedorService;
  private final UsuarioService usuarioService;
  private final TransportistaService transportistaService;
  private final FacturaVentaService facturaVentaService;
  private final ReciboService reciboService;
  private final AuthService authService;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public FacturaController(FacturaService facturaService,
                           SucursalService sucursalService,
                           FacturaCompraService facturaCompraService,
                           ProveedorService proveedorService,
                           UsuarioService usuarioService,
                           TransportistaService transportistaService,
                           FacturaVentaService facturaVentaService,
                           ReciboService reciboService,
                           AuthService authService,
                           MessageSource messageSource) {
    this.facturaService = facturaService;
    this.sucursalService = sucursalService;
    this.facturaCompraService = facturaCompraService;
    this.proveedorService = proveedorService;
    this.usuarioService = usuarioService;
    this.transportistaService = transportistaService;
    this.facturaVentaService = facturaVentaService;
    this.reciboService = reciboService;
    this.authService = authService;
    this.messageSource = messageSource;
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
  public Resultados calcularResultadosFactura(@RequestBody NuevosResultadosComprobanteDTO nrecoDTO) {
    Resultados nuevoResultado = facturaService.calcularResultadosFactura(nrecoDTO);
    if (nrecoDTO.getTipoDeComprobante() == TipoDeComprobante.FACTURA_B
            || nrecoDTO.getTipoDeComprobante() == TipoDeComprobante.PRESUPUESTO) {
      nuevoResultado.setSubTotalBruto(
              nuevoResultado
                      .getSubTotalBruto()
                      .add(nuevoResultado.getIva21Neto().add(nuevoResultado.getIva105Neto())));
      nuevoResultado.setIva21Neto(BigDecimal.ZERO);
      nuevoResultado.setIva105Neto(BigDecimal.ZERO);
    }
    return nuevoResultado;
  }

  @PostMapping("/api/v1/facturas/compras")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<FacturaCompra> guardarFacturaCompra(@RequestBody NuevaFacturaCompraDTO nuevaCompraCompraDTO,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
    FacturaCompra fc = new FacturaCompra();
    fc.setFecha(nuevaCompraCompraDTO.getFecha());
    fc.setTipoComprobante(nuevaCompraCompraDTO.getTipoDeComprobante());
    fc.setNumSerie(nuevaCompraCompraDTO.getNumSerie() != null ? nuevaCompraCompraDTO.getNumSerie() : 0L);
    fc.setNumFactura(nuevaCompraCompraDTO.getNumFactura() != null ? nuevaCompraCompraDTO.getNumFactura() : 0L);
    fc.setFechaVencimiento(nuevaCompraCompraDTO.getFechaVencimiento());
    fc.setFechaAlta(LocalDateTime.now());
    fc.setRenglones(facturaService.calcularRenglones(
            nuevaCompraCompraDTO.getTipoDeComprobante(),
            Movimiento.COMPRA,
            nuevaCompraCompraDTO.getRenglones()));
    fc.setRecargoPorcentaje(
            nuevaCompraCompraDTO.getRecargoPorcentaje() != null
                    ? nuevaCompraCompraDTO.getRecargoPorcentaje()
                    : BigDecimal.ZERO);
    fc.setDescuentoPorcentaje(
            nuevaCompraCompraDTO.getDescuentoPorcentaje() != null
                    ? nuevaCompraCompraDTO.getDescuentoPorcentaje()
                    : BigDecimal.ZERO);
    fc.setObservaciones(
            nuevaCompraCompraDTO.getObservaciones() != null
                    ? nuevaCompraCompraDTO.getObservaciones()
                    : "");
    fc.setSucursal(sucursalService.getSucursalPorId(nuevaCompraCompraDTO.getIdSucursal()));
    fc.setProveedor(proveedorService.getProveedorNoEliminadoPorId(nuevaCompraCompraDTO.getIdProveedor()));
    if (nuevaCompraCompraDTO.getIdTransportista() != null) {
      fc.setTransportista(transportistaService.getTransportistaNoEliminadoPorId(nuevaCompraCompraDTO.getIdTransportista()));
    }
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    fc.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class)));
    List<FacturaCompra> facturas = new ArrayList<>();
    facturas.add(fc);
    return facturaCompraService.guardar(facturas);
  }

  @PostMapping("/api/v1/facturas/compras/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<FacturaCompra> buscarFacturaCompra(@RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaCompraService.buscarFacturaCompra(criteria);
  }

  @GetMapping("/api/v1/facturas/compras/tipos/sucursales/{idSucursal}/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public TipoDeComprobante[] getTipoFacturaCompra(@PathVariable long idSucursal,
                                                  @PathVariable long idProveedor) {
    return facturaCompraService.getTiposDeComprobanteCompra(
            sucursalService.getSucursalPorId(idSucursal),
            proveedorService.getProveedorNoEliminadoPorId(idProveedor));
  }

  @PostMapping("/api/v1/facturas/compras/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> calcularRenglonesCompra(
          @RequestBody List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO,
          @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaService.calcularRenglones(tipoDeComprobante, Movimiento.COMPRA, nuevosRenglonesFacturaDTO);
  }

  @PostMapping("/api/v1/facturas/compras/total-facturado/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalFacturadoCompra(@RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaCompraService.calcularTotalFacturadoCompra(criteria);
  }

  @PostMapping("/api/v1/facturas/compras/total-iva/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalIvaCompra(@RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaCompraService.calcularIvaCompra(criteria);
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
