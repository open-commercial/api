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
import sic.modelo.dto.NuevoRenglonFacturaDTO;
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
  private final IPedidoService pedidoService;
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
      IPedidoService pedidoService,
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
    this.pedidoService = pedidoService;
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
      @RequestBody NuevaFacturaDTO nuevaFacturaVentaDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
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
            this.getArrayDeBonificaciones(nuevaFacturaVentaDTO.getRenglones())));
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

//  @PostMapping("/facturas/compra")
//  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
//  public List<FacturaCompra> guardarFacturaCompra(
//          @RequestBody NuevaFacturaDTO nuevaCompraVentaDTO,
//      @RequestHeader("Authorization") String authorizationHeader) {
//    FacturaCompra fc = modelMapper.map(facturaCompraDTO, FacturaCompra.class);
//    fc.setSucursal(sucursalService.getSucursalPorId(facturaCompraDTO.getIdSucursal()));
//    fc.setProveedor(proveedorService.getProveedorNoEliminadoPorId(facturaCompraDTO.getIdProveedor()));
//    if (facturaCompraDTO.getIdTransportista() != null) {
//      fc.setTransportista(
//          transportistaService.getTransportistaNoEliminadoPorId(
//              facturaCompraDTO.getIdTransportista()));
//    }
//    Claims claims = authService.getClaimsDelToken(authorizationHeader);
//    fc.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
//    List<FacturaCompra> facturas = new ArrayList<>();
//    facturas.add(fc);
//    return facturaService.guardar(facturas);
//  }

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

  @PostMapping("/facturas/renglones-venta")
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

  @PostMapping("/facturas/renglones-compra")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> calcularRenglonesCompra(
      @RequestBody List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO,
      @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaService.calcularRenglones(
        tipoDeComprobante,
        Movimiento.COMPRA,
        this.getArrayDeCantidadesProducto(nuevosRenglonesFacturaDTO),
        this.getArrayDeIdProducto(nuevosRenglonesFacturaDTO),
        this.getArrayDeBonificaciones(nuevosRenglonesFacturaDTO));
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

  @GetMapping("/facturas/email/{idFactura}")
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

  private BigDecimal[] getArrayDeBonificaciones(List<NuevoRenglonFacturaDTO> nuevosRenglones) {
    BigDecimal[] bonificaciones = new BigDecimal[nuevosRenglones.size()];
    for (int i = 0; i < nuevosRenglones.size(); ++i) {
      bonificaciones[i] = nuevosRenglones.get(i).getBonificacion();
    }
    return bonificaciones;
  }
}
