package sic.controller;

import io.jsonwebtoken.Claims;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import sic.exception.UnauthorizedException;
import sic.modelo.*;
import sic.modelo.dto.RecoveryPasswordDTO;
import sic.modelo.dto.RegistracionClienteAndUsuarioDTO;
import sic.service.IAuthService;
import sic.service.IRegistracionService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

  private final IUsuarioService usuarioService;
  private final IRegistracionService registracionService;
  private final IAuthService authService;
  private final MessageSource messageSource;

  @Autowired
  public AuthController(
      IUsuarioService usuarioService,
      IRegistracionService registracionService,
      IAuthService authService,
      MessageSource messageSource) {
    this.usuarioService = usuarioService;
    this.registracionService = registracionService;
    this.authService = authService;
    this.messageSource = messageSource;
  }

  @PostMapping("/login")
  public String login(@RequestBody Credencial credencial) {
    Usuario usuario = usuarioService.autenticarUsuario(credencial);
    TokenAcceso tokenAcceso = new TokenAcceso();
    tokenAcceso.setAplicacion(credencial.getAplicacion());
    String token = "";
    if (!usuario.getTokens().isEmpty() && usuario.getTokens().contains(tokenAcceso)) {
      for (TokenAcceso f : usuario.getTokens()) {
        if (f.getAplicacion().equals(credencial.getAplicacion()))
          token = f.getToken();
      }
      return token;
    } else {
      token = authService.generarToken(usuario.getIdUsuario(), credencial.getAplicacion(), usuario.getRoles());
      tokenAcceso = new TokenAcceso();
      tokenAcceso.setAplicacion(credencial.getAplicacion());
      tokenAcceso.setToken(token);
      usuario.getTokens().add(tokenAcceso);
      usuarioService.actualizar(usuario);
      return token;
    }
  }

  @PutMapping("/logout")
  public void logout(
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    long idUsuario = (int) claims.get("idUsuario");
    Aplicacion aplicacion = Aplicacion.valueOf(claims.get("app").toString());
    Usuario usuario = usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
    TokenAcceso tokenAcceso = new TokenAcceso();
    tokenAcceso.setAplicacion(aplicacion);
    usuario.getTokens().remove(tokenAcceso);
    usuarioService.actualizar(usuario);
  }

  @GetMapping("/password-recovery")
  public void recuperarPassword(
      @RequestParam String email, @RequestParam long idSucursal, HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin == null) origin = request.getHeader("Host");
    usuarioService.enviarEmailDeRecuperacion(idSucursal, email, origin);
  }

  @PostMapping("/password-recovery")
  public String generarTokenTemporal(@RequestBody RecoveryPasswordDTO recoveryPasswordDTO) {
    TokenAcceso tokenAcceso;
    Usuario usuario =
        usuarioService.getUsuarioPorPasswordRecoveryKeyAndIdUsuario(
            recoveryPasswordDTO.getKey(), recoveryPasswordDTO.getId());
    if (usuario != null && LocalDateTime.now().isBefore(usuario.getPasswordRecoveryKeyExpirationDate())) {
      tokenAcceso = TokenAcceso.builder().aplicacion(recoveryPasswordDTO.getAplicacion()).build();
      usuario.getTokens().remove(tokenAcceso);
      tokenAcceso =
          TokenAcceso.builder()
              .aplicacion(recoveryPasswordDTO.getAplicacion())
              .token(
                  authService.generarToken(
                      usuario.getIdUsuario(),
                      recoveryPasswordDTO.getAplicacion(),
                      usuario.getRoles()))
              .build();
      usuario.getTokens().add(tokenAcceso);
      usuarioService.actualizar(usuario);
      usuarioService.actualizarPasswordRecoveryKey(null, recoveryPasswordDTO.getId());
    } else {
      throw new UnauthorizedException(messageSource.getMessage(
        "mensaje_error_passwordRecoveryKey", null, Locale.getDefault()));
    }
    return tokenAcceso.getToken();
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
    Cliente nuevoCliente = new Cliente();
    nuevoCliente.setTelefono(registracionClienteAndUsuarioDTO.getTelefono());
    nuevoCliente.setEmail(registracionClienteAndUsuarioDTO.getEmail());
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
