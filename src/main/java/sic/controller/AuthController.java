package sic.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sic.modelo.*;
import sic.modelo.dto.RecoveryPasswordDTO;
import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;
import sic.service.IAuthService;
import sic.service.IRegistracionService;
import sic.service.IUsuarioService;

@RestController
public class AuthController {

  private final IUsuarioService usuarioService;
  private final IRegistracionService registracionService;
  private final IAuthService authService;

  @Autowired
  public AuthController(
      IUsuarioService usuarioService,
      IRegistracionService registracionService,
      IAuthService authService) {
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
                                @RequestParam long idSucursal,
                                HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    usuarioService.enviarEmailDeRecuperacion(idSucursal, email, origin);
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
