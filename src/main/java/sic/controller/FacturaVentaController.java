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
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1")
public class FacturaVentaController {

  private final IFacturaService facturaService;
  private final ISucursalService sucursalService;
  private final IClienteService clienteService;
  private final IUsuarioService usuarioService;
  private final ITransportistaService transportistaService;
  private final IReciboService reciboService;
  private final IPedidoService pedidoService;
  private final IAuthService authService;
  private final MessageSource messageSource;

  @Autowired
  public FacturaVentaController(
      IFacturaService facturaService,
      ISucursalService sucursalService,
      IClienteService clienteService,
      IUsuarioService usuarioService,
      ITransportistaService transportistaService,
      IReciboService reciboService,
      IPedidoService pedidoService,
      IAuthService authService,
      MessageSource messageSource) {
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
    fv.setDescuentoPorcentaje(nuevaFacturaVentaDTO.getDescuentoPorcentaje());
    fv.setRecargoPorcentaje(nuevaFacturaVentaDTO.getRecargoPorcentaje());
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
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    fv.setRenglones(
        facturaService.calcularRenglones(
            nuevaFacturaVentaDTO.getTipoDeComprobante(),
            Movimiento.VENTA,
            this.getArrayDeCantidadesProducto(nuevaFacturaVentaDTO.getRenglones()),
            this.getArrayDeIdProducto(nuevaFacturaVentaDTO.getRenglones()),
            null));
    fv.setObservaciones(
        nuevaFacturaVentaDTO.getObservaciones() != null
            ? nuevaFacturaVentaDTO.getObservaciones()
            : "");
    List<FacturaVenta> facturasGuardadas;
    if (nuevaFacturaVentaDTO.getIndices() != null) {
      facturasGuardadas =
          facturaService.guardar(
              facturaService.dividirFactura(fv, nuevaFacturaVentaDTO.getIndices()),
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
          facturaService.guardar(
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
    return facturasGuardadas;
  }

  @PostMapping("/facturas/ventas/{idFactura}/autorizacion")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public FacturaVenta autorizarFactura(@PathVariable long idFactura) {
    return facturaService.autorizarFacturaVenta(
        (FacturaVenta) facturaService.getFacturaNoEliminadaPorId(idFactura));
  }

  @PostMapping("/facturas/ventas/busqueda/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public Page<FacturaVenta> buscarFacturaVenta(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaService.buscarFacturaVenta(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/facturas/ventas/tipos/sucursales/{idSucursal}/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public TipoDeComprobante[] getTipoFacturaVenta(
      @PathVariable long idSucursal,
      @PathVariable long idCliente,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuario = (int) claims.get("idUsuario");
    List<Rol> rolesDeUsuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario).getRoles();
    if (rolesDeUsuario.contains(Rol.ADMINISTRADOR)
        || rolesDeUsuario.contains(Rol.ENCARGADO)
        || rolesDeUsuario.contains(Rol.VENDEDOR)) {
      return facturaService.getTipoFacturaVenta(
          sucursalService.getSucursalPorId(idSucursal),
          clienteService.getClienteNoEliminadoPorId(idCliente));
    } else if (rolesDeUsuario.contains(Rol.VIAJANTE) || rolesDeUsuario.contains(Rol.COMPRADOR)) {
      return new TipoDeComprobante[] {TipoDeComprobante.PEDIDO};
    }
    return new TipoDeComprobante[0];
  }

  @GetMapping("/facturas/ventas/{idFactura}/reporte")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public ResponseEntity<byte[]> getReporteFacturaVenta(@PathVariable long idFactura) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("content-disposition", "inline; filename=Factura.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF =
        facturaService.getReporteFacturaVenta(facturaService.getFacturaNoEliminadaPorId(idFactura));
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }

  @GetMapping("/facturas/ventas/renglones/pedidos/{idPedido}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> getRenglonesPedidoParaFacturar(
      @PathVariable long idPedido, @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaService.getRenglonesPedidoParaFacturar(idPedido, tipoDeComprobante);
  }

  @PostMapping("/facturas/ventas/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> calcularRenglonesVenta(
      @RequestBody List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO,
      @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaService.calcularRenglones(
        tipoDeComprobante,
        Movimiento.VENTA,
        this.getArrayDeCantidadesProducto(nuevosRenglonesFacturaDTO),
        this.getArrayDeIdProducto(nuevosRenglonesFacturaDTO),
        null);
  }

  @PostMapping("/facturas/ventas/total-facturado/criteria")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public BigDecimal calcularTotalFacturadoVenta(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaService.calcularTotalFacturadoVenta(criteria, (int) claims.get("idUsuario"));
  }

  @PostMapping("/facturas/ventas/total-iva/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularIvaVenta(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaService.calcularIvaVenta(criteria, (int) claims.get("idUsuario"));
  }

  @PostMapping("/facturas/ventas/ganancia-total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularGananciaTotal(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaService.calcularGananciaTotal(criteria, (int) claims.get("idUsuario"));
  }

  @GetMapping("/facturas/ventas/email/{idFactura}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public void enviarFacturaVentaPorEmail(@PathVariable long idFactura) {
    facturaService.enviarFacturaVentaPorEmail(idFactura);
  }

  private long[] getArrayDeIdProducto(List<NuevoRenglonFacturaDTO> nuevosRenglones) {
    long[] idProductoItem = new long[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      idProductoItem[i] = nuevosRenglones.get(i).getIdProducto();
    }
    return idProductoItem;
  }

  private BigDecimal[] getArrayDeCantidadesProducto(List<NuevoRenglonFacturaDTO> nuevosRenglones) {
    BigDecimal[] cantidades = new BigDecimal[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      cantidades[i] = nuevosRenglones.get(i).getCantidad();
    }
    return cantidades;
  }
}
