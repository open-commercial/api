package sic.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Cliente;
import sic.modelo.Sucursal;
import sic.modelo.Usuario;

public interface ClienteRepository
    extends PagingAndSortingRepository<Cliente, Long>, QuerydslPredicateExecutor<Cliente> {

  Cliente findByIdFiscalAndSucursalAndEliminado(Long idFiscal, Sucursal sucursal, boolean eliminado);

  Cliente findBySucursalAndPredeterminadoAndEliminado(
    Sucursal sucursal, boolean predeterminado, boolean eliminado);

  boolean existsBySucursalAndPredeterminadoAndEliminado(
    Sucursal sucursal, boolean predeterminado, boolean eliminado);

  @Query(
      "SELECT c FROM Pedido p INNER JOIN p.cliente c WHERE p.id_Pedido = :idPedido AND c.eliminado = false")
  Cliente findClienteByIdPedido(@Param("idPedido") long idPedido);

  @Query(
      "SELECT c FROM Cliente c WHERE c.credencial.id_Usuario = :idUsuario "
          + "AND c.sucursal.idSucursal = :idSucursal AND c.eliminado = false")
  Cliente findClienteByIdUsuarioYidSucursal(
      @Param("idUsuario") long idUsuario, @Param("idSucursal") long idSucursal);

  Cliente findByCredencialAndEliminado(Usuario usuarioCredencial, boolean eliminado);

  @Modifying
  @Query("UPDATE Cliente c SET c.viajante = null WHERE c.viajante.id_Usuario = :idUsuarioViajante")
  int desvincularClienteDeViajante(@Param("idUsuarioViajante") long idUsuarioViajante);

  @Modifying
  @Query(
      "UPDATE Cliente c SET c.credencial = null WHERE c.credencial.id_Usuario = :idUsuarioCredencial")
  int desvincularClienteDeCredencial(@Param("idUsuarioCredencial") long idUsuarioCredencial);

  Cliente findByNroClienteAndSucursalAndEliminado(
    String nroCliente, Sucursal sucursal, boolean eliminado);
}
