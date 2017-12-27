package sic.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import sic.modelo.Rol;
import sic.modelo.Usuario;

public interface UsuarioRepository extends PagingAndSortingRepository<Usuario, Long> {

      @Query("SELECT u FROM Usuario u WHERE u.id_Usuario = :idUsuario AND u.eliminado = false") 
      Usuario findById(@Param("idUsuario") long idUsuario);
      
      @Query("SELECT u FROM Usuario u WHERE (u.username = :username OR u.email = :email) AND u.password = :password AND u.eliminado = false")
      Usuario findByUsernameOrEmailAndPasswordAndEliminado(@Param("username") String username,
              @Param("email") String email, @Param("password")String password);
    
      Usuario findByNombreAndEliminado(String nombre, boolean eliminado);

      List<Usuario> findAllByAndEliminadoOrderByNombreAsc(boolean eliminado);
      
      List<Usuario> findAllByAndEliminadoAndRolesOrderByNombreAsc(boolean eliminado, Rol rol);

      List<Usuario> findAllByAndRolesAndEliminadoOrderByNombreAsc(Rol rol, boolean eliminado);
    
}
