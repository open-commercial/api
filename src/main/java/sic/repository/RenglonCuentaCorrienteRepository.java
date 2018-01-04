package sic.repository;

import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.CuentaCorriente;
import sic.modelo.FacturaVenta;
import sic.modelo.Nota;
import sic.modelo.Recibo;
import sic.modelo.RenglonCuentaCorriente;

public interface RenglonCuentaCorrienteRepository extends PagingAndSortingRepository<RenglonCuentaCorriente, Long> {

    RenglonCuentaCorriente findByFacturaAndEliminado(FacturaVenta fv, boolean eliminado);

    RenglonCuentaCorriente findByNotaAndEliminado(Nota n, boolean eliminado);
    
    RenglonCuentaCorriente findByReciboAndEliminado(Recibo r, boolean eliminado);

    Page<RenglonCuentaCorriente> findAllByCuentaCorrienteAndEliminado(CuentaCorriente cuentaCorriente, boolean eliminado, Pageable page);
    
    @Query("SELECT r FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false "
            + " AND (r.tipoComprobante = \'FACTURA_A\' OR r.tipoComprobante = \'FACTURA_B\' OR r.tipoComprobante = \'FACTURA_C\'"
            + " OR r.tipoComprobante = \'FACTURA_X\' OR r.tipoComprobante = \'FACTURA_Y\' OR r.tipoComprobante = \'PRESUPUESTO\'"
            + " OR r.tipoComprobante = \'NOTA_DEBITO_A\' OR r.tipoComprobante = \'NOTA_DEBITO_B\' OR r.tipoComprobante = \'NOTA_DEBITO_X\'"
            + " OR r.tipoComprobante = \'NOTA_DEBITO_Y\' OR r.tipoComprobante = \'NOTA_DEBITO_PRESUPUESTO\')"
            + " ORDER BY r.fecha, r.tipoComprobante ASC")
    Slice<RenglonCuentaCorriente> getRenglonesVentaYDebitoCuentaCorriente(@Param("idCuentaCorriente") long idCuentaCorriente, Pageable page);

    @Query("SELECT SUM(r.monto) FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false")
    Double getSaldoCuentaCorriente(@Param("idCuentaCorriente") long idCuentaCorriente);
    
    @Query("SELECT max(r.fecha) FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false")
    Date getFechaUltimoMovimiento(@Param("idCuentaCorriente") long idCuentaCorriente);

}
