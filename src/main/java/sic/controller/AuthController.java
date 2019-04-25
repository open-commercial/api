package sic.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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
  private final RestTemplate restTemplate;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("Mensajes");
  private static final String URL_RECAPTCHA = "https://www.google.com/recaptcha/api/siteverify";

  @Value("${RECAPTCHA_SECRET_KEY}")
  private String recaptchaSecretkey;

  @Value("${RECAPTCHA_TEST_KEY}")
  private String recaptchaTestKey;

  @Autowired
  public AuthController(
      IUsuarioService usuarioService,
      IEmpresaService empresaService,
      IRegistracionService registracionService,
      IAuthService authService,
      RestTemplate restTemplate) {
    this.usuarioService = usuarioService;
    this.empresaService = empresaService;
    this.registracionService = registracionService;
    this.authService = authService;
    this.restTemplate = restTemplate;
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
  public void logout(@RequestHeader("Authorization") String authorizationHeader) {
    if (authService.esAuthorizationHeaderValido(authorizationHeader)) {
        Claims claims = authService.getClaimsDelToken(authorizationHeader);
        long idUsuario = (int) claims.get("idUsuario");
        usuarioService.actualizarToken("", idUsuario);
    }
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
      throw new UnauthorizedException(
          RESOURCE_BUNDLE.getString("mensaje_error_passwordRecoveryKey"));
    }
    return token;
  }

  @PostMapping("/registracion")
  public void registrarse(
      @RequestBody RegistracionClienteAndUsuarioDTO registracionClienteAndUsuarioDTO) {
    String params =
        "?secret="
            + recaptchaSecretkey
            + "&response="
            + registracionClienteAndUsuarioDTO.getRecaptcha();
    boolean recaptchaIsSuccess;
    if (registracionClienteAndUsuarioDTO.getRecaptcha().equals(recaptchaTestKey)) {
      recaptchaIsSuccess = true;
    } else {
      ReCaptchaResponse reCaptchaResponse =
          restTemplate
              .exchange(URL_RECAPTCHA + params, HttpMethod.POST, null, ReCaptchaResponse.class)
              .getBody();
      recaptchaIsSuccess = reCaptchaResponse.isSuccess();
    }
    if (recaptchaIsSuccess) {
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
      }
      this.registracionService.crearCuentaConClienteAndUsuario(nuevoCliente, nuevoUsuario);
    } else {
      throw new UnauthorizedException(RESOURCE_BUNDLE.getString("mensaje_recaptcha_no_valido"));
    }
  }
}
