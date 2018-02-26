package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Empresa;
import sic.modelo.Factura;
import sic.modelo.Nota;
import sic.modelo.Pago;
import sic.modelo.Recibo;

public interface PagoRepository extends PagingAndSortingRepository<Pago, Long> {

    @Query("SELECT p FROM Pago p WHERE p.id_Pago = :idPago AND p.eliminado = false")
    Pago findById(@Param("idPago") long idPago);
    
    List<Pago> findByFacturaAndEliminado(Factura factura, boolean eliminado);

    @Query("SELECT p FROM FacturaVenta fv INNER JOIN fv.pagos p WHERE fv.cliente.id_Cliente = :idCliente AND p.eliminado = false AND p.fecha BETWEEN :desde AND :hasta")
    Page<Pago> getPagosPorClienteEntreFechas(@Param("idCliente") long idCliente, @Param("desde") Date desde, @Param("hasta") Date hasta, Pageable page);
    
    @Query("SELECT p FROM FacturaCompra fp INNER JOIN fp.pagos p WHERE p.formaDePago.id_FormaDePago = :idFormaDePago AND p.empresa.id_Empresa = :idEmpresa AND p.eliminado = false AND p.fecha BETWEEN :desde AND :hasta")
    List<Pago> getPagosComprasPorClienteEntreFechas(@Param("idEmpresa") long idEmpresa, @Param("idFormaDePago") long idFormaDePago, @Param("desde") Date desde, @Param("hasta") Date hasta);
    
    Pago findTopByEmpresaOrderByNroPagoDesc(Empresa empresa);
    
    @Query("SELECT SUM(p.monto) FROM FacturaVenta fv INNER JOIN fv.pagos p WHERE fv.cliente.id_Cliente = :idCliente AND p.eliminado = false AND p.fecha <= :hasta")
    BigDecimal getSaldoPagosPorCliente(@Param("idCliente") long idCliente, @Param("hasta") Date hasta);
    
    List<Pago> findByNotaDebitoAndEliminado(Nota nota, boolean eliminado);
    
    @Query("SELECT SUM(p.monto) FROM Factura f INNER JOIN f.pagos p WHERE f.id_Factura = :idFactura AND p.eliminado = false")
    BigDecimal getTotalPagosDeFactura(@Param("idFactura") long idFactura);
    
    @Query("SELECT SUM(p.monto) FROM Pago p INNER JOIN p.notaDebito n WHERE n.idNota = :idNota AND p.eliminado = false")
    BigDecimal getTotalPagosDeNota(@Param("idNota") long idNota);
    
    List<Pago> findAllByReciboAndEliminado(Recibo recibo, boolean eliminado);
    
}