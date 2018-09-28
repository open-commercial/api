package sic.service;

import sic.modelo.Cliente;
import sic.modelo.Usuario;

public interface IRegistracionService {

  void crearCuentaConClienteAndUsuario(Cliente cliente, Usuario usuario);

  String generarUsername(String nombre, String apellido);
}
