package sic.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.domain.TipoDeOperacion;
import sic.entity.*;
import sic.entity.criteria.BusquedaCuentaCorrienteClienteCriteria;
import sic.entity.criteria.BusquedaCuentaCorrienteProveedorCriteria;

import java.math.BigDecimal;
import java.util.List;

public interface ICuentaCorrienteService {

  CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente);

  CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor);

  BigDecimal getSaldoCuentaCorriente(long idCliente);

  CuentaCorrienteCliente guardarCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente);

  CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(
      CuentaCorrienteProveedor cuentaCorrienteProveedor);

  void validarReglasDeNegocio(CuentaCorriente cuentaCorriente);

  void eliminarCuentaCorrienteCliente(long idCliente);

  void eliminarCuentaCorrienteProveedor(long idProveedor);

  Page<CuentaCorrienteCliente> buscarCuentaCorrienteCliente(
      BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  Page<CuentaCorrienteProveedor> buscarCuentaCorrienteProveedor(
      BusquedaCuentaCorrienteProveedorCriteria criteria);

  Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Integer pagina);

  List<RenglonCuentaCorriente> getRenglonesCuentaCorrienteParaReporte(long idCuentaCorriente);

  void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(FacturaCompra facturaCompra);

  void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(Recibo recibo, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(Remito remito, TipoDeOperacion tipo);

  byte[] getReporteCuentaCorrienteCliente(
          CuentaCorrienteCliente cuentaCorrienteCliente, String formato);

  List<RenglonCuentaCorriente> getUltimosDosMovimientos(CuentaCorriente cuentaCorriente);

  RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura factura, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo recibo, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeRemito(Remito remito, boolean eliminado);

  byte[] getReporteListaDeCuentasCorrienteClientePorCriteria(
          BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn, String formato);

  BooleanBuilder getBuilder(
      BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  List<CuentaCorrienteCliente> buscarCuentasCorrienteClienteParaReporte(
      BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  Pageable getPageable(Integer pagina, String ordenarPor, String sentido, String ordenDefault, int tamanioPagina);
}
