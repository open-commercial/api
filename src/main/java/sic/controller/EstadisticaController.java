package sic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.Rol;
import sic.modelo.dto.EntidadMontoDTO;
import sic.modelo.dto.PeriodoMontoDTO;
import sic.service.IEstadisticaService;
import java.util.List;

@RestController
public class EstadisticaController {

  public final IEstadisticaService estadisticaService;

  @Autowired
  public EstadisticaController(IEstadisticaService estadisticaService) {
    this.estadisticaService = estadisticaService;
  }

  @GetMapping("/api/v1/estadisticas/compras/monto-neto-mensual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<PeriodoMontoDTO> getMontoNetoCompradoPorMes(@PathVariable long idSucursal,
                                                          @RequestParam int anio) {
    return estadisticaService.getMontoNetoCompradoPorMes(idSucursal, anio);
  }

  @GetMapping("/api/v1/estadisticas/compras/monto-neto-anual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<PeriodoMontoDTO> getMontoNetoCompradoPorAnio(@PathVariable long idSucursal,
                                                           @RequestParam(required = false, defaultValue = "4") int limite) {
    return estadisticaService.getMontoNetoCompradoPorAnio(idSucursal, limite);
  }

  @GetMapping("/api/v1/estadisticas/compras/proveedores/monto-neto-anual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorAnio(@PathVariable long idSucursal,
                                                                       @RequestParam int anio) {
    return estadisticaService.getMontoNetoCompradoPorProveedorPorAnio(idSucursal, anio);
  }

  @GetMapping("/api/v1/estadisticas/compras/proveedores/monto-neto-mensual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorMes(@PathVariable long idSucursal,
                                                                      @RequestParam int anio,
                                                                      @RequestParam int mes) {
    return estadisticaService.getMontoNetoCompradoPorProveedorPorMes(idSucursal, anio, mes);
  }
}
