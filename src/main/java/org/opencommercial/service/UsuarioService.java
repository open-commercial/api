package org.opencommercial.service;

import org.opencommercial.model.Credencial;
import org.opencommercial.model.Rol;
import org.opencommercial.model.TipoDeOperacion;
import org.opencommercial.model.Usuario;
import org.opencommercial.model.criteria.BusquedaUsuarioCriteria;
import org.springframework.data.domain.Page;

public interface UsuarioService {

  Usuario getUsuarioNoEliminadoPorId(long idUsuario);

  Usuario getUsuarioPorUsername(String username);

  Usuario getUsuarioPorPasswordRecoveryKeyAndIdUsuario(String passwordRecoveryKey, long idUsuario);

  void actualizar(Usuario usuarioPorActualizar);

  void actualizarPasswordRecoveryKey(String passwordRecoveryKey, Usuario usuario);

  void actualizarPasswordConRecuperacion(String key, long idUsuario, String newPassword);

  void enviarEmailDeRecuperacion(String email, String host);

  void eliminar(long idUsuario);

  Usuario autenticarUsuario(Credencial credencial);

  Page<Usuario> buscarUsuarios(BusquedaUsuarioCriteria criteria);

  void validarReglasDeNegocio(TipoDeOperacion operacion, Usuario usuario);

  Usuario guardar(Usuario usuario);

  void actualizarIdSucursalDeUsuario(long idUsuario, long idSucursalPredeterminada);

  Page<Usuario> getUsuariosPorRol(Rol rol);

  boolean esUsuarioHabilitado(long idUsuario);
}
