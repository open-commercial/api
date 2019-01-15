package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.Factura;
import sic.modelo.FacturaVenta;
import sic.modelo.Pedido;
import sic.modelo.TipoDeComprobante;

public interface FacturaVentaRepository extends FacturaRepository<FacturaVenta>, FacturaVentaRepositoryCustom, QueryDslPredicateExecutor<FacturaVenta> {
    
    @Override
    List<Factura> findAllByPedidoAndEliminada(Pedido pedido, boolean eliminada);
       
    @Query("SELECT max(fv.numFactura) FROM FacturaVenta fv WHERE fv.tipoComprobante = :tipoComprobante AND fv.numSerie = :numSerie AND fv.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumFacturaSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("numSerie") long numSerie, @Param("idEmpresa") long idEmpresa);
}
