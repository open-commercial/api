package sic.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.*;

import java.util.List;

public interface NotaDebitoRepository extends NotaRepository<NotaDebito>, QueryDslPredicateExecutor<NotaDebito> {

    @Query("SELECT nd FROM NotaDebito nd WHERE nd.idNota= :idNotaDebito AND nd.eliminada = false")
    NotaDebito getById(@Param("idNotaDebito") long idNotaDebito);

    @Query("SELECT max(nd.nroNota) FROM NotaDebito nd " +
            "WHERE nd.tipoComprobante = :tipoComprobante AND nd.serie = :serie AND nd.empresa.id_Empresa = :idEmpresa " +
            "AND nd.cliente IS NOT null")
    Long buscarMayorNumNotaDebitoClienteSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);

    @Query("SELECT max(nd.nroNota) FROM NotaDebito nd " +
            "WHERE nd.tipoComprobante = :tipoComprobante AND nd.serie = :serie AND nd.empresa.id_Empresa = :idEmpresa " +
            "AND nd.proveedor IS NOT null")
    Long buscarMayorNumNotaDebitoProveedorSegunTipo(@Param("tipoComprobante") TipoDeComprobante tipoComprobante, @Param("serie") long serie, @Param("idEmpresa") long idEmpresa);

    List<NotaDebito> findAllByProveedorAndEmpresaAndEliminada(Proveedor proveedor, Empresa empresa, boolean eliminada);

    boolean existsByReciboAndEliminada(Recibo recibo, boolean eliminada);

}
