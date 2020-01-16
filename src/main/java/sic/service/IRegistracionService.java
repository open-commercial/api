package sic.service;

import sic.modelo.Usuario;
import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;

import javax.validation.Valid;

public interface IRegistracionService {

  Usuario crearCuenta(@Valid RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO);

  String generarUsername(String nombre, String apellido);
}
