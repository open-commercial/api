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
@Transactional
public class RegistracionServiceImpl implements IRegistracionService {

  private final IUsuarioService usuarioService;
  private final IClienteService clienteService;

  @Autowired
  public RegistracionServiceImpl(IUsuarioService usuarioService,
                                 IClienteService clienteService) {
    this.usuarioService = usuarioService;
    this.clienteService = clienteService;
  }

  @Override
  public void crearCuentaConClienteAndUsuario(Cliente cliente, Usuario usuario) {
    usuario.setUsername(this.generarUsername(usuario.getNombre(), usuario.getApellido()));
    Usuario credencial = usuarioService.guardar(usuario);
    cliente.setCredencial(credencial);
    clienteService.guardar(cliente);
    //send email
  }

  @Override
  public String generarUsername(String nombre, String apellido) {
    long min = 1L;
    long max = 999L; // 3 digitos
    long randomLong;
    boolean esRepetido = true;
    nombre = nombre.replaceAll("\\s+","");
    apellido = apellido.replaceAll("\\s+","");
    String nuevoUsername = "";
    while (esRepetido) {
      randomLong = min + (long) (Math.random() * (max - min));
      nuevoUsername = nombre + apellido + Long.toString(randomLong);
      Usuario u = usuarioService.getUsuarioPorUsername(nuevoUsername);
      if (u == null) esRepetido = false;
    }
    return nuevoUsername.toLowerCase();
  }
}
