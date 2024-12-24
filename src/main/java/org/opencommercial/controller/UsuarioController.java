package org.opencommercial.controller;

import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.opencommercial.aspect.AccesoRolesPermitidos;
import org.opencommercial.exception.ForbiddenException;
import org.opencommercial.model.Rol;
import org.opencommercial.model.Usuario;
import org.opencommercial.model.criteria.BusquedaUsuarioCriteria;
import org.opencommercial.model.dto.UsuarioDTO;
import org.opencommercial.service.AuthService;
import org.opencommercial.service.UsuarioService;
import org.opencommercial.util.EncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
public class UsuarioController {

  private final UsuarioService usuarioService;
  private final AuthService authService;
  private final ModelMapper modelMapper;
  private final MessageSource messageSource;
  private final EncryptUtils encryptUtils;
  private static final String CLAIM_ID_USUARIO = "idUsuario";

  @Autowired
  public UsuarioController(UsuarioService usuarioService,
                           AuthService authService,
                           ModelMapper modelMapper,
                           MessageSource messageSource,
                           EncryptUtils encryptUtils) {
    this.usuarioService = usuarioService;
    this.authService = authService;
    this.modelMapper = modelMapper;
    this.messageSource = messageSource;
    this.encryptUtils = encryptUtils;
  }

  @GetMapping("/api/v1/usuarios/{idUsuario}")
  public Usuario getUsuarioPorId(@PathVariable long idUsuario) {
    return usuarioService.getUsuarioNoEliminadoPorId(idUsuario);
  }

  @PostMapping("/api/v1/usuarios/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Usuario> buscarUsuarios(@RequestBody BusquedaUsuarioCriteria criteria) {
    return usuarioService.buscarUsuarios(criteria);
  }

  @PostMapping("/api/v1/usuarios")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Usuario guardar(@RequestBody UsuarioDTO usuarioDTO,
                         @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLoggedIn = usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class));
    if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)
        && (usuarioDTO.getRoles().size() != 1 || !usuarioDTO.getRoles().contains(Rol.COMPRADOR))) {
      throw new ForbiddenException(messageSource.getMessage(
        "mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
    }
    Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
    return usuarioService.guardar(usuario);
  }

  @PutMapping("/api/v1/usuarios")
  public void actualizar(@RequestBody UsuarioDTO usuarioDTO,
                         @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLoggedIn = usuarioService.getUsuarioNoEliminadoPorId(claims.get(CLAIM_ID_USUARIO, Long.class));
    boolean usuarioSeModificaASiMismo = usuarioLoggedIn.getIdUsuario() == usuarioDTO.getIdUsuario();
    boolean usuarioLoggedInEsAdmin = usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR);
    if (usuarioSeModificaASiMismo || usuarioLoggedInEsAdmin) {
      Usuario usuarioPorActualizar = modelMapper.map(usuarioDTO, Usuario.class);
      Usuario usuarioPersistido = usuarioService.getUsuarioNoEliminadoPorId(usuarioDTO.getIdUsuario());
      if (!usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
        usuarioPorActualizar.setRoles(usuarioPersistido.getRoles());
        usuarioPorActualizar.setHabilitado(usuarioPersistido.isHabilitado());
      }
      if (usuarioPorActualizar.getPassword() != null && !usuarioPorActualizar.getPassword().isEmpty()) {
        usuarioPorActualizar.setPassword(encryptUtils.encryptWithMD5(usuarioPorActualizar.getPassword()));
      } else {
        usuarioPorActualizar.setPassword(usuarioPersistido.getPassword());
      }
      usuarioService.actualizar(usuarioPorActualizar);
    } else {
      throw new ForbiddenException(messageSource.getMessage("mensaje_usuario_rol_no_valido", null, Locale.getDefault()));
    }
  }

  @PutMapping("/api/v1/usuarios/{idUsuario}/sucursales/{idSucursalPredeterminada}")
  public void actualizarIdSucursalDeUsuario(@PathVariable long idUsuario,
                                            @PathVariable long idSucursalPredeterminada) {
    usuarioService.actualizarIdSucursalDeUsuario(idUsuario, idSucursalPredeterminada);
  }

  @DeleteMapping("/api/v1/usuarios/{idUsuario}")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idUsuario) {
    usuarioService.eliminar(idUsuario);
  }
}
