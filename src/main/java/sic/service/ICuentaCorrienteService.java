package sic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;

import java.util.List;

public interface ICuentaCorrienteService {

  CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente);

  CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor);

  CuentaCorrienteCliente guardarCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente);

  CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(
      CuentaCorrienteProveedor cuentaCorrienteProveedor);

  void validarCuentaCorriente(CuentaCorriente cuentaCorriente);

  Page<CuentaCorrienteCliente> buscarCuentaCorrienteCliente(BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(
      long idCuentaCorriente, Pageable pageable);

  void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(FacturaCompra facturaCompra, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(Recibo recibo, TipoDeOperacion tipo);

  byte[] getReporteCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page, String formato);

  List<RenglonCuentaCorriente> getUltimosDosMovimientos(CuentaCorriente cuentaCorriente);

  RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura factura, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo recibo, boolean eliminado);

  int updateCAEFactura(long idFactura, long CAE);

  int updateCAENota(long idNota, long CAE);
}
