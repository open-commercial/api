package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.Remito;
import org.opencommercial.model.RenglonRemito;
import org.opencommercial.model.Rol;
import org.opencommercial.model.criteria.BusquedaRemitoCriteria;
import org.opencommercial.model.dto.NuevoRemitoDTO;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.RemitoService;

import java.util.List;

@RestController
public class RemitoController {

  private final RemitoService remitoService;
  private final AuthService authService;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public RemitoController(RemitoService remitoService,
                          AuthService authService) {
    this.remitoService = remitoService;
    this.authService = authService;
  }

  @GetMapping("/api/v1/remitos/{idRemito}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Remito getRemitoPorId(@PathVariable long idRemito) {
    return remitoService.getRemitoPorId(idRemito);
  }

  @PostMapping("/api/v1/remitos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Remito crearRemitoDeFactura(@RequestBody NuevoRemitoDTO nuevoRemitoDTO,
                                     @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return remitoService.crearRemitoDeFacturasVenta(nuevoRemitoDTO, idUsuarioLoggedIn);
  }

  @DeleteMapping("/api/v1/remitos/{idRemito}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idRemito) {
    remitoService.eliminar(idRemito);
  }

  @GetMapping("/api/v1/remitos/{idRemito}/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonRemito> getRenglonesDelRemito(@PathVariable long idRemito) {
    return remitoService.getRenglonesDelRemito(idRemito);
  }

  @PostMapping("/api/v1/remitos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Remito> getRemitosCriteria(@RequestBody BusquedaRemitoCriteria criteria) {
    return remitoService.buscarRemito(criteria);
  }

  @GetMapping("/api/v1/remitos/{idRemito}/reporte")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public ResponseEntity<byte[]> getReporteRemito(@PathVariable long idRemito) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("content-disposition", "inline; filename=Remito.pdf");
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    byte[] reportePDF = remitoService.getReporteRemito(idRemito);
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }
}
