package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.*;

public interface ICuentaCorrienteService {

      void eliminar(Long idCuentaCorriente);

      CuentaCorriente getCuentaCorrientePorID(Long idCuentaCorriente);
      
      CuentaCorrienteCliente getCuentaCorrientePorCliente(Cliente cliente);
      
      CuentaCorrienteProveedor getCuentaCorrientePorProveedor(Proveedor proveedor);

      CuentaCorrienteCliente guardarCuentaCorrienteCliente(CuentaCorrienteCliente cuentaCorrienteCliente);
      
      CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(CuentaCorrienteProveedor cuentaCorrienteProveedor);

      void validarCuentaCorriente(CuentaCorriente cuentaCorriente);
  
      BigDecimal getSaldoCuentaCorriente(long idCuentaCorriente);
      
      Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable pageable); 
      
      void asentarEnCuentaCorriente(FacturaVenta facturaVenta, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(FacturaCompra facturaCompra, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(Nota nota, TipoDeOperacion tipo);

      void asentarEnCuentaCorriente(Recibo recibo, TipoDeOperacion tipo);
      
      Date getFechaUltimoMovimiento(long idCuentaCorriente);

      byte[] getReporteCuentaCorrienteClienteXlsx(CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page);

      byte[] getReporteCuentaCorrienteClientePDF(CuentaCorrienteCliente cuentaCorrienteCliente, Pageable page);

}
