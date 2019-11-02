package sic.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
      Empresa empresa,
      Cliente cliente,
      Usuario usuario,
      BigDecimal[] monto,
      BigDecimal totalFactura,
      LocalDateTime fecha);

  long getSiguienteNumeroRecibo(long idEmpresa, long serie);

  void eliminar(long idRecibo);

  byte[] getReporteRecibo(Recibo recibo);

  BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(
      long idEmpresa, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(
      long idEmpresa, long idFormaDePago, LocalDateTime desde, LocalDateTime hasta);

  List<Recibo> getRecibosEntreFechasPorFormaDePago(
    LocalDateTime desde, LocalDateTime hasta, FormaDePago formaDePago, Empresa empresa);

  BigDecimal getTotalRecibosClientesQueAfectanCajaEntreFechas(
      long idEmpresa, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosProveedoresQueAfectanCajaEntreFechas(
      long idEmpresa, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosClientesEntreFechas(long idEmpresa, LocalDateTime desde, LocalDateTime hasta);

  BigDecimal getTotalRecibosProveedoresEntreFechas(long idEmpresa, LocalDateTime desde, LocalDateTime hasta);
}
