package sic.service;

import org.springframework.data.domain.Page;
import sic.modelo.*;

public interface IUsuarioService {

  Usuario getUsuarioPorId(Long idUsuario);

  Usuario getUsuarioPorUsername(String username);

  Usuario getUsuarioPorPasswordRecoveryKeyAndIdUsuario(String passwordRecoveryKey, long idUsuario);

  void actualizar(Usuario usuarioPorActualizar, Usuario usuarioPersistido);

  void actualizarToken(String token, long idUsuario);

  void actualizarPasswordRecoveryKey(String passwordRecoveryKey, long idUsuario);

  void enviarEmailDeRecuperacion(long idEmpresa, String email, String host);

  void eliminar(long idUsuario);

  Usuario autenticarUsuario(Credencial credencial);

  Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria);

  void validarOperacion(TipoDeOperacion operacion, Usuario usuario);

  Usuario guardar(Usuario usuario);

  int actualizarIdEmpresaDeUsuario(long idUsuario, long idEmpresaPredeterminada);

  Page<Usuario> getUsuariosPorRol(Rol rol);

  String encriptarConMD5(String password);
}
