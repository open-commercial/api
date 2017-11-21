package sic.service;

import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sic.modelo.CuentaCorriente;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.RenglonCuentaCorriente;
import sic.modelo.TipoDeOperacion;

public interface ICuentaCorrienteService {

      void eliminar(Long idCuentaCorriente);

      CuentaCorriente getCuentaCorrientePorID(Long idCuentaCorriente);
      
      CuentaCorriente getCuentaCorrientePorCliente(long idCliente);

      CuentaCorriente guardar(CuentaCorriente cuentaCorriente);

      void validarCuentaCorriente(CuentaCorriente cuentaCorriente);
  
      double getSaldoCuentaCorriente(long idCuentaCorriente, Date hasta);
      
      Page<RenglonCuentaCorriente> getRenglonesCuentaCorriente(long idCuentaCorriente, Pageable pageable); 
      
      void asentarEnCuentaCorriente(FacturaVenta fv, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(Nota n, TipoDeOperacion tipo);
      
      void asentarEnCuentaCorriente(Pago p, TipoDeOperacion tipo, Long idCliente);
      
}
