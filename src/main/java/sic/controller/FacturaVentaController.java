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
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class FacturaVentaController {

  private final IFacturaVentaService facturaVentaService;
  private final IFacturaService facturaService;
  private final ISucursalService sucursalService;
  private final IClienteService clienteService;
  private final IUsuarioService usuarioService;
  private final ITransportistaService transportistaService;
  private final IReciboService reciboService;
  private final IPedidoService pedidoService;
  private final IAuthService authService;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public FacturaVentaController(
      IFacturaVentaService facturaVentaService,
      IFacturaService facturaService,
      ISucursalService sucursalService,
      IClienteService clienteService,
      IUsuarioService usuarioService,
      ITransportistaService transportistaService,
      IReciboService reciboService,
      IPedidoService pedidoService,
      IAuthService authService,
      MessageSource messageSource) {
    this.facturaVentaService = facturaVentaService;
    this.facturaService = facturaService;
    this.sucursalService = sucursalService;
    this.clienteService = clienteService;
    this.usuarioService = usuarioService;
    this.transportistaService = transportistaService;
    this.reciboService = reciboService;
    this.pedidoService = pedidoService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @PostMapping("/facturas/ventas")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<FacturaVenta> guardarFacturaVenta(
      @RequestBody NuevaFacturaVentaDTO nuevaFacturaVentaDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    List<TipoDeComprobante> tiposDeFacturaPermititos =
        Arrays.asList(
            TipoDeComprobante.FACTURA_A,
            TipoDeComprobante.FACTURA_B,
            TipoDeComprobante.FACTURA_C,
            TipoDeComprobante.FACTURA_X,
            TipoDeComprobante.PRESUPUESTO);
    if (!tiposDeFacturaPermititos.contains(nuevaFacturaVentaDTO.getTipoDeComprobante())) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_tipo_de_comprobante_no_valido", null, Locale.getDefault()));
    }
    FacturaVenta fv = new FacturaVenta();
    Sucursal sucursal;
    if (nuevaFacturaVentaDTO.getIdPedido() != null) {
      Pedido pedido = pedidoService.getPedidoNoEliminadoPorId(nuevaFacturaVentaDTO.getIdPedido());
      fv.setPedido(pedido);
      sucursal = pedido.getSucursal();
    } else {
      sucursal = sucursalService.getSucursalPorId(nuevaFacturaVentaDTO.getIdSucursal());
    }
    fv.setSucursal(sucursal);
    fv.setTipoComprobante(nuevaFacturaVentaDTO.getTipoDeComprobante());
    fv.setDescuentoPorcentaje(
        nuevaFacturaVentaDTO.getDescuentoPorcentaje() != null
            ? nuevaFacturaVentaDTO.getDescuentoPorcentaje()
            : BigDecimal.ZERO);
    fv.setRecargoPorcentaje(
        nuevaFacturaVentaDTO.getRecargoPorcentaje() != null
            ? nuevaFacturaVentaDTO.getRecargoPorcentaje()
            : BigDecimal.ZERO);
    Cliente cliente =
        clienteService.getClienteNoEliminadoPorId(nuevaFacturaVentaDTO.getIdCliente());
    if (cliente.getUbicacionFacturacion() == null
        && (fv.getTipoComprobante() == TipoDeComprobante.FACTURA_A
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_C)) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault()));
    }
    fv.setCliente(cliente);
    fv.setClienteEmbedded(clienteService.crearClienteEmbedded(cliente));
    if (nuevaFacturaVentaDTO.getIdTransportista() != null) {
      fv.setTransportista(
          transportistaService.getTransportistaNoEliminadoPorId(
              nuevaFacturaVentaDTO.getIdTransportista()));
    }
    fv.setFecha(LocalDateTime.now());
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    fv.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(
            ((Integer) claims.get(CLAIM_ID_USUARIO)).longValue()));
    fv.setRenglones(
        facturaService.calcularRenglones(
            nuevaFacturaVentaDTO.getTipoDeComprobante(),
            Movimiento.VENTA,
            nuevaFacturaVentaDTO.getRenglones()));
    fv.setObservaciones(
        nuevaFacturaVentaDTO.getObservaciones() != null
            ? nuevaFacturaVentaDTO.getObservaciones()
            : "");
    List<FacturaVenta> facturasGuardadas;
    if (nuevaFacturaVentaDTO.getIndices() != null && nuevaFacturaVentaDTO.getIndices().length > 0) {
      facturasGuardadas =
          facturaVentaService.guardar(
              facturaVentaService.dividirFactura(fv, nuevaFacturaVentaDTO.getIndices()),
              nuevaFacturaVentaDTO.getIdPedido(),
              reciboService.construirRecibos(
                  nuevaFacturaVentaDTO.getIdsFormaDePago(),
                  sucursal,
                  fv.getCliente(),
                  fv.getUsuario(),
                  nuevaFacturaVentaDTO.getMontos(),
                  fv.getTotal(),
                  fv.getFecha()));
    } else {
      List<FacturaVenta> facturas = new ArrayList<>();
      facturas.add(fv);
      facturasGuardadas =
          facturaVentaService.guardar(
              facturas,
              nuevaFacturaVentaDTO.getIdPedido(),
              reciboService.construirRecibos(
                  nuevaFacturaVentaDTO.getIdsFormaDePago(),
                  sucursal,
                  fv.getCliente(),
                  fv.getUsuario(),
                  nuevaFacturaVentaDTO.getMontos(),
                  fv.getTotal(),
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
    List<Rol> rolesDeUsuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario).getRoles();
    if (rolesDeUsuario.contains(Rol.ADMINISTRADOR)
        || rolesDeUsuario.contains(Rol.ENCARGADO)
        || rolesDeUsuario.contains(Rol.VENDEDOR)) {
      return facturaVentaService.getTiposDeComprobanteVenta(
          sucursalService.getSucursalPorId(idSucursal),
          clienteService.getClienteNoEliminadoPorId(idCliente));
    } else if (rolesDeUsuario.contains(Rol.VIAJANTE) || rolesDeUsuario.contains(Rol.COMPRADOR)) {
      return new TipoDeComprobante[] {TipoDeComprobante.PEDIDO};
    }
    return new TipoDeComprobante[0];
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
