package sic.controller;

import java.util.List;
import java.util.ResourceBundle;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sic.aspect.AccesoRolesPermitidos;
import sic.modelo.BusquedaUsuarioCriteria;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class UsuarioController {

  private final IUsuarioService usuarioService;

  @Value("${SIC_JWT_KEY}")
  private String secretkey;

  @Autowired
  public UsuarioController(IUsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @GetMapping("/usuarios/{idUsuario}")
  @ResponseStatus(HttpStatus.OK)
  public Usuario getUsuarioPorId(@PathVariable long idUsuario) {
    return usuarioService.getUsuarioPorId(idUsuario);
  }

  @GetMapping("/usuarios/busqueda/criteria")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({Rol.ADMINISTRADOR, Rol.ENCARGADO, Rol.VIAJANTE, Rol.VENDEDOR})
  public Page<Usuario> buscarUsuarios(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) String apellido,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) List<Rol> roles,
      @RequestHeader("Authorization") String token) {
    int TAMANIO_PAGINA_DEFAULT = 50;
    if (tamanio == null || tamanio <= 0) tamanio = TAMANIO_PAGINA_DEFAULT;
    if (pagina == null || pagina < 0) pagina = 0;
    Pageable pageable = new PageRequest(pagina, tamanio, new Sort(Sort.Direction.ASC, "nombre"));
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
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    return usuarioService.buscarUsuarios(criteria, (int) claims.get("idUsuario"));
  }

  @PostMapping("/usuarios")
  @ResponseStatus(HttpStatus.CREATED)
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public Usuario guardar(@RequestBody Usuario usuario) {
    return usuarioService.guardar(usuario);
  }

  @PutMapping("/usuarios")
  @ResponseStatus(HttpStatus.OK)
  @AccesoRolesPermitidos({
    Rol.ADMINISTRADOR,
    Rol.ENCARGADO,
    Rol.VIAJANTE,
    Rol.VENDEDOR,
    Rol.COMPRADOR
  })
  public void actualizar(
      @RequestBody Usuario usuario, @RequestHeader("Authorization") String token) {
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
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
  @ResponseStatus(HttpStatus.OK)
  public void actualizarIdEmpresaDeUsuario(
      @PathVariable long idUsuario, @PathVariable long idEmpresaPredeterminada) {
    usuarioService.actualizarIdEmpresaDeUsuario(idUsuario, idEmpresaPredeterminada);
  }

  @DeleteMapping("/usuarios/{idUsuario}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @AccesoRolesPermitidos(Rol.ADMINISTRADOR)
  public void eliminar(@PathVariable long idUsuario) {
    usuarioService.eliminar(idUsuario);
  }
}
