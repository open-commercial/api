package sic.service;

import sic.modelo.dto.EntidadMontoDTO;
import sic.modelo.dto.PeriodoMontoDTO;
import java.util.List;

public interface IEstadisticaService {

  List<PeriodoMontoDTO> getMontoNetoCompradoPorAnio(long idSucursal);

  List<PeriodoMontoDTO> getMontoNetoCompradoPorMes(long idSucursal, int anio);

  List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorAnio(long idSucursal, int anio);

  List<EntidadMontoDTO> getMontoNetoCompradoPorProveedorPorMes(long idSucursal, int anio, int mes);

  List<PeriodoMontoDTO> getMontoNetoVendidoPorAnio(long idSucursal, int limite);

  List<PeriodoMontoDTO> getMontoNetoVendidoPorMes(long idSucursal, int anio);

  List<EntidadMontoDTO> getMontoNetoVendidoPorRubroPorAnio(long idSucursal, int anio);

  List<EntidadMontoDTO> getMontoNetoVendidoPorRubroPorMes(long idSucursal, int anio, int mes);

}
