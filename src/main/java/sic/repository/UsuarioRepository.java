package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Rol;
import sic.modelo.Usuario;

public interface UsuarioRepository extends PagingAndSortingRepository<Usuario, Long>, QueryDslPredicateExecutor<Usuario> {

      @Query("SELECT u FROM Usuario u WHERE u.id_Usuario = :idUsuario AND u.eliminado = false")
      Usuario findById(@Param("idUsuario") long idUsuario);
      
      @Query("SELECT u FROM Usuario u WHERE (u.username = :username OR u.email = :email) AND u.password = :password AND u.eliminado = false")
      Usuario findByUsernameOrEmailAndPasswordAndEliminado(@Param("username") String username, @Param("email") String email, @Param("password") String password);
    
      Usuario findByUsernameAndEliminado(String username, boolean eliminado);
      
      Usuario findByEmailAndEliminado(String email, boolean eliminado);
    
      @Modifying
      @Query("UPDATE Usuario u SET u.token = ?1 WHERE u.id_Usuario = ?2")
      int updateToken(String token, long idUsuario);
      
      @Modifying
      @Query("UPDATE Usuario u SET u.idEmpresaPredeterminada = :idEmpresaPredeterminada WHERE u.id_Usuario = :idUsuario")
      int updateIdEmpresa(@Param("idUsuario") long idUsuario, @Param("idEmpresaPredeterminada") long idEmpresaPredeterminada);
      
}
