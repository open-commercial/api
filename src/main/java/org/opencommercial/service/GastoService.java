package org.opencommercial.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.opencommercial.model.criteria.BusquedaGastoCriteria;
import org.opencommercial.model.Sucursal;
import org.opencommercial.model.FormaDePago;
import org.opencommercial.model.Gasto;

public interface GastoService {

  Gasto getGastoNoEliminadoPorId(Long id);

  void eliminar(long idGasto);

  List<Gasto> getGastosEntreFechasYFormaDePago(
      Sucursal sucursal, FormaDePago formaDePago, LocalDateTime desde, LocalDateTime hasta);

  Pageable getPageable(Integer pagina, String ordenarPor, String sentido);

  BooleanBuilder getBuilder(BusquedaGastoCriteria criteria);

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
