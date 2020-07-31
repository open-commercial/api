package sic.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Remito;
import sic.modelo.RenglonPedido;
import sic.modelo.RenglonRemito;
import sic.modelo.Rol;
import sic.modelo.dto.NuevoRemitoDTO;
import sic.service.IAuthService;
import sic.service.IRemitoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RemitoController {

  private final IRemitoService remitoService;
  private final IAuthService authService;

  @Autowired
  public RemitoController(IRemitoService remitoService, IAuthService authService) {
    this.remitoService = remitoService;
    this.authService = authService;
  }

  @GetMapping("/remitos/{idRemito}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Remito getRemitoPorId(@PathVariable long idRemito) {
    return remitoService.getRemitoPorId(idRemito);
  }

  @PostMapping("/remitos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Remito crearRemitoDeFactura(
      @RequestBody NuevoRemitoDTO nuevoRemitoDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    return remitoService.crearRemitoDeFacturaVenta(nuevoRemitoDTO, idUsuarioLoggedIn);
  }

  @DeleteMapping("/remitos/{idRemito}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idRemito) {
    remitoService.eliminar(idRemito);
  }

  @GetMapping("/remitos/{idRemito}/renglones")
  public List<RenglonRemito> getRenglonesDelRemito(@PathVariable long idRemito) {
    return remitoService.getRenglonesDelRemito(idRemito);
  }
}
