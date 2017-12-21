package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.CuentaCorriente;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;

public interface RenglonCuentaCorrienteRepository extends PagingAndSortingRepository<RenglonCuentaCorriente, Long> {

    RenglonCuentaCorriente findByFacturaAndEliminado(FacturaVenta fv, boolean eliminado);

    RenglonCuentaCorriente findByNotaAndEliminado(Nota n, boolean eliminado);

    RenglonCuentaCorriente findByPagoAndEliminado(Pago p, boolean eliminado);
    
    RenglonCuentaCorriente findByReciboAndEliminado(Recibo r, boolean eliminado);

    Page<RenglonCuentaCorriente> findAllByCuentaCorrienteAndEliminado(CuentaCorriente cuentaCorriente, boolean eliminado, Pageable page);

    @Query("SELECT SUM(r.monto) FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false")
    Double getSaldoCuentaCorriente(@Param("idCuentaCorriente") long idCuentaCorriente);

}
