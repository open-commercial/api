package sic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaUsuarioCriteria;
import sic.modelo.Credencial;
import sic.modelo.Rol;
import sic.modelo.Usuario;

public interface IUsuarioService {
   
    Usuario getUsuarioPorId(Long idUsuario);
    
    void actualizar(Usuario usuario, Long idCliente);
    
    void actualizarToken(String token, long idUsuario);

    void eliminar(long idUsuario);
    
    Usuario autenticarUsuario(Credencial credencial);

    Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria, long idUsuarioLoggedIn);

    Usuario guardar(Usuario usuario, Long idUsuario);
    
    int actualizarIdEmpresaDeUsuario(long idUsuario, long idEmpresaPredeterminada);
    
}
