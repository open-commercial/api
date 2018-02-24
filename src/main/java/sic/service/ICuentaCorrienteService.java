package sic.service;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.CuentaCorriente;
import sic.modelo.CuentaCorrienteCliente;
import sic.modelo.CuentaCorrienteProveedor;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.TipoDeOperacion;

public interface ICuentaCorrienteService {

      void eliminar(Long idCuentaCorriente);

      CuentaCorriente getCuentaCorrientePorID(Long idCuentaCorriente);
      
      CuentaCorrienteCliente getCuentaCorrientePorCliente(long idCliente);
      
      CuentaCorrienteProveedor getCuentaCorrientePorProveedor(long idProveedor);

      CuentaCorrienteCliente guardarCuentaCorrienteCliente(CuentaCorrienteCliente cuentaCorrienteCliente);
      
      CuentaCorrienteProveedor guardarCuentaCorrienteProveedor(CuentaCorrienteProveedor cuentaCorrienteProveedor);

      void validarCuentaCorriente(CuentaCorriente cuentaCorriente);
  
      BigDecimal getSaldoCuentaCorriente(long idCuentaCorriente);
      
      Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable pageable); 
      
      void asentarEnCuentaCorriente(FacturaVenta fv, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(FacturaCompra fc, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(Nota n, TipoDeOperacion tipo);

      void asentarEnCuentaCorriente(Recibo r, TipoDeOperacion tipo);
      
      Date getFechaUltimoMovimiento(long idCuentaCorriente);
      
}
