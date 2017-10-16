package sic.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.RenglonCuentaCorriente;

public interface RenglonCuentaCorrienteRepository extends PagingAndSortingRepository<RenglonCuentaCorriente, Long> {
    
    RenglonCuentaCorriente findByFacturaAndEliminado(FacturaVenta fv, boolean eliminado);
    
    RenglonCuentaCorriente findByNotaAndEliminado(Nota n, boolean eliminado);
    
    RenglonCuentaCorriente findByPagoAndEliminado(Pago p, boolean eliminado);
    
}
