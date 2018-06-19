package sic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.controller.ForbiddenException;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.service.IAuthService;
import sic.service.IUsuarioService;

import java.util.List;
import java.util.ResourceBundle;

@Service
@Transactional
public class AuthServiceImpl implements IAuthService {

  private final IUsuarioService usuarioService;

  @Autowired
  public AuthServiceImpl(IUsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  public void verificarAcceso(List<Rol> roles, long idUsuarioLoggedIn) {
    Usuario usuarioLoggedIn = usuarioService.getUsuarioPorId(idUsuarioLoggedIn);
    if (!usuarioLoggedIn.getRoles().containsAll(roles)) {
      throw new ForbiddenException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_rol_no_valido"));
    }
  }
}
