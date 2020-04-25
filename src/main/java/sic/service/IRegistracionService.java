package sic.service;

import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;

public interface IRegistracionService {

  void crearCuenta(RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO);

  String generarUsername(String nombre, String apellido);
}
