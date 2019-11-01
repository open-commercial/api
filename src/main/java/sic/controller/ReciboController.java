package sic.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.criteria.BusquedaReciboCriteria;
import sic.modelo.Recibo;
import sic.modelo.Rol;
import sic.modelo.dto.ReciboDTO;
import sic.service.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
public class ReciboController {

  private final IReciboService reciboService;
  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;
  private final IProveedorService proveedorService;
  private final IFormaDePagoService formaDePagoService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;

  @Autowired
  public ReciboController(
      IReciboService reciboService,
      IEmpresaService empresaService,
      IUsuarioService usuarioService,
      IClienteService clienteService,
      IProveedorService proveedorService,
      IFormaDePagoService formaDePagoService,
      IAuthService authService,
      ModelMapper modelMapper) {
    this.reciboService = reciboService;
    this.empresaService = empresaService;
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
    this.formaDePagoService = formaDePagoService;
    this.proveedorService = proveedorService;
    this.authService = authService;
    this.modelMapper = modelMapper;
  }

  @GetMapping("/recibos/{idRecibo}")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
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
      @RequestBody ReciboDTO reciboDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Recibo recibo = modelMapper.map(reciboDTO, Recibo.class);
    recibo.setEmpresa(empresaService.getEmpresaPorId(reciboDTO.getIdEmpresa()));
    recibo.setCliente(clienteService.getClienteNoEliminadoPorId(reciboDTO.getIdCliente()));
    recibo.setFormaDePago(formaDePagoService.getFormasDePagoNoEliminadoPorId(reciboDTO.getIdFormaDePago()));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    recibo.setFecha(LocalDateTime.now());
    return reciboService.guardar(recibo);
  }

  @PostMapping("/recibos/proveedores")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Recibo guardarReciboProveedor(
      @RequestBody ReciboDTO reciboDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Recibo recibo = modelMapper.map(reciboDTO, Recibo.class);
    recibo.setEmpresa(empresaService.getEmpresaPorId(reciboDTO.getIdEmpresa()));
    recibo.setProveedor(proveedorService.getProveedorNoEliminadoPorId(reciboDTO.getIdProveedor()));
    recibo.setFormaDePago(formaDePagoService.getFormasDePagoNoEliminadoPorId(reciboDTO.getIdFormaDePago()));
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    recibo.setUsuario(
        usuarioService.getUsuarioNoEliminadoPorId(((Integer) claims.get("idUsuario")).longValue()));
    return reciboService.guardar(recibo);
  }

  @DeleteMapping("/recibos/{idRecibo}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR})
  public void eliminar(@PathVariable long idRecibo) {
    reciboService.eliminar(idRecibo);
  }

  @GetMapping("/recibos/{idRecibo}/reporte")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VENDEDOR,
    Rol.VIAJANTE,
    Rol.COMPRADOR
  })
  public ResponseEntity<byte[]> getReporteRecibo(@PathVariable long idRecibo) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("content-disposition", "inline; filename=Recibo.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF = reciboService.getReporteRecibo(reciboService.getReciboNoEliminadoPorId(idRecibo));
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }
}
