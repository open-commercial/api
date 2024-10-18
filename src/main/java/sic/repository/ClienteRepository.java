package sic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Usuario;
import java.util.List;

public interface ClienteRepository extends
        JpaRepository<Cliente, Long>,
        QuerydslPredicateExecutor<Cliente> {

  List<Cliente> findByIdFiscalAndEliminado(Long idFiscal, boolean eliminado);

  Cliente findByAndPredeterminadoAndEliminado(boolean predeterminado, boolean eliminado);

  boolean existsByAndPredeterminadoAndEliminado(boolean predeterminado, boolean eliminado);

  @Query("SELECT c FROM Pedido p INNER JOIN p.cliente c WHERE p.idPedido = :idPedido AND c.eliminado = false")
  Cliente findClienteByIdPedido(@Param("idPedido") long idPedido);

  @Query("SELECT c FROM Cliente c WHERE c.credencial.idUsuario = :idUsuario AND c.eliminado = false")
  Cliente findClienteByIdUsuario(@Param("idUsuario") long idUsuario);

  Cliente findByCredencialAndEliminado(Usuario usuarioCredencial, boolean eliminado);

  @Modifying
  @Query("UPDATE Cliente c SET c.viajante = null WHERE c.viajante.idUsuario = :idUsuarioViajante")
  void desvincularClienteDeViajante(@Param("idUsuarioViajante") long idUsuarioViajante);

  @Modifying
  @Query("UPDATE Cliente c SET c.credencial = null WHERE c.credencial.idUsuario = :idUsuarioCredencial")
  void desvincularClienteDeCredencial(@Param("idUsuarioCredencial") long idUsuarioCredencial);

  boolean existsByNroCliente(String nroCliente);
}
