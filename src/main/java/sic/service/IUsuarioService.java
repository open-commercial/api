package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.BusquedaUsuarioCriteria;
import sic.modelo.Credencial;
import sic.modelo.Usuario;

public interface IUsuarioService {

  Usuario getUsuarioPorId(Long idUsuario);

  void actualizar(Usuario usuario, long idUsuarioLoggedIn);

  void actualizarToken(String token, long idUsuario);

  void eliminar(long idUsuario);

  Usuario autenticarUsuario(Credencial credencial);

  Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria);

  Usuario guardar(Usuario usuario);

  int actualizarIdEmpresaDeUsuario(long idUsuario, long idEmpresaPredeterminada);

}
