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
import sic.modelo.RenglonTraspaso;
import sic.modelo.Rol;
import sic.modelo.Traspaso;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.service.AuthService;
import sic.service.TraspasoService;

import java.util.List;
import java.util.Locale;

@RestController
public class TraspasoController {

  private final TraspasoService traspasoService;
  private final AuthService authService;
  private final MessageSource messageSource;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public TraspasoController(TraspasoService traspasoService,
                            AuthService authService,
                            MessageSource messageSource) {
    this.traspasoService = traspasoService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @GetMapping("/api/v1/traspasos/{idTraspaso}")
  public Traspaso getTraspasoPorId(@PathVariable long idTraspaso) {
    return traspasoService.getTraspasoNoEliminadoPorid(idTraspaso);
  }

  @PostMapping("/api/v1/traspasos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Traspaso> getTraspasosCriteria(@RequestBody BusquedaTraspasoCriteria criteria) {
    return traspasoService.buscarTraspasos(criteria);
  }

  @GetMapping("/api/v1/traspasos/{idTraspaso}/renglones")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public List<RenglonTraspaso> getRenglonesDelTraspaso(@PathVariable long idTraspaso) {
    return traspasoService.getRenglonesTraspaso(idTraspaso);
  }

  @PostMapping("/api/v1/traspasos")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public Traspaso guardarTraspaso(@RequestBody NuevoTraspasoDTO nuevoTraspasoDTO,
                                  @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuarioLoggedIn = claims.get(CLAIM_ID_USUARIO, Long.class);
    return traspasoService.guardarTraspaso(nuevoTraspasoDTO, idUsuarioLoggedIn);
  }

  @DeleteMapping("/api/v1/traspasos/{idTraspaso}")
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

  @PostMapping("/api/v1/traspasos/reporte/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public ResponseEntity<byte[]> getReporteTraspaso(@RequestBody BusquedaTraspasoCriteria criteria) {
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.add("Content-Disposition", "attachment; filename=Traspasos.pdf");
    byte[] reportePDF = traspasoService.getReporteTraspaso(criteria);
    return new ResponseEntity<>(reportePDF, headers, HttpStatus.OK);
  }
}
