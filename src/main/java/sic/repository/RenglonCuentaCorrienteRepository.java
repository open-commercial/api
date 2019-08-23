package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

import java.util.List;

public interface RenglonCuentaCorrienteRepository extends PagingAndSortingRepository<RenglonCuentaCorriente, Long> {

    RenglonCuentaCorriente findByFacturaAndEliminado(Factura factura, boolean eliminado);

    RenglonCuentaCorriente findByNotaAndEliminado(Nota nota, boolean eliminado);
    
    RenglonCuentaCorriente findByReciboAndEliminado(Recibo recibo, boolean eliminado);

    @Query("SELECT r FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false"
            + " ORDER BY r.idRenglonCuentaCorriente DESC")
    Page<RenglonCuentaCorriente> findAllByCuentaCorrienteAndEliminado(@Param("idCuentaCorriente") long idCuentaCorriente, Pageable page);

    List<RenglonCuentaCorriente> findTop2ByAndCuentaCorrienteAndEliminadoOrderByIdRenglonCuentaCorrienteDesc(CuentaCorriente cuentaCorriente, boolean eliminado);

    @Modifying
    @Query("UPDATE RenglonCuentaCorriente rcc SET rcc.cae = :cae WHERE rcc.factura.id_Factura = :idFactura")
    int updateCaeFactura(@Param("idFactura") long idFactura, @Param("cae") long cae);
    
    @Modifying
    @Query("UPDATE RenglonCuentaCorriente rcc SET rcc.cae = :cae WHERE rcc.nota.idNota = :idNota")
    int updateCaeNota(@Param("idNota") long idNota, @Param("cae") long cae);

}
