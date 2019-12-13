package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaCuentaCorrienteClienteCriteria;
import sic.modelo.criteria.BusquedaCuentaCorrienteProveedorCriteria;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

public interface ICuentaCorrienteService {

  CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente);

  CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor);

  BigDecimal getSaldoCuentaCorriente(long idCliente);

  CuentaCorrienteCliente guardarCuentaCorrienteCliente(
      @Valid CuentaCorrienteCliente cuentaCorrienteCliente);

  CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(
      @Valid CuentaCorrienteProveedor cuentaCorrienteProveedor);

  void validarOperacion(CuentaCorriente cuentaCorriente);

  void eliminarCuentaCorrienteCliente(long idCliente);

  void eliminarCuentaCorrienteProveedor(long idProveedor);

  Page<CuentaCorrienteCliente> buscarCuentaCorrienteCliente(
    BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  Page<CuentaCorrienteProveedor> buscarCuentaCorrienteProveedor(BusquedaCuentaCorrienteProveedorCriteria criteria);

  Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Integer pagina);

  List<RenglonCuentaCorriente> getRenglonesCuentaCorrienteParaReporte(long idCuentaCorriente);

  void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(FacturaCompra facturaCompra);

  void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo);

  void asentarEnCuentaCorriente(Recibo recibo, TipoDeOperacion tipo);

  byte[] getReporteCuentaCorrienteCliente(
      CuentaCorrienteCliente cuentaCorrienteCliente, String formato);

  List<RenglonCuentaCorriente> getUltimosDosMovimientos(CuentaCorriente cuentaCorriente);

  RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura factura, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo recibo, boolean eliminado);
}
