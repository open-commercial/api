package sic.controller;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import sic.exception.UnauthorizedException;
import sic.modelo.*;
import sic.modelo.dto.RecoveryPasswordDTO;
import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;
import sic.service.IAuthService;
import sic.service.IEmpresaService;
import sic.service.IRegistracionService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

  private final IUsuarioService usuarioService;
  private final IEmpresaService empresaService;
  private final IRegistracionService registracionService;
  private final IAuthService authService;
  private final MessageSource messageSource;

  @Autowired
  public AuthController(
      IUsuarioService usuarioService,
      IEmpresaService empresaService,
      IRegistracionService registracionService,
      IAuthService authService,
      MessageSource messageSource) {
    this.usuarioService = usuarioService;
    this.empresaService = empresaService;
    this.registracionService = registracionService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @PostMapping("/login")
  public String login(@RequestBody Credencial credencial) {
    Usuario usuario = usuarioService.autenticarUsuario(credencial);
    if (authService.esTokenValido(usuario.getToken())) {
      return usuario.getToken();
    } else {
      String token = authService.generarToken(usuario.getId_Usuario(), usuario.getRoles());
      usuarioService.actualizarToken(token, usuario.getId_Usuario());
      return token;
    }
  }

  @PutMapping("/logout")
  public void logout(
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuario = (int) claims.get("idUsuario");
    usuarioService.actualizarToken("", idUsuario);
  }

  @GetMapping("/password-recovery")
  public void recuperarPassword(
      @RequestParam String email, @RequestParam long idEmpresa, HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    usuarioService.enviarEmailDeRecuperacion(idEmpresa, email, origin);
  }

  @PostMapping("/password-recovery")
  public String generarTokenTemporal(@RequestBody RecoveryPasswordDTO recoveryPasswordDTO) {
    String token;
    Usuario usuario =
        usuarioService.getUsuarioPorPasswordRecoveryKeyAndIdUsuario(
            recoveryPasswordDTO.getKey(), recoveryPasswordDTO.getId());
    if (usuario != null && (new Date()).before(usuario.getPasswordRecoveryKeyExpirationDate())) {
      token = authService.generarToken(usuario.getId_Usuario(), usuario.getRoles());
      usuarioService.actualizarToken(token, usuario.getId_Usuario());
      usuarioService.actualizarPasswordRecoveryKey(null, recoveryPasswordDTO.getId());
    } else {
      throw new UnauthorizedException(messageSource.getMessage(
        "mensaje_error_passwordRecoveryKey", null, Locale.getDefault()));
    }
    return token;
  }

  @PostMapping("/registracion")
  public void registrarse(
      @RequestBody RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO) {
    authService.validarRecaptcha(registracionClienteAndUsuarioDTO.getRecaptcha());
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setHabilitado(true);
    nuevoUsuario.setNombre(registracionClienteAndUsuarioDTO.getNombre());
    nuevoUsuario.setApellido(registracionClienteAndUsuarioDTO.getApellido());
    nuevoUsuario.setEmail(registracionClienteAndUsuarioDTO.getEmail());
    nuevoUsuario.setPassword(registracionClienteAndUsuarioDTO.getPassword());
    nuevoUsuario.setRoles(Collections.singletonList(Rol.COMPRADOR));
    nuevoUsuario.setIdEmpresaPredeterminada(registracionClienteAndUsuarioDTO.getIdEmpresa());
    Cliente nuevoCliente = new Cliente();
    nuevoCliente.setTelefono(registracionClienteAndUsuarioDTO.getTelefono());
    nuevoCliente.setEmail(registracionClienteAndUsuarioDTO.getEmail());
    nuevoCliente.setEmpresa(
        empresaService.getEmpresaPorId(registracionClienteAndUsuarioDTO.getIdEmpresa()));
    CategoriaIVA categoriaIVA = registracionClienteAndUsuarioDTO.getCategoriaIVA();
    if (categoriaIVA == CategoriaIVA.CONSUMIDOR_FINAL) {
      nuevoCliente.setNombreFiscal(
          registracionClienteAndUsuarioDTO.getNombre()
              + " "
              + registracionClienteAndUsuarioDTO.getApellido());
      nuevoCliente.setCategoriaIVA(CategoriaIVA.CONSUMIDOR_FINAL);
    } else if (categoriaIVA == CategoriaIVA.RESPONSABLE_INSCRIPTO
        || categoriaIVA == CategoriaIVA.MONOTRIBUTO
        || categoriaIVA == CategoriaIVA.EXENTO) {
      nuevoCliente.setNombreFiscal(registracionClienteAndUsuarioDTO.getNombreFiscal());
      nuevoCliente.setCategoriaIVA(categoriaIVA);
      nuevoCliente.setBonificacion(BigDecimal.ZERO);
    }
    this.registracionService.crearCuentaConClienteAndUsuario(nuevoCliente, nuevoUsuario);
  }
}
