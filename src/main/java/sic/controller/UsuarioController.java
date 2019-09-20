package sic.controller;

import java.util.Locale;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.exception.ForbiddenException;
import sic.modelo.criteria.BusquedaUsuarioCriteria;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.modelo.dto.UsuarioDTO;
import sic.service.IAuthService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

  private final IUsuarioService usuarioService;
  private final IAuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;

  @Autowired
  public UsuarioController(
      IUsuarioService usuarioService,
      IAuthService authService,
      ModelMapper modelMapper,
      MessageSource messageSource) {
    this.usuarioService = usuarioService;
    this.authService = authService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
  }

  @GetMapping("/usuarios/{idUsuario}")
  public Usuario getUsuarioPorId(@PathVariable long idUsuario) {
    return usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
  }

  @PostMapping("/usuarios/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Usuario> buscarUsuarios(
      @RequestBody BusquedaUsuarioCriteria criteria) {
    return usuarioService.buscarUsuarios(criteria);
  }

  @PostMapping("/usuarios")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Usuario guardar(
      @RequestBody UsuarioDTO usuarioDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLoggedIn = this.getUsuarioPorId((int) claims.get("idUsuario"));
    if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
        && (usuarioDTO.getRoles().size() != 1 || !usuarioDTO.getRoles().contains(Rol.COMPRADOR))) {
      throw new ForbiddenException(messageSource.getMessage(
        "mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
    }
    Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
    return usuarioService.guardar(usuario);
  }

  @PutMapping("/usuarios")
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VIAJANTE,
    Rol.VENDEDOR,
    Rol.COMPRADOR
  })
  public void actualizar(
      @RequestBody UsuarioDTO usuarioDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLoggedIn = this.getUsuarioPorId((int) claims.get("idUsuario"));
    boolean usuarioSeModificaASiMismo =
        usuarioLoggedIn.getId_Usuario() == usuarioDTO.getId_Usuario();
    if (usuarioSeModificaASiMismo || usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
      Usuario usuarioPorActualizar = modelMapper.map(usuarioDTO, Usuario.class);
      Usuario usuarioPersistido = usuarioService.getUsuarioNoEliminadoPorId(usuarioDTO.getId_Usuario());
      if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
        usuarioPorActualizar.setRoles(usuarioPersistido.getRoles());
      }
      if (usuarioLoggedIn.getId_Usuario() == usuarioPersistido.getId_Usuario()) {
        usuarioPorActualizar.setToken(usuarioLoggedIn.getToken());
      }
      if (usuarioPorActualizar.getPassword() != null
          && !usuarioPorActualizar.getPassword().isEmpty()) {
        usuarioPorActualizar.setPassword(
            usuarioService.encriptarConMD5(usuarioPorActualizar.getPassword()));
      } else {
        usuarioPorActualizar.setPassword(usuarioPersistido.getPassword());
      }
      usuarioService.actualizar(usuarioPorActualizar, usuarioPersistido);
      if (!usuarioSeModificaASiMismo)
        usuarioService.actualizarToken("", usuarioPorActualizar.getId_Usuario());
    } else {
      throw new ForbiddenException(messageSource.getMessage(
        "mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
    }
  }

  @PutMapping("/usuarios/{idUsuario}/empresas/{idEmpresaPredeterminada}")
  public void actualizarIdEmpresaDeUsuario(
      @PathVariable long idUsuario, @PathVariable long idEmpresaPredeterminada) {
    usuarioService.actualizarIdEmpresaDeUsuario(idUsuario, idEmpresaPredeterminada);
  }

  @DeleteMapping("/usuarios/{idUsuario}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idUsuario) {
    usuarioService.eliminar(idUsuario);
  }
}
