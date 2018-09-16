package sic.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sic.modelo.Cliente;
import sic.modelo.Usuario;
import sic.service.IClienteService;
import sic.service.IRegistracionService;
import sic.service.IUsuarioService;

@Service
public class RegistracionServiceImpl implements IRegistracionService {

  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;

  @Autowired
  public RegistracionServiceImpl(IUsuarioService usuarioService, IClienteService clienteService) {
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
  }

  @Override
  @Transactional
  public void crearCuentaConClienteAndUsuario(Cliente cliente, Usuario usuario) {

  }
}
