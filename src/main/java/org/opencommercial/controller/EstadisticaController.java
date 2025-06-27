package org.opencommercial.controller;

import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.model.Rol;
import org.opencommercial.model.dto.EntidadMontoDTO;
import org.opencommercial.model.dto.PeriodoMontoDTO;
import org.opencommercial.service.EstadisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EstadisticaController {

  public final EstadisticaService estadisticaService;

  @Autowired
  public EstadisticaController(EstadisticaService estadisticaService) {
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
                                                           @RequestParam(required = false, defaultValue = "5") int limite) {
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

  @GetMapping("/api/v1/estadisticas/ventas/monto-neto-anual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<PeriodoMontoDTO> getMontoNetoVendidoPorAnio(@PathVariable long idSucursal,
                                                          @RequestParam(required = false, defaultValue = "5") int limite) {
    return estadisticaService.getMontoNetoVendidoPorAnio(idSucursal, limite);
  }

  @GetMapping("/api/v1/estadisticas/ventas/monto-neto-mensual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<PeriodoMontoDTO> getMontoNetoVendidoPorMes(@PathVariable long idSucursal,
                                                         @RequestParam int anio) {
    return estadisticaService.getMontoNetoVendidoPorMes(idSucursal, anio);
  }

  @GetMapping("/api/v1/estadisticas/ventas/clientes/monto-neto-anual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<EntidadMontoDTO> getMontoNetoVendidoPorClientePorAnio(@PathVariable long idSucursal,
                                                                    @RequestParam int anio) {
    return estadisticaService.getMontoNetoVendidoPorClientePorAnio(idSucursal, anio);
  }

  @GetMapping("/api/v1/estadisticas/ventas/clientes/monto-neto-mensual/sucursales/{idSucursal}")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO})
  public List<EntidadMontoDTO> getMontoNetoVendidoPorClientePorMes(@PathVariable long idSucursal,
                                                                   @RequestParam int anio,
                                                                   @RequestParam int mes) {
    return estadisticaService.getMontoNetoVendidoPorClientePorMes(idSucursal, anio, mes);
  }

}
