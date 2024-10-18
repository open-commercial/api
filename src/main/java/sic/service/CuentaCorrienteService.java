package sic.service;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;
import sic.modelo.criteria.BusquedaCuentaCorrienteClienteCriteria;
import sic.modelo.criteria.BusquedaCuentaCorrienteProveedorCriteria;
import sic.util.FormatoReporte;
import java.math.BigDecimal;
import java.util.List;

public interface CuentaCorrienteService {

  CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente);

  CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor);

  BigDecimal getSaldoCuentaCorriente(long idCliente);

  CuentaCorrienteCliente guardarCuentaCorrienteCliente(CuentaCorrienteCliente cuentaCorrienteCliente);

  CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(CuentaCorrienteProveedor cuentaCorrienteProveedor);

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

  byte[] getReporteCuentaCorrienteCliente(CuentaCorrienteCliente cuentaCorrienteCliente, FormatoReporte formato);

  byte[] getReporteListaDeCuentasCorrienteClientePorCriteria(
          BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn, FormatoReporte formato);

  List<RenglonCuentaCorriente> getUltimosDosMovimientos(CuentaCorriente cuentaCorriente);

  RenglonCuentaCorriente guardar(RenglonCuentaCorriente renglonCuentaCorriente);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeFactura(Factura factura, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeNota(Nota nota, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeRecibo(Recibo recibo, boolean eliminado);

  RenglonCuentaCorriente getRenglonCuentaCorrienteDeRemito(Remito remito, boolean eliminado);

  BooleanBuilder getBuilder(BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  List<CuentaCorrienteCliente> buscarCuentasCorrienteClienteParaReporte(
      BusquedaCuentaCorrienteClienteCriteria criteria, long idUsuarioLoggedIn);

  Pageable getPageable(Integer pagina, String ordenarPor, String sentido, String ordenDefault, int tamanioPagina);
}
