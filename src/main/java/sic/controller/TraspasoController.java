package sic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.RenglonTraspaso;
import sic.modelo.Rol;
import sic.modelo.Traspaso;
import sic.modelo.criteria.BusquedaTraspasoCriteria;
import sic.modelo.dto.NuevoTraspasoDTO;
import sic.service.ITraspasoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TraspasoController {

  private final ITraspasoService traspasoService;

  @Autowired
  public TraspasoController(ITraspasoService traspasoService) {
    this.traspasoService = traspasoService;
  }

  @PostMapping("/traspasos/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Traspaso> getTraspasosCriteria(@RequestBody BusquedaTraspasoCriteria criteria) {
    return traspasoService.buscarTraspasos(criteria);
  }

  @GetMapping("/traspasos/{idTraspaso}/renglones")
  public List<RenglonTraspaso> getRenglonesDelTraspaso(@PathVariable long idTraspaso) {
    return traspasoService.getRenglonesTraspaso(idTraspaso);
  }

  @PostMapping("/traspasos")
  public Traspaso guardarTraspaso(NuevoTraspasoDTO nuevoTraspasoDTO) {
    return traspasoService.guardar(nuevoTraspasoDTO);
  }
}
