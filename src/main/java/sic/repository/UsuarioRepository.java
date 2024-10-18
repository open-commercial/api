package sic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends
        JpaRepository<Usuario, Long>,
        QuerydslPredicateExecutor<Usuario> {

  @Query("SELECT u FROM Usuario u WHERE u.idUsuario = :idUsuario AND u.eliminado = false")
  Optional<Usuario> findByIdUsuario(@Param("idUsuario") long idUsuario);

  @Query("SELECT u FROM Usuario u "
          + "WHERE (u.username = :username OR u.email = :email) AND u.password = :password "
          + "AND u.eliminado = false")
  Usuario findByUsernameOrEmailAndPasswordAndEliminado(@Param("username") String username,
                                                       @Param("email") String email,
                                                       @Param("password") String password);

  Usuario findByUsernameAndEliminado(String username, boolean eliminado);

  Usuario findByEmailAndEliminado(String email, boolean eliminado);

  @Query("SELECT u FROM Usuario u "
          + "WHERE u.passwordRecoveryKey = ?1 AND u.idUsuario = ?2 "
          + "AND u.eliminado = false AND u.habilitado = true")
  Usuario findByPasswordRecoveryKeyAndIdUsuarioAndEliminadoAndHabilitado(String passwordRecoveryKey, long idUsuario);

  Page<Usuario> findAllByRolesContainsAndEliminado(Rol rol, boolean eliminado, Pageable pageable);
}
