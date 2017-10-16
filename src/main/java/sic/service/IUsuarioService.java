package sic.service;

import java.util.List;
import sic.modelo.Rol;
import sic.modelo.Usuario;

public interface IUsuarioService {
   
    Usuario getUsuarioPorId(Long idUsuario);

    void actualizar(Usuario usuario);

    void eliminar(long idUsuario);

    Usuario getUsuarioPorNombre(String nombre);

    Usuario getUsuarioPorNombreContrasenia(String nombre, String contrasenia);

    List<Usuario> getUsuarios();
    
    List<Usuario> getUsuariosPorRol(Rol rol);

    List<Usuario> getUsuariosAdministradores();

    Usuario guardar(Usuario usuario);
    
}
