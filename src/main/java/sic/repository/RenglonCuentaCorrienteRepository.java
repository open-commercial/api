package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Factura;
import sic.modelo.Nota;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;

public interface RenglonCuentaCorrienteRepository extends PagingAndSortingRepository<RenglonCuentaCorriente, Long> {

    RenglonCuentaCorriente findByFacturaAndEliminado(Factura factura, boolean eliminado);

    RenglonCuentaCorriente findByNotaAndEliminado(Nota nota, boolean eliminado);
    
    RenglonCuentaCorriente findByReciboAndEliminado(Recibo recibo, boolean eliminado);

    @Query("SELECT r FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false"
            + " ORDER BY r.idRenglonCuentaCorriente DESC")
    Page<RenglonCuentaCorriente> findAllByCuentaCorrienteAndEliminado(@Param("idCuentaCorriente") long idCuentaCorriente, Pageable page);

    @Query("SELECT SUM(r.monto) FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false")
    BigDecimal getSaldoCuentaCorriente(@Param("idCuentaCorriente") long idCuentaCorriente);
    
    @Query("SELECT max(r.fecha) FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false")
    Date getFechaUltimoMovimiento(@Param("idCuentaCorriente") long idCuentaCorriente);

}
