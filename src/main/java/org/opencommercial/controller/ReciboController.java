package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.Recibo;
import org.opencommercial.model.Rol;
import org.opencommercial.model.criteria.BusquedaReciboCriteria;
import org.opencommercial.model.dto.ReciboDTO;
import org.opencommercial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
public class ReciboController {

  private final ReciboService reciboService;
  private final SucursalService sucursalService;
  private final UsuarioService usuarioService;
  private final ClienteService clienteService;
  private final ProveedorService proveedorService;
  private final FormaDePagoService formaDePagoService;
  private final AuthService authService;
  private final ModelMapper modelMapper;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public ReciboController(ReciboService reciboService,
                          SucursalService sucursalService,
                          UsuarioService usuarioService,
                          ClienteService clienteService,
                          ProveedorService proveedorService,
                          FormaDePagoService formaDePagoService,
                          AuthService authService,
                          ModelMapper modelMapper) {
    this.reciboService = reciboService;
    this.sucursalService = sucursalService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.formaDePagoService = formaDePagoService;
    this.proveedorService = proveedorService;
    this.authService = authService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/api/v1/recibos/{idRecibo}")
  public Recibo getReciboPorId(@PathVariable long idRecibo) {
    return reciboService.getReciboNoEliminadoPorId(idRecibo);
  }

  @PostMapping("/api/v1/recibos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Recibo> buscarConCriteria(@RequestBody BusquedaReciboCriteria criteria) {
    return reciboService.buscarRecibos(criteria);
  }

  @PostMapping("/api/v1/recibos/total/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public BigDecimal getTotalRecibos(@RequestBody BusquedaReciboCriteria criteria) {
    return reciboService.getTotalRecibos(criteria);
  }

  @PostMapping("/api/v1/recibos/clientes")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Recibo guardarReciboCliente(@RequestBody ReciboDTO reciboDTO,
                                     @RequestHeader("Authorization") String authorizationHeader) {
    Recibo recibo = modelMapper.map(reciboDTO, Recibo.class);
    recibo.setSucursal(sucursalService.getSucursalPorId(reciboDTO.getIdSucursal()));
    recibo.setCliente(clienteService.getClienteNoEliminadoPorId(reciboDTO.getIdCliente()));
    recibo.setFormaDePago(formaDePagoService.getFormasDePagoNoEliminadoPorId(reciboDTO.getIdFormaDePago()));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class)));
    recibo.setFecha(LocalDateTime.now());
    return reciboService.guardar(recibo);
  }

  @PostMapping("/api/v1/recibos/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Recibo guardarReciboProveedor(@RequestBody ReciboDTO reciboDTO,
                                       @RequestHeader("Authorization") String authorizationHeader) {
    Recibo recibo = modelMapper.map(reciboDTO, Recibo.class);
    recibo.setSucursal(sucursalService.getSucursalPorId(reciboDTO.getIdSucursal()));
    recibo.setProveedor(proveedorService.getProveedorNoEliminadoPorId(reciboDTO.getIdProveedor()));
    recibo.setFormaDePago(formaDePagoService.getFormasDePagoNoEliminadoPorId(reciboDTO.getIdFormaDePago()));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class)));
    recibo.setFecha(LocalDateTime.now());
    return reciboService.guardar(recibo);
  }

  @DeleteMapping("/api/v1/recibos/{idRecibo}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public void eliminar(@PathVariable long idRecibo) {
    reciboService.eliminar(idRecibo);
  }

  @GetMapping("/api/v1/recibos/{idRecibo}/reporte")
  public ResponseEntity<byte[]> getReporteRecibo(@PathVariable long idRecibo) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("content-disposition", "inline; filename=Recibo.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF = reciboService.getReporteRecibo(reciboService.getReciboNoEliminadoPorId(idRecibo));
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }
}
