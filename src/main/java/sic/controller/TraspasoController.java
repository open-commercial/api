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
import sic.entity.RenglonTraspaso;
import sic.domain.Rol;
import sic.entity.Traspaso;
import sic.entity.criteria.BusquedaTraspasoCriteria;
import sic.dto.NuevoTraspasoDTO;
import sic.service.IAuthService;
import sic.service.ITraspasoService;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1")
public class TraspasoController {

  private final ITraspasoService traspasoService;
  private final IAuthService authService;
  private final MessageSource messageSource;

  @Autowired
  public TraspasoController(
      ITraspasoService traspasoService, IAuthService authService, MessageSource messageSource) {
    this.traspasoService = traspasoService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @GetMapping("/traspasos/{idTraspaso}")
  public Traspaso getTraspasoPorId(@PathVariable long idTraspaso) {
    return traspasoService.getTraspasoNoEliminadoPorid(idTraspaso);
  }

  @PostMapping("/traspasos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Traspaso> getTraspasosCriteria(@RequestBody BusquedaTraspasoCriteria criteria) {
    return traspasoService.buscarTraspasos(criteria);
  }

  @GetMapping("/traspasos/{idTraspaso}/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonTraspaso> getRenglonesDelTraspaso(@PathVariable long idTraspaso) {
    return traspasoService.getRenglonesTraspaso(idTraspaso);
  }

  @PostMapping("/traspasos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Traspaso guardarTraspaso(@RequestBody NuevoTraspasoDTO nuevoTraspasoDTO,
                                  @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = (int) claims.get("idUsuario");
    return traspasoService.guardarTraspaso(nuevoTraspasoDTO, idUsuarioLoggedIn);
  }

  @DeleteMapping("/traspasos/{idTraspaso}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public void eliminarTraspaso(@PathVariable long idTraspaso) {
    Traspaso traspaso = traspasoService.getTraspasoNoEliminadoPorid(idTraspaso);
    if (traspaso.getNroPedido() != null) {
      throw new BusinessServiceException(
          messageSource.getMessage(
              "mensaje_traspaso_error_eliminar_con_pedido",
              new Object[] {traspaso},
              Locale.getDefault()));
    }
    traspasoService.eliminar(idTraspaso);
  }

  @PostMapping("/traspasos/reporte/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public ResponseEntity<byte[]> getReporteTraspaso(
      @RequestBody BusquedaTraspasoCriteria criteria) {
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("Content-Disposition", "attachment; filename=Traspasos.pdf");
    byte[] reportePDF = traspasoService.getReporteTraspaso(criteria);
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }
}
