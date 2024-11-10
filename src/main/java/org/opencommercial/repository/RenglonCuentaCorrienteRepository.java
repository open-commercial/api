package org.opencommercial.repository;

import org.opencommercial.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RenglonCuentaCorrienteRepository extends JpaRepository<RenglonCuentaCorriente, Long> {

    RenglonCuentaCorriente findByFacturaAndEliminado(Factura factura, boolean eliminado);

    RenglonCuentaCorriente findByNotaAndEliminado(Nota nota, boolean eliminado);
    
    RenglonCuentaCorriente findByReciboAndEliminado(Recibo recibo, boolean eliminado);

    RenglonCuentaCorriente findByRemitoAndEliminado(Remito remito, boolean eliminado);

    @Query("SELECT r FROM CuentaCorriente cc INNER JOIN cc.renglones r"
            + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false"
            + " ORDER BY r.idRenglonCuentaCorriente DESC")
    Page<RenglonCuentaCorriente> findAllByCuentaCorrienteAndEliminado(@Param("idCuentaCorriente") long idCuentaCorriente,
                                                                      Pageable page);

    @Query("SELECT r FROM CuentaCorriente cc INNER JOIN cc.renglones r"
      + " WHERE cc.idCuentaCorriente = :idCuentaCorriente AND cc.eliminada = false AND r.eliminado = false"
      + " ORDER BY r.idRenglonCuentaCorriente DESC")
    List<RenglonCuentaCorriente> findAllByCuentaCorrienteAndEliminado(@Param("idCuentaCorriente") long idCuentaCorriente);

    List<RenglonCuentaCorriente> findTop2ByAndCuentaCorrienteAndEliminadoOrderByIdRenglonCuentaCorrienteDesc(
            CuentaCorriente cuentaCorriente, boolean eliminado);
}
