package sic.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.FacturaCompra;

public interface FacturaCompraRepository extends FacturaRepository<FacturaCompra>, FacturaCompraRepositoryCustom {
    
    @Override
    @Query("SELECT f FROM FacturaCompra f WHERE f.id_Factura = :idFactura AND f.eliminada = false")
    FacturaCompra findById(@Param("idFactura") long idFactura);
    
    @Query("SELECT f FROM FacturaCompra f WHERE f.proveedor.id_Proveedor = :id_Proveedor AND f.eliminada = false"
            + " ORDER BY f.fecha ASC")
    Slice<FacturaCompra> getFacturasCompraProveedor(@Param("id_Proveedor") long id_Proveedor, Pageable page);

}
