package sic.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaReciboCriteria;

import javax.validation.Valid;

public interface IReciboService {

  Recibo getReciboNoEliminadoPorId(long idRecibo);

  Optional<Recibo> getReciboPorIdMercadoPago(String idPagoMercadoPago);

  Page<Recibo> buscarRecibos(BusquedaReciboCriteria criteria);

  BooleanBuilder getBuilder(BusquedaReciboCriteria criteria);

  BigDecimal getTotalRecibos(BusquedaReciboCriteria criteria);

  Recibo guardar(@Valid Recibo recibo);

  void validarOperacion(Recibo recibo);

  List<Recibo> construirRecibos(
      Long[] formaDePago,
      Sucursal sucursal,
      Cliente cliente,
      Usuario usuario,
      BigDecimal[] monto,
      BigDecimal totalFactura,
      LocalDateTime fecha);

  long getSiguienteNumeroRecibo(long idSucursal, long serie);

  void eliminar(long idRecibo);

  byte[] getReporteRecibo(Recibo recibo);

  BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      long idSucursal, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      long idSucursal, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta);

  List<Recibo> getRecibosEntreFechasPorFormaDePago(
    LocalDateTime desde, LocalDateTime hasta, FormaDePago formaDePago, Sucursal sucursal);

  BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      long idSucursal, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      long idSucursal, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosClientesEntreFechas(long idSucursal, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosProveedoresEntreFechas(long idSucursal, LocalDateTime desde, LocalDateTime hasta);
}
