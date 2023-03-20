package sic.service;

import org.springframework.data.domain.Page;
import sic.domain.Credencial;
import sic.domain.Rol;
import sic.domain.TipoDeOperacion;
import sic.entity.Usuario;
import sic.entity.criteria.BusquedaUsuarioCriteria;

public interface IUsuarioService {

  Usuario getUsuarioNoEliminadoPorId(Long idUsuario);

  Usuario getUsuarioPorUsername(String username);

  Usuario getUsuarioPorPasswordRecoveryKeyAndIdUsuario(String passwordRecoveryKey, long idUsuario);

  void actualizar(Usuario usuarioPorActualizar);

  void actualizarPasswordRecoveryKey(String passwordRecoveryKey, long idUsuario);

  void enviarEmailDeRecuperacion(long idSucursal, String email, String host);

  void eliminar(long idUsuario);

  Usuario autenticarUsuario(Credencial credencial);

  Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Usuario usuario);

  Usuario guardar(Usuario usuario);

  void actualizarIdSucursalDeUsuario(long idUsuario, long idSucursalPredeterminada);

  Page<Usuario> getUsuariosPorRol(Rol rol);

  String encriptarConMD5(String password);

  boolean esUsuarioHabilitado(long idUsuario);
}
