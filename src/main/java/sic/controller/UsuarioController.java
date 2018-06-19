package sic.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private final int TAMANIO_PAGINA_DEFAULT = 50;

    @Value("${SIC_JWT_KEY}")
    private String secretkey;
    
    @Autowired
    public UsuarioController(IUsuarioService usuarioService, IAuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }
    
    @GetMapping("/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.OK)
    public Usuario getUsuarioPorId(@PathVariable long idUsuario) {
        return usuarioService.getUsuarioPorId(idUsuario);
    }

  @GetMapping("/usuarios/busqueda/criteria")
  @ResponseStatus(HttpStatus.OK)
  public Page<Usuario> buscarUsuarios(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) String apellido,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) Integer pagina,
      @RequestParam(required = false) Integer tamanio,
      @RequestParam(required = false) List<Rol> roles,
      @RequestHeader("Authorization") String token) {
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
    List<Rol> rolesPermitidos = Arrays.asList(Rol.ADMINISTRADOR, Rol.ENCARGADO);
    authService.verificarAcceso(rolesPermitidos, (int) claims.get("idUsuario"));
    return usuarioService.buscarUsuarios(criteria);
  }

  @PostMapping("/usuarios")
  @ResponseStatus(HttpStatus.CREATED)
  public Usuario guardar(
      @RequestBody Usuario usuario,
      @RequestHeader("Authorization") String token) {
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    authService.verificarAcceso(Collections.singletonList(Rol.ADMINISTRADOR), (int) claims.get("idUsuario"));
    return usuarioService.guardar(usuario);
  }

  @PutMapping("/usuarios")
  @ResponseStatus(HttpStatus.OK)
  public void actualizar(
      @RequestBody Usuario usuario,
      @RequestHeader("Authorization") String token) {
    Claims claims =
        Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
    authService.verificarAcceso(Collections.singletonList(Rol.ADMINISTRADOR), (int) claims.get("idUsuario"));
    usuarioService.actualizar(usuario, (int) claims.get("idUsuario"));
  }

    @PutMapping("/usuarios/{idUsuario}/empresas/{idEmpresaPredeterminada}")
    @ResponseStatus(HttpStatus.OK)
    public void actualizarIdEmpresaDeUsuario(@PathVariable long idUsuario, @PathVariable long idEmpresaPredeterminada) {
       usuarioService.actualizarIdEmpresaDeUsuario(idUsuario, idEmpresaPredeterminada);
    }
    
    @DeleteMapping("/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idUsuario,
                         @RequestHeader("Authorization") String token) {
        Claims claims = Jwts.parser().setSigningKey(secretkey).parseClaimsJws(token.substring(7)).getBody();
        authService.verificarAcceso(Collections.singletonList(Rol.ADMINISTRADOR), (int) claims.get("idUsuario"));
        usuarioService.eliminar(idUsuario);
    }    
}