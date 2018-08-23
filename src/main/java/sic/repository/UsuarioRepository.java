package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Rol;
import sic.modelo.Usuario;

import java.util.Date;

public interface UsuarioRepository
    extends PagingAndSortingRepository<Usuario, Long>, QueryDslPredicateExecutor<Usuario> {

  @Query("SELECT u FROM Usuario u WHERE u.id_Usuario = :idUsuario AND u.eliminado = false")
  Usuario findById(@Param("idUsuario") long idUsuario);

  @Query(
      "SELECT u FROM Usuario u "
          + "WHERE (u.username = :username OR u.email = :email) AND u.password = :password "
          + "AND u.eliminado = false")
  Usuario findByUsernameOrEmailAndPasswordAndEliminado(
      @Param("username") String username,
      @Param("email") String email,
      @Param("password") String password);

  Usuario findByUsernameAndEliminado(String username, boolean eliminado);

  Usuario findByEmailAndEliminadoAndHabilitado(String email, boolean eliminado, boolean habilitado);

  @Query(
      "SELECT u FROM Usuario u "
          + "WHERE u.passwordRecoveryKey = ?1 AND u.id_Usuario = ?2 "
          + "AND u.eliminado = false AND u.habilitado = true")
  Usuario findByPasswordRecoveryKeyAndIdUsuarioAndEliminadoAndHabilitado(
      String passwordRecoveryKey, long idUsuario);

  @Modifying
  @Query("UPDATE Usuario u SET u.token = ?1 WHERE u.id_Usuario = ?2")
  int updateToken(String token, long idUsuario);

  @Modifying
  @Query("UPDATE Usuario u SET u.passwordRecoveryKey = ?1, u.passwordRecoveryKeyExpirationDate = ?2 "
          + "WHERE u.id_Usuario = ?3")
  int updatePasswordRecoveryKey(String passwordRecoveryKey, Date passwordRecoveryKeyExpirationDate, long idUsuario);

  @Modifying
  @Query(
      "UPDATE Usuario u SET u.idEmpresaPredeterminada = :idEmpresaPredeterminada "
          + "WHERE u.id_Usuario = :idUsuario")
  int updateIdEmpresa(
      @Param("idUsuario") long idUsuario,
      @Param("idEmpresaPredeterminada") long idEmpresaPredeterminada);

  Page<Usuario> findAllByRolesContainsAndEliminado(Rol rol, boolean eliminado, Pageable pageable);
}
