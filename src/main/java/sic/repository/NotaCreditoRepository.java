package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

import java.math.BigDecimal;
import java.util.List;

public interface NotaCreditoRepository extends NotaRepository<NotaCredito>, NotaCreditoRepositoryCustom {
    
    @Query("SELECT nc FROM NotaCredito nc WHERE nc.idNota = :idNotaCredito AND nc.eliminada = false")
    NotaCredito getById(@Param("idNotaCredito") long idNotaCredito);

    List<NotaCredito> findAllByFacturaVentaAndEliminada(FacturaVenta factura, boolean eliminada);

    @Query("SELECT max(nc.nroNota) FROM NotaCredito nc " +
            "WHERE nc.tipoComprobante = :tipoComprobante AND nc.serie = :serie AND nc.empresa.id_Empresa = :idEmpresa " +
            "AND nc.cliente IS NOT null")
    Long buscarMayorNumNotaCreditoClienteSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);

    @Query("SELECT SUM(nc.total) FROM NotaCredito nc WHERE nc.facturaVenta = :facturaVenta AND nc.eliminada = false")
    BigDecimal getTotalNotasCreditoPorFacturaVenta(@Param("facturaVenta") FacturaVenta facturaVenta);

    boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta, boolean eliminada);

    List<NotaCredito> findAllByFacturaCompraAndEliminada(FacturaCompra factura, boolean eliminada);
    
}
