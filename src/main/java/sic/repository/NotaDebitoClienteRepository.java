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
    
    @Query("SELECT max(ndc.nroNota) FROM NotaDebitoCliente ndc WHERE ndc.tipoComprobante = :tipoComprobante AND ndc.serie = :serie AND ndc.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumNotaDebitoClienteSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);
    
    @Query("SELECT ndc FROM NotaDebitoCliente ndc WHERE ndc.idNota = :idNotaCreditoCliente AND ndc.eliminada = false")
    @Override
    NotaDebitoCliente getById(@Param("idNotaCreditoCliente") long idNotaCreditoCliente);
    
    boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);
    
}
