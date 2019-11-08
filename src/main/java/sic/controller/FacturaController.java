package sic.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaCompraCriteria;
import sic.modelo.criteria.BusquedaFacturaVentaCriteria;
import sic.modelo.dto.FacturaCompraDTO;
import sic.modelo.dto.FacturaVentaDTO;
import sic.service.*;
import sic.exception.BusinessServiceException;

@RestController
@RequestMapping("/api/v1")
public class FacturaController {

  private final IFacturaService facturaService;
  private final ISucursalService sucursalService;
  private final IProveedorService proveedorService;
  private final IClienteService clienteService;
  private final IUsuarioService usuarioService;
  private final ITransportistaService transportistaService;
  private final IReciboService reciboService;
  private final ModelMapper modelMapper;
  private final IAuthService authService;
  private final MessageSource messageSource;

  @Autowired
  public FacturaController(
      IFacturaService facturaService,
      ISucursalService sucursalService,
      IProveedorService proveedorService,
      IClienteService clienteService,
      IUsuarioService usuarioService,
      ITransportistaService transportistaService,
      IReciboService reciboService,
      ModelMapper modelMapper,
      IAuthService authService,
      MessageSource messageSource) {
    this.facturaService = facturaService;
    this.sucursalService = sucursalService;
    this.proveedorService = proveedorService;
    this.clienteService = clienteService;
    this.usuarioService = usuarioService;
    this.transportistaService = transportistaService;
    this.reciboService = reciboService;
    this.authService = authService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

    @GetMapping("/facturas/{idFactura}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public Factura getFacturaPorId(@PathVariable long idFactura) {
        return facturaService.getFacturaNoEliminadaPorId(idFactura);
    }

  @DeleteMapping("/facturas/{idFactura}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idFactura) {
    facturaService.eliminarFactura(idFactura);
  }

  @PostMapping("/facturas/venta")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<FacturaVenta> guardarFacturaVenta(
      @RequestBody FacturaVentaDTO facturaVentaDTO,
      @RequestParam(required = false) long[] idsFormaDePago,
      @RequestParam(required = false) BigDecimal[] montos,
      @RequestParam(required = false) int[] indices,
      @RequestParam(required = false) Long idPedido,
      @RequestHeader("Authorization") String authorizationHeader) {
    FacturaVenta fv = modelMapper.map(facturaVentaDTO, FacturaVenta.class);
    Sucursal sucursal = sucursalService.getSucursalPorId(facturaVentaDTO.getIdSucursal());
    fv.setSucursal(sucursal);
    Cliente cliente = clienteService.getClienteNoEliminadoPorId(facturaVentaDTO.getIdCliente());
    if (cliente.getUbicacionFacturacion() == null
        && (fv.getTipoComprobante() == TipoDeComprobante.FACTURA_A
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_B
            || fv.getTipoComprobante() == TipoDeComprobante.FACTURA_C)) {
      throw new BusinessServiceException(messageSource.getMessage(
        "mensaje_ubicacion_facturacion_vacia", null, Locale.getDefault()));
    }
    fv.setCliente(cliente);
    facturaService.asignarClienteEmbeddable(fv, cliente);
    fv.setTransportista(transportistaService.getTransportistaNoEliminadoPorId(facturaVentaDTO.getIdTransportista()));
    fv.setFecha(LocalDateTime.now());
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    fv.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    List<FacturaVenta> facturasGuardadas;
    if (indices != null) {
      facturasGuardadas =
          facturaService.guardar(
              facturaService.dividirFactura(fv, indices),
              idPedido,
              reciboService.construirRecibos(
                  idsFormaDePago,
                  sucursal,
                  fv.getCliente(),
                  fv.getUsuario(),
                  montos,
                  fv.getTotal(),
                  fv.getFecha()));
    } else {
      List<FacturaVenta> facturas = new ArrayList<>();
      facturas.add(fv);
      facturasGuardadas =
          facturaService.guardar(
              facturas,
              idPedido,
              reciboService.construirRecibos(
                  idsFormaDePago,
                  sucursal,
                  fv.getCliente(),
                  fv.getUsuario(),
                  montos,
                  fv.getTotal(),
                  fv.getFecha()));
    }
    return facturasGuardadas;
  }

  @PostMapping("/facturas/compra")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<FacturaCompra> guardarFacturaCompra(
      @RequestBody FacturaCompraDTO facturaCompraDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    FacturaCompra fc = modelMapper.map(facturaCompraDTO, FacturaCompra.class);
    fc.setSucursal(sucursalService.getSucursalPorId(facturaCompraDTO.getIdSucursal()));
    fc.setProveedor(proveedorService.getProveedorNoEliminadoPorId(facturaCompraDTO.getIdProveedor()));
    fc.setTransportista(
        transportistaService.getTransportistaNoEliminadoPorId(facturaCompraDTO.getIdTransportista()));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    fc.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    List<FacturaCompra> facturas = new ArrayList<>();
    facturas.add(fc);
    return facturaService.guardar(facturas);
  }

    @PostMapping("/facturas/{idFactura}/autorizacion")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public FacturaVenta autorizarFactura(@PathVariable long idFactura) {
        return facturaService.autorizarFacturaVenta((FacturaVenta) facturaService.getFacturaNoEliminadaPorId(idFactura));
    }

    @GetMapping("/facturas/{idFactura}/renglones")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public List<RenglonFactura> getRenglonesDeLaFactura(@PathVariable long idFactura) {
            return facturaService.getRenglonesDeLaFactura(idFactura);
    }

  @GetMapping("/facturas/{idFactura}/renglones/notas/credito")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonFactura> getRenglonesDeLaFacturaModificadosParaCredito(
      @PathVariable long idFactura) {
    return facturaService.getRenglonesDeLaFacturaModificadosParaCredito(idFactura);
  }

  @PostMapping("/facturas/compra/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<FacturaCompra> buscarFacturaCompra(
      @RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaService.buscarFacturaCompra(criteria);
  }

  @PostMapping("/facturas/venta/busqueda/criteria")
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

    @GetMapping("/facturas/compra/tipos/sucursales/{idSucursal}/proveedores/{idProveedor}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
    public TipoDeComprobante[] getTipoFacturaCompra(@PathVariable long idSucursal, @PathVariable long idProveedor) {
        return facturaService.getTipoFacturaCompra(sucursalService.getSucursalPorId(idSucursal), proveedorService.getProveedorNoEliminadoPorId(idProveedor));
    }

  @GetMapping("/facturas/venta/tipos/sucursales/{idSucursal}/clientes/{idCliente}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public TipoDeComprobante[] getTipoFacturaVenta(
      @PathVariable long idSucursal,
      @PathVariable long idCliente,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuario = (int) claims.get("idUsuario");
    List<Rol> rolesDeUsuario =
        usuarioService.getUsuarioNoEliminadoPorId(idUsuario).getRoles();
    if (rolesDeUsuario.contains(Rol.ADMINISTRADOR)
        || rolesDeUsuario.contains(Rol.ENCARGADO)
        || rolesDeUsuario.contains(Rol.VENDEDOR)) {
      return facturaService.getTipoFacturaVenta(
          sucursalService.getSucursalPorId(idSucursal), clienteService.getClienteNoEliminadoPorId(idCliente));
    } else if (rolesDeUsuario.contains(Rol.VIAJANTE)
            || rolesDeUsuario.contains(Rol.COMPRADOR)) {
      return new TipoDeComprobante[] {TipoDeComprobante.PEDIDO};
    }
    return new TipoDeComprobante[0];
  }

    @GetMapping("/facturas/tipos/sucursales/{idSucursal}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public TipoDeComprobante[] getTiposFacturaSegunSucursal(@PathVariable long idSucursal) {
        return facturaService.getTiposFacturaSegunSucursal(sucursalService.getSucursalPorId(idSucursal));
    }    
    
    @GetMapping("/facturas/{idFactura}/reporte")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE, Rol.COMPRADOR})
    public ResponseEntity<byte[]> getReporteFacturaVenta(@PathVariable long idFactura) {        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);        
        headers.add("content-disposition", "inline; filename=Factura.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        byte[] reportePDF = facturaService.getReporteFacturaVenta(facturaService.getFacturaNoEliminadaPorId(idFactura));
        return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
    }
    
