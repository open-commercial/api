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
import sic.entity.criteria.BusquedaUsuarioCriteria;
import sic.domain.Rol;
import sic.entity.Usuario;
import sic.dto.UsuarioDTO;
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
  public void actualizar(
      @RequestBody UsuarioDTO usuarioDTO,
      @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLoggedIn = this.getUsuarioPorId((int) claims.get("idUsuario"));
    boolean usuarioSeModificaASiMismo =
        usuarioLoggedIn.getIdUsuario() == usuarioDTO.getIdUsuario();
    if (usuarioSeModificaASiMismo || usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
      Usuario usuarioPorActualizar = modelMapper.map(usuarioDTO, Usuario.class);
      Usuario usuarioPersistido = usuarioService.getUsuarioNoEliminadoPorId(usuarioDTO.getIdUsuario());
      if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
        usuarioPorActualizar.setRoles(usuarioPersistido.getRoles());
        usuarioPorActualizar.setHabilitado(usuarioPersistido.isHabilitado());
      }
      if (usuarioPorActualizar.getPassword() != null
          && !usuarioPorActualizar.getPassword().isEmpty()) {
        usuarioPorActualizar.setPassword(
            usuarioService.encriptarConMD5(usuarioPorActualizar.getPassword()));
      } else {
        usuarioPorActualizar.setPassword(usuarioPersistido.getPassword());
      }
      usuarioService.actualizar(usuarioPorActualizar);
    } else {
      throw new ForbiddenException(messageSource.getMessage(
        "mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
    }
  }

  @PutMapping("/usuarios/{idUsuario}/sucursales/{idSucursalPredeterminada}")
  public void actualizarIdSucursalDeUsuario(
      @PathVariable long idUsuario, @PathVariable long idSucursalPredeterminada) {
    usuarioService.actualizarIdSucursalDeUsuario(idUsuario, idSucursalPredeterminada);
  }

  @DeleteMapping("/usuarios/{idUsuario}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idUsuario) {
    usuarioService.eliminar(idUsuario);
  }
}
