package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.AjusteCuentaCorriente;
import sic.modelo.Cliente;
import sic.modelo.CuentaCorriente;
import sic.modelo.CuentaCorrienteCliente;
import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.Factura;
import sic.modelo.Nota;
import sic.modelo.Proveedor;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.TipoDeOperacion;

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
      
      void asentarEnCuentaCorriente(Factura fv, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(Nota n, TipoDeOperacion tipo);

      void asentarEnCuentaCorriente(Recibo r, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(AjusteCuentaCorriente ajusteCC, TipoDeOperacion tipo);
      
      Date getFechaUltimoMovimiento(long idCuentaCorriente);
      
}
