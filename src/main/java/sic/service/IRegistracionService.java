package sic.service;

import sic.dto.RegistracionClienteAndUsuarioDTO;

public interface IRegistracionService {

  void crearCuenta(RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO);

  String generarUsername(String nombre, String apellido);
}
