package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.Pedido;
import sic.modelo.TipoDeComprobante;

public interface FacturaRepository<T extends Factura> extends PagingAndSortingRepository<T, Long> {
    
    @Query("SELECT f FROM Factura f WHERE f.id_Factura = :idFactura AND f.eliminada = false")
    Factura findById(@Param("idFactura") long idFactura);
    
    @Query("SELECT f.total FROM Factura f WHERE f.id_Factura = :idFactura AND f.eliminada = false")
    Double getTotalById(@Param("idFactura") long idFactura);
    
    Factura findByTipoComprobanteAndNumSerieAndNumFacturaAndEmpresaAndEliminada(TipoDeComprobante tipoComprobante, long serie, long num, Empresa empresa, boolean eliminada);
    
    List<Factura> findAllByPedidoAndEliminada(Pedido pedido, boolean eliminada);
    
    @Query("SELECT f.CAE FROM Factura f WHERE f.id_Factura = :idFactura AND f.eliminada = false")
    Long getCAEById(@Param("idFactura") long idFactura);
    
}
