package sic.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.criteria.BusquedaGastoCriteria;
import sic.modelo.Sucursal;
import sic.modelo.FormaDePago;
import sic.modelo.Gasto;

public interface IGastoService {

  Gasto getGastoNoEliminadoPorId(Long id);

  void eliminar(long idGasto);

  List<Gasto> getGastosEntreFechasYFormaDePago(
      Sucursal sucursal, FormaDePago formaDePago, LocalDateTime desde, LocalDateTime hasta);

  Gasto guardar(Gasto gasto);

  void validarReglasDeNegocio(Gasto gasto);

  Page<Gasto> buscarGastos(BusquedaGastoCriteria criteria);

  long getUltimoNumeroDeGasto(long idSucursal);

  BigDecimal getTotalGastosEntreFechasYFormaDePago(
      long idSucursal, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalGastosQueAfectanCajaEntreFechas(
      long idSucursal, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalGastosEntreFechas(long idSucursal, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalGastos(BusquedaGastoCriteria criteria);
}
