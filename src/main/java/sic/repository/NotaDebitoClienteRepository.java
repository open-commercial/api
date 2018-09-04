package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.NotaDebitoCliente;
import sic.modelo.Recibo;
import sic.modelo.TipoDeComprobante;

public interface NotaDebitoClienteRepository extends NotaDebitoRepository<NotaDebitoCliente>, QueryDslPredicateExecutor<NotaDebitoCliente> {
    
    @Query("SELECT max(ndc.nroNota) FROM NotaDebitoCliente ndc WHERE ndc.tipoComprobante = :tipoComprobante AND ndc.serie = :serie AND ndc.empresa.id_Empresa = :idEmpresa")
    Long buscarMayorNumNotaDebitoClienteSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);
    
    @Query("SELECT ndc FROM NotaDebitoCliente ndc WHERE ndc.idNota = :idNotaCreditoCliente AND ndc.eliminada = false")
    @Override
    NotaDebitoCliente getById(@Param("idNotaCreditoCliente") long idNotaCreditoCliente);
    
    boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);
    
}
