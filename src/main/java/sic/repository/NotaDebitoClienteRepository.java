package sic.repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.NotaDebitoCliente;
import sic.modelo.Recibo;
import sic.modelo.TipoDeComprobante;

public interface NotaDebitoClienteRepository extends NotaDebitoRepository<NotaDebitoCliente> {
    
    Page<NotaDebitoCliente> findAllByFechaBetweenAndClienteAndEmpresaAndEliminada(Date desde, Date hasta, Cliente cliente, Empresa empresa, boolean eliminada, Pageable page);
    
    List<NotaDebitoCliente> findAllByClienteAndEmpresaAndEliminada(Cliente cliente, Empresa empresa, boolean eliminada);
    
    @Query("SELECT max(ncp.nroNota) FROM NotaDebitoCliente ncp WHERE ncp.tipoComprobante = :tipoComprobante AND ncp.serie = :serie AND ncp.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumNotaDebitoClienteSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);
    
    @Query("SELECT ncc FROM NotaCreditoCliente ncc WHERE ncc.idNota = :idNotaCreditoCliente AND ncc.eliminada = false")
    @Override
    NotaDebitoCliente getById(@Param("idNotaCreditoCliente") long idNotaCreditoCliente);
    
    boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);
    
}
