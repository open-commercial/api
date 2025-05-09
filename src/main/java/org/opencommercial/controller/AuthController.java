package org.opencommercial.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.opencommercial.model.Credencial;
import org.opencommercial.model.dto.RecoveryPasswordDTO;
import org.opencommercial.model.dto.RegistracionClienteAndUsuarioDTO;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.RegistracionService;
import org.opencommercial.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

  private final UsuarioService usuarioService;
  private final RegistracionService registracionService;
  private final AuthService authService;

  @Autowired
  public AuthController(UsuarioService usuarioService,
                        RegistracionService registracionService,
                        AuthService authService) {
    this.usuarioService = usuarioService;
    this.registracionService = registracionService;
    this.authService = authService;
  }

  @PostMapping("/api/v1/login")
  public String login(@RequestBody Credencial credencial) {
    var usuario = usuarioService.autenticarUsuario(credencial);
    return authService.generarJWT(usuario.getIdUsuario(), usuario.getRoles());
  }

  @PutMapping("/api/v1/logout")
  public void logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    if (authorizationHeader != null) {
      authService.excluirTokenAcceso(authorizationHeader);
    }
  }

  @GetMapping("/api/v1/password-recovery")
  public void recuperarPassword(@RequestParam String email,
                                HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    usuarioService.enviarEmailDeRecuperacion(email, origin);
  }

  @PostMapping("/api/v1/password-recovery")
  public void cambiarPasswordConRecuperacion(@RequestBody RecoveryPasswordDTO recoveryPasswordDTO) {
    usuarioService.actualizarPasswordConRecuperacion(recoveryPasswordDTO.getKey(),
            recoveryPasswordDTO.getId(), recoveryPasswordDTO.getNewPassword());
  }

  @PostMapping("/api/v1/registracion")
  public void registrarse(@RequestBody RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO) {
    authService.validarRecaptcha(registracionClienteAndUsuarioDTO.getRecaptcha());
    this.registracionService.crearCuenta(registracionClienteAndUsuarioDTO);
  }
}
