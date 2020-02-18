package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaFacturaCompraCriteria;
import sic.modelo.dto.NuevaFacturaCompraDTO;
import sic.modelo.dto.NuevoRenglonFacturaDTO;
import sic.service.*;
import sic.util.CalculosComprobante;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FacturaCompraController {

  private final IFacturaCompraService facturaCompraService;
  private final IFacturaService facturaService;
  private final ISucursalService sucursalService;
  private final IProveedorService proveedorService;
  private final IUsuarioService usuarioService;
  private final ITransportistaService transportistaService;
  private final IAuthService authService;

  @Autowired
  public FacturaCompraController(
      IFacturaCompraService facturaCompraService,
      IFacturaService facturaService,
      ISucursalService sucursalService,
      IProveedorService proveedorService,
      IUsuarioService usuarioService,
      ITransportistaService transportistaService,
      IAuthService authService) {
    this.facturaCompraService = facturaCompraService;
    this.facturaService = facturaService;
    this.sucursalService = sucursalService;
    this.proveedorService = proveedorService;
    this.usuarioService = usuarioService;
    this.transportistaService = transportistaService;
    this.authService = authService;
  }

  @PostMapping("/facturas/compras")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<FacturaCompra> guardarFacturaCompra(
      @RequestBody NuevaFacturaCompraDTO nuevaCompraCompraDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    FacturaCompra fc = new FacturaCompra();
    fc.setFecha(nuevaCompraCompraDTO.getFecha());
    fc.setTipoComprobante(nuevaCompraCompraDTO.getTipoDeComprobante());
    fc.setNumSerie(
        nuevaCompraCompraDTO.getNumSerie() != null ? nuevaCompraCompraDTO.getNumSerie() : 0L);
    fc.setNumFactura(
        nuevaCompraCompraDTO.getNumFactura() != null ? nuevaCompraCompraDTO.getNumFactura() : 0L);
    fc.setFechaVencimiento(nuevaCompraCompraDTO.getFechaVencimiento());
    fc.setRenglones(
        facturaService.calcularRenglones(
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
    fc.setProveedor(
        proveedorService.getProveedorNoEliminadoPorId(nuevaCompraCompraDTO.getIdProveedor()));
    if (nuevaCompraCompraDTO.getIdTransportista() != null) {
      fc.setTransportista(
          transportistaService.getTransportistaNoEliminadoPorId(
              nuevaCompraCompraDTO.getIdTransportista()));
    }
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    fc.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    List<FacturaCompra> facturas = new ArrayList<>();
    facturas.add(fc);
    return facturaCompraService.guardar(facturas);
  }

  @PostMapping("/facturas/compras/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<FacturaCompra> buscarFacturaCompra(
      @RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaCompraService.buscarFacturaCompra(criteria);
  }

  @GetMapping("/facturas/compras/tipos/sucursales/{idSucursal}/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public TipoDeComprobante[] getTipoFacturaCompra(
      @PathVariable long idSucursal, @PathVariable long idProveedor) {
    return facturaCompraService.getTiposDeComprobanteCompra(
        sucursalService.getSucursalPorId(idSucursal),
        proveedorService.getProveedorNoEliminadoPorId(idProveedor));
  }

  @PostMapping("/facturas/compras/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR, Rol.VIAJANTE})
  public List<RenglonFactura> calcularRenglonesCompra(
      @RequestBody List<NuevoRenglonFacturaDTO> nuevosRenglonesFacturaDTO,
      @RequestParam TipoDeComprobante tipoDeComprobante) {
    return facturaService.calcularRenglones(
        tipoDeComprobante, Movimiento.COMPRA, nuevosRenglonesFacturaDTO);
  }

  @PostMapping("/facturas/compras/total-facturado/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalFacturadoCompra(
      @RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaCompraService.calcularTotalFacturadoCompra(criteria);
  }

  @PostMapping("/facturas/compras/total-iva/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalIvaCompra(@RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaCompraService.calcularIvaCompra(criteria);
  }
}
