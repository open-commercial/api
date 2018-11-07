package sic.service;

import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;

public interface ICuentaCorrienteService {

  CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente);

  CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor);

  CuentaCorrienteCliente guardarCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente);

  CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(
      CuentaCorrienteProveedor cuentaCorrienteProveedor);

  void validarCuentaCorriente(CuentaCorriente cuentaCorriente);

  Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(
      long idCuentaCorriente, Pageable pageable);

  void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(FacturaCompra facturaCompra, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(Recibo recibo, TipoDeOperacion tipo);

  Date getFechaUltimoMovimiento(long idCuentaCorriente);

  byte[] getReporteCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page, String formato);

  RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura factura, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo recibo, boolean eliminado);

  int updateCAEFactura(long idFactura, long CAE);

  int updateCAENota(long idNota, long CAE);
}
