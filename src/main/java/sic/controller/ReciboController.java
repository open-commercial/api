package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.EstadoRecibo;
import sic.modelo.criteria.BusquedaReciboCriteria;
import sic.modelo.Recibo;
import sic.modelo.Rol;
import sic.modelo.dto.NuevoReciboClienteDTO;
import sic.modelo.dto.NuevoReciboDepositoDTO;
import sic.modelo.dto.NuevoReciboProveedorDTO;
import sic.service.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
public class ReciboController {

  private final IReciboService reciboService;
  private final ISucursalService sucursalService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IProveedorService proveedorService;
  private final IFormaDePagoService formaDePagoService;
  private final IAuthService authService;
  @Autowired
  public ReciboController(
      IReciboService reciboService,
      ISucursalService sucursalService,
      IUsuarioService usuarioService,
      IClienteService clienteService,
      IProveedorService proveedorService,
      IFormaDePagoService formaDePagoService,
      IAuthService authService) {
    this.reciboService = reciboService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.formaDePagoService = formaDePagoService;
    this.proveedorService = proveedorService;
    this.authService = authService;
  }

  @GetMapping("/recibos/{idRecibo}")
  public Recibo getReciboPorId(@PathVariable long idRecibo) {
    return reciboService.getReciboNoEliminadoPorId(idRecibo);
  }

  @PostMapping("/recibos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Recibo> buscarConCriteria(@RequestBody BusquedaReciboCriteria criteria) {
    return reciboService.buscarRecibos(criteria);
  }

  @PostMapping("/recibos/total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalRecibos(@RequestBody BusquedaReciboCriteria criteria) {
    return reciboService.getTotalRecibos(criteria);
  }

  @PostMapping("/recibos/clientes")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Recibo guardarReciboCliente(
      @RequestBody NuevoReciboClienteDTO nuevoReciboClienteDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    var recibo = new Recibo();
    recibo.setConcepto(nuevoReciboClienteDTO.getConcepto());
    recibo.setSucursal(sucursalService.getSucursalPorId(nuevoReciboClienteDTO.getIdSucursal()));
    recibo.setCliente(
        clienteService.getClienteNoEliminadoPorId(nuevoReciboClienteDTO.getIdCliente()));
    recibo.setFormaDePago(
        formaDePagoService.getFormasDePagoNoEliminadoPorId(
            nuevoReciboClienteDTO.getIdFormaDePago()));
    var claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    recibo.setFecha(LocalDateTime.now());
    recibo.setEstado(EstadoRecibo.APROBADO);
    recibo.setMonto(nuevoReciboClienteDTO.getMonto());
    return reciboService.guardar(recibo);
  }

  @PostMapping("/recibos/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Recibo guardarReciboProveedor(
      @RequestBody NuevoReciboProveedorDTO nuevoReciboProveedorDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    var recibo = new Recibo();
    recibo.setConcepto(nuevoReciboProveedorDTO.getConcepto());
    recibo.setSucursal(sucursalService.getSucursalPorId(nuevoReciboProveedorDTO.getIdSucursal()));
    recibo.setProveedor(
        proveedorService.getProveedorNoEliminadoPorId(nuevoReciboProveedorDTO.getIdProveedor()));
    recibo.setFormaDePago(
        formaDePagoService.getFormasDePagoNoEliminadoPorId(
            nuevoReciboProveedorDTO.getIdFormaDePago()));
    var claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    recibo.setFecha(LocalDateTime.now());
    recibo.setMonto(nuevoReciboProveedorDTO.getMonto());
    recibo.setEstado(EstadoRecibo.APROBADO);
    return reciboService.guardar(recibo);
  }

  @PostMapping("/recibos/clientes/depositos")
  @AccesoRolesPermitidos({Rol.COMPRADOR})
  public Recibo guardarReciboPorDeposito(@RequestBody NuevoReciboDepositoDTO nuevoReciboDepositoDTO) {
    return reciboService.guardarReciboPorDeposito(nuevoReciboDepositoDTO);
  }

  @DeleteMapping("/recibos/{idRecibo}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public void eliminar(@PathVariable long idRecibo) {
    reciboService.eliminar(idRecibo);
  }

  @GetMapping("/recibos/{idRecibo}/reporte")
  public ResponseEntity<byte[]> getReporteRecibo(@PathVariable long idRecibo) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("content-disposition", "inline; filename=Recibo.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF = reciboService.getReporteRecibo(reciboService.getReciboNoEliminadoPorId(idRecibo));
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }

  @PutMapping("/recibos/{idRecibo}/aprobar")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void aprobarRecibo(@PathVariable long idRecibo) {
    reciboService.aprobarRecibo(idRecibo);
  }
}
