package sic.service;

import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;

import javax.validation.Valid;

public interface IRegistracionService {

  void crearCuenta(@Valid RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO);

  String generarUsername(String nombre, String apellido);
}
