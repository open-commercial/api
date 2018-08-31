package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.FacturaCompra;
import sic.modelo.FacturaVenta;
import sic.modelo.NotaCredito;
import sic.modelo.NotaCreditoCliente;
import sic.modelo.TipoDeComprobante;

public interface NotaCreditoClienteRepository extends NotaCreditoRepository<NotaCreditoCliente>, QueryDslPredicateExecutor<NotaCreditoCliente> {
    
    List<NotaCredito> findAllByFacturaVentaAndEliminada(FacturaVenta factura, boolean eliminada);
    
    @Query("SELECT max(ncc.nroNota) FROM NotaCreditoCliente ncc WHERE ncc.tipoComprobante = :tipoComprobante AND ncc.serie = :serie AND ncc.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumNotaCreditoClienteSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);
    
    @Query("SELECT SUM(ncc.total) FROM NotaCreditoCliente ncc WHERE ncc.facturaVenta = :facturaVenta AND ncc.eliminada = false")
    BigDecimal getTotalNotasCreditoPorFacturaVenta(@Param("facturaVenta") FacturaVenta facturaVenta);
    
    @Override
    @Query("SELECT ncc FROM NotaCreditoCliente ncc WHERE ncc.idNota = :idNotaCreditoCliente AND ncc.eliminada = false")
    NotaCreditoCliente getById(@Param("idNotaCreditoCliente") long idNotaCreditoCliente);
    
    boolean existsByFacturaVentaAndEliminada(FacturaVenta facturaVenta, boolean eliminada);

}
