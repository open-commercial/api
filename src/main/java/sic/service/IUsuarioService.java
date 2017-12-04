package sic.service;

import java.util.List;
import sic.modelo.Credencial;
import sic.modelo.Rol;
import sic.modelo.Usuario;

public interface IUsuarioService {
   
    Usuario getUsuarioPorId(Long idUsuario);
    
    void actualizar(Usuario usuario);

    void eliminar(long idUsuario);
    
    Usuario autenticarUsuario(Credencial credencial);

    List<Usuario> getUsuarios();
    
    List<Usuario> getUsuariosPorRol(Rol rol);

    List<Usuario> getUsuariosAdministradores();

    Usuario guardar(Usuario usuario);
    
}