    @GetMapping("/facturas/renglones/pedidos/{idPedido}")
    @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
    public List<RenglonFactura> getRenglonesPedidoParaFacturar(@PathVariable long idPedido,
                                                               @RequestParam TipoDeComprobante tipoDeComprobante) {
        return facturaService.getRenglonesPedidoParaFacturar(idPedido, tipoDeComprobante);
    }

  @GetMapping("/facturas/renglon-venta")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public RenglonFactura calcularRenglonVenta(
      @RequestParam long idProducto,
      @RequestParam TipoDeComprobante tipoDeComprobante,
      @RequestParam Movimiento movimiento,
      @RequestParam BigDecimal cantidad,
      @RequestParam long idCliente) {
    return facturaService.calcularRenglon(
        tipoDeComprobante,
        movimiento,
        cantidad,
        idProducto,
        false,
        clienteService.getClienteNoEliminadoPorId(idCliente),
        null);
  }

  @GetMapping("/facturas/renglon-compra")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public RenglonFactura calcularRenglonCompra(
      @RequestParam long idProducto,
      @RequestParam TipoDeComprobante tipoDeComprobante,
      @RequestParam Movimiento movimiento,
      @RequestParam BigDecimal cantidad,
      @RequestParam BigDecimal bonificacion) {
    return facturaService.calcularRenglon(
        tipoDeComprobante, movimiento, cantidad, idProducto, false, null, bonificacion);
  }

  @PostMapping("/facturas/total-facturado-venta/criteria")
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

  @PostMapping("/facturas/total-facturado-compra/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalFacturadoCompra(
      @RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaService.calcularTotalFacturadoCompra(criteria);
  }

  @PostMapping("/facturas/total-iva-venta/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularIvaVenta(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaService.calcularIvaVenta(criteria, (int) claims.get("idUsuario"));
  }

  @PostMapping("/facturas/total-iva-compra/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalIvaCompra(@RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaService.calcularIvaCompra(criteria);
  }

  @PostMapping("/facturas/ganancia-total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularGananciaTotal(
      @RequestBody BusquedaFacturaVentaCriteria criteria,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    return facturaService.calcularGananciaTotal(criteria, (int) claims.get("idUsuario"));
  }
}
