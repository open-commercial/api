package sic.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;

import javax.validation.Valid;
import java.util.List;

public interface ICuentaCorrienteService {

  CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente);

  CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor);

  CuentaCorrienteCliente guardarCuentaCorrienteCliente(
      @Valid CuentaCorrienteCliente cuentaCorrienteCliente);

  CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(
      @Valid CuentaCorrienteProveedor cuentaCorrienteProveedor);

  void validarOperacion(@Valid CuentaCorriente cuentaCorriente);

  void eliminarCuentaCorrienteCliente(long idCliente);

  void eliminarCuentaCorrienteProveedor(long idProveedor);

  Page<CuentaCorrienteCliente> buscarCuentaCorrienteCliente(
      BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  Page<CuentaCorrienteProveedor> buscarCuentaCorrienteProveedor(BusquedaCuentaCorrienteProveedorCriteria criteria);

  Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(
      long idCuentaCorriente, Pageable pageable);

  void asentarEnCuentaCorriente(FacturaVenta facturaVenta);

  void asentarEnCuentaCorriente(FacturaCompra facturaCompra);

  void asentarEnCuentaCorriente(Nota nota);

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
