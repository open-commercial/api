package org.opencommercial.service;

import org.opencommercial.model.dto.RegistracionClienteAndUsuarioDTO;

public interface RegistracionService {

  void crearCuenta(RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO);

  String generarUsername(String nombre, String apellido);
}
