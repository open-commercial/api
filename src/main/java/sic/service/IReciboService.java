package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaReciboCriteria;

import javax.validation.Valid;

public interface IReciboService {

  Recibo getReciboNoEliminadoPorId(long idRecibo);

  Optional<Recibo> getReciboPorIdMercadoPago(String idPagoMercadoPago);

  Page<Recibo> buscarRecibos(BusquedaReciboCriteria criteria);

  BigDecimal getTotalRecibos(BusquedaReciboCriteria criteria);

  Recibo guardar(@Valid Recibo recibo);

  void validarOperacion(Recibo recibo);

  List<Recibo> construirRecibos(
      long[] formaDePago,
      Sucursal sucursal,
      Cliente cliente,
      Usuario usuario,
      BigDecimal[] monto,
      BigDecimal totalFactura,
      Date fecha);

  long getSiguienteNumeroRecibo(long idSucursal, long serie);

  void eliminar(long idRecibo);

  byte[] getReporteRecibo(Recibo recibo);

  BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      long idSucursal, long idFormaDePago, Date desde, Date hasta);

  BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      long idSucursal, long idFormaDePago, Date desde, Date hasta);

  List<Recibo> getRecibosEntreFechasPorFormaDePago(
      Date desde, Date hasta, FormaDePago formaDePago, Sucursal sucursal);

  BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      long idSucursal, Date desde, Date hasta);

  BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      long idSucursal, Date desde, Date hasta);

  BigDecimal getTotalRecibosClientesEntreFechas(long idSucursal, Date desde, Date hasta);

  BigDecimal getTotalRecibosProveedoresEntreFechas(long idSucursal, Date desde, Date hasta);
}
