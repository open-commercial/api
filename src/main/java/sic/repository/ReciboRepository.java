package sic.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.Recibo;
import sic.modelo.Usuario;

public interface ReciboRepository extends PagingAndSortingRepository<Recibo, Long> {
    
    @Query("SELECT r FROM Recibo r WHERE r.idRecibo= :idRecibo AND r.eliminado = false")
    Recibo findById(@Param("idRecibo") long idRecibo);
    
    @Query("SELECT r.monto FROM Recibo r WHERE r.idRecibo= :idRecibo AND r.eliminado = false")
    BigDecimal getMontoById(@Param("idRecibo") long idRecibo);
    
    Recibo findTopByEmpresaAndNumSerieOrderByNumReciboDesc(Empresa empresa, long serie);
    
    List<Recibo> findAllByClienteAndEmpresaAndEliminado(Cliente cliente, Empresa empresa, boolean eliminado);
    
    List<Recibo> findAllByUsuarioAndEmpresaAndEliminado(Usuario usuario, Empresa empresa, boolean eliminado);
    
    @Query("SELECT r FROM Recibo r WHERE r.empresa.id_Empresa = :idEmpresa AND r.formaDePago.id_FormaDePago = :idFormaDePago AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
    List<Recibo> getRecibosEntreFechasPorFormaDePago(@Param("idEmpresa") long idEmpresa, @Param("idFormaDePago") long idFormaDePago, @Param("desde") Date desde, @Param("hasta") Date hasta);

    @Query("SELECT SUM(r.monto) FROM Recibo r WHERE r.empresa.id_Empresa = :idEmpresa AND (r.proveedor is null) AND r.formaDePago.id_FormaDePago = :idFormaDePago AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
    BigDecimal getTotalRecibosClientesEntreFechasPorFormaDePago(@Param("idEmpresa") long idEmpresa, @Param("idFormaDePago") long idFormaDePago, @Param("desde") Date desde, @Param("hasta") Date hasta);

    @Query("SELECT SUM(r.monto) FROM Recibo r WHERE r.empresa.id_Empresa = :idEmpresa AND (r.cliente is null) AND r.formaDePago.id_FormaDePago = :idFormaDePago AND r.fecha BETWEEN :desde AND :hasta AND r.eliminado = false")
    BigDecimal getTotalRecibosProveedoresEntreFechasPorFormaDePago(@Param("idEmpresa") long idEmpresa, @Param("idFormaDePago") long idFormaDePago, @Param("desde") Date desde, @Param("hasta") Date hasta);

}
