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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FacturaCompraController {

  private final IFacturaService facturaService;
  private final ISucursalService sucursalService;
  private final IProveedorService proveedorService;
  private final IUsuarioService usuarioService;
  private final ITransportistaService transportistaService;
  private final IAuthService authService;

  @Autowired
  public FacturaCompraController(
      IFacturaService facturaService,
      ISucursalService sucursalService,
      IProveedorService proveedorService,
      IUsuarioService usuarioService,
      ITransportistaService transportistaService,
      IAuthService authService) {
    this.facturaService = facturaService;
    this.sucursalService = sucursalService;
    this.proveedorService = proveedorService;
    this.usuarioService = usuarioService;
    this.transportistaService = transportistaService;
    this.authService = authService;
  }

  @PostMapping("/facturas-compra")
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
            this.getArrayDeCantidadesProducto(nuevaCompraCompraDTO.getRenglones()),
            this.getArrayDeIdProducto(nuevaCompraCompraDTO.getRenglones()),
            this.getArrayDeBonificaciones(nuevaCompraCompraDTO.getRenglones())));
    fc.setRecargoPorcentaje(nuevaCompraCompraDTO.getRecargoPorcentaje());
    fc.setDescuentoPorcentaje(nuevaCompraCompraDTO.getDescuentoPorcentaje());
    fc.setObservaciones(nuevaCompraCompraDTO.getObservaciones());
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
    return facturaService.guardar(facturas);
  }

  @PostMapping("/facturas-compra/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Page<FacturaCompra> buscarFacturaCompra(
      @RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaService.buscarFacturaCompra(criteria);
  }

  @GetMapping("/facturas-compra/tipos/sucursales/{idSucursal}/proveedores/{idProveedor}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public TipoDeComprobante[] getTipoFacturaCompra(
      @PathVariable long idSucursal, @PathVariable long idProveedor) {
    return facturaService.getTipoFacturaCompra(
        sucursalService.getSucursalPorId(idSucursal),
        proveedorService.getProveedorNoEliminadoPorId(idProveedor));
  }

  @PostMapping("/facturas-compra/renglones")
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

  @PostMapping("/facturas-compra/total-facturado/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalFacturadoCompra(
      @RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaService.calcularTotalFacturadoCompra(criteria);
  }

  @PostMapping("/facturas-compra/total-iva/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public BigDecimal calcularTotalIvaCompra(@RequestBody BusquedaFacturaCompraCriteria criteria) {
    return facturaService.calcularIvaCompra(criteria);
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
