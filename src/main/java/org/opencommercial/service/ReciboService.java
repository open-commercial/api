package org.opencommercial.service;

import com.mercadopago.resources.payment.Payment;
import com.querydsl.core.BooleanBuilder;
import org.opencommercial.model.*;
import org.opencommercial.model.criteria.BusquedaReciboCriteria;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReciboService {

  Recibo getReciboNoEliminadoPorId(long idRecibo);

  Optional<Recibo> getReciboPorIdMercadoPago(long idPagoMercadoPago);

  Page<Recibo> buscarRecibos(BusquedaReciboCriteria criteria);

  BooleanBuilder getBuilder(BusquedaReciboCriteria criteria);

  BigDecimal getTotalRecibos(BusquedaReciboCriteria criteria);

  Recibo guardar(Recibo recibo);

  void validarReglasDeNegocio(Recibo recibo);

  List<Recibo> construirRecibos(
      Long[] formaDePago,
      Long idSucursal,
      Cliente cliente,
      Usuario usuario,
      BigDecimal[] monto,
      LocalDateTime fecha);

  Recibo construirReciboPorPayment(
          Sucursal sucursal, Usuario usuario, Cliente cliente, Payment payment);

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
