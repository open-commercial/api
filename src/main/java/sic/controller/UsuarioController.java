package sic.controller;

import java.util.List;
import java.util.ResourceBundle;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.BusquedaUsuarioCriteria;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.service.IAuthService;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

  private final IUsuarioService usuarioService;
  private final IAuthService authService;
  private static final int TAMANIO_PAGINA_DEFAULT = 25;

  @Autowired
  public UsuarioController(IUsuarioService usuarioService, IAuthService authService) {
    this.usuarioService = usuarioService;
    this.authService = authService;
  }

  @GetMapping("/usuarios/{idUsuario}")
  public Usuario getUsuarioPorId(@PathVariable long idUsuario) {
    return usuarioService.getUsuarioPorId(idUsuario);
  }

  @GetMapping("/usuarios/busqueda/criteria")
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VENDEDOR})
  public Page<Usuario> buscarUsuarios(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) String apellido,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) String ordenarPor,
      @RequestParam(required = false) String sentido,
      @RequestParam(required = false) List<Rol> roles) {
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable;
    if (ordenarPor == null || sentido == null) {
      pageable =
        new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombre"));
    } else {
      switch (sentido) {
        case "ASC" : pageable =
          new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, ordenarPor));
          break;
        case "DESC" : pageable =
          new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.DESC, ordenarPor));
          break;
        default: pageable =
          new PageRequest(pagina, TAMANIO_PAGINA_DEFAULT, new Sort(Sort.Direction.ASC, "nombre"));
          break;
      }
    }
    BusquedaUsuarioCriteria criteria =
        BusquedaUsuarioCriteria.builder()
            .buscarPorNombreDeUsuario(username != null)
            .username(username)
            .buscaPorNombre(nombre != null)
            .nombre(nombre)
            .buscaPorApellido(apellido != null)
            .apellido(apellido)
            .buscaPorEmail(email != null)
            .email(email)
            .buscarPorRol(roles != null && !roles.isEmpty())
            .roles(roles)
            .pageable(pageable)
            .build();
    return usuarioService.buscarUsuarios(criteria);
  }

  @PostMapping("/usuarios")
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public Usuario guardar(@RequestBody Usuario usuario) {
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
      @RequestBody Usuario usuario, @RequestHeader("Authorization") String authorizationHeader) {
    Claims claims = authService.getClaimsDelToken(authorizationHeader);
    Usuario usuarioLoggedIn = this.getUsuarioPorId((int) claims.get("idUsuario"));
    boolean usuarioSeModificaASiMismo = usuarioLoggedIn.getId_Usuario() == usuario.getId_Usuario();
    if (usuarioSeModificaASiMismo || usuarioLoggedIn.getRoles().contains(Rol.ADMINISTRADOR)) {
      usuarioService.actualizar(usuario, usuarioLoggedIn);
    } else {
      throw new ForbiddenException(
          ResourceBundle.getBundle("Mensajes").getString("mensaje_usuario_rol_no_valido"));
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
