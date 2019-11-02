package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Empresa;
import sic.modelo.Usuario;

public interface ClienteRepository
    extends PagingAndSortingRepository<Cliente, Long>, QuerydslPredicateExecutor<Cliente> {

  Cliente findByIdFiscalAndEmpresaAndEliminado(Long idFiscal, Empresa empresa, boolean eliminado);

  Cliente findByEmpresaAndPredeterminadoAndEliminado(
      Empresa empresa, boolean predeterminado, boolean eliminado);

  boolean existsByEmpresaAndPredeterminadoAndEliminado(
      Empresa empresa, boolean predeterminado, boolean eliminado);

  @Query(
      "SELECT c FROM Pedido p INNER JOIN p.cliente c WHERE p.id_Pedido = :idPedido AND c.eliminado = false")
  Cliente findClienteByIdPedido(@Param("idPedido") long idPedido);

  @Query(
      "SELECT c FROM Cliente c WHERE c.credencial.id_Usuario = :idUsuario "
          + "AND c.empresa.idEmpresa = :idEmpresa AND c.eliminado = false")
  Cliente findClienteByIdUsuarioYidEmpresa(
      @Param("idUsuario") long idUsuario, @Param("idEmpresa") long idEmpresa);

  Cliente findByCredencialAndEliminado(Usuario UsuarioCredencial, boolean eliminado);

  @Modifying
  @Query("UPDATE Cliente c SET c.viajante = null WHERE c.viajante.id_Usuario = :idUsuarioViajante")
  int desvincularClienteDeViajante(@Param("idUsuarioViajante") long idUsuarioViajante);

  @Modifying
  @Query(
      "UPDATE Cliente c SET c.credencial = null WHERE c.credencial.id_Usuario = :idUsuarioCredencial")
  int desvincularClienteDeCredencial(@Param("idUsuarioCredencial") long idUsuarioCredencial);

  Cliente findByNroClienteAndEmpresaAndEliminado(
      String nroCliente, Empresa empresa, boolean eliminado);
}
