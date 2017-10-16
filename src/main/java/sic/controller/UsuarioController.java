package sic.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.Rol;
import sic.modelo.Usuario;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class UsuarioController {
    
    private final IUsuarioService usuarioService;
    
    @Autowired
    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    @GetMapping("/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.OK)
    public Usuario getUsuarioPorId(@PathVariable long idUsuario) {
        return usuarioService.getUsuarioPorId(idUsuario);
    }
    
    @GetMapping("/usuarios/busqueda")
    @ResponseStatus(HttpStatus.OK)
    public Usuario getUsuarioPorNombre(@RequestParam String nombre) {
        return usuarioService.getUsuarioPorNombre(nombre);
    }
    
    @GetMapping("/usuarios")
    @ResponseStatus(HttpStatus.OK)
    public List<Usuario> getUsuarios() {
        return usuarioService.getUsuarios();
    }
    
    @GetMapping("/usuarios/roles")
    @ResponseStatus(HttpStatus.OK)
    public List<Usuario> getUsuariosPorRol(@RequestParam Rol rol) {
        return usuarioService.getUsuariosPorRol(rol);
    }
    
    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario guardar(@RequestBody Usuario usuario) {
        return usuarioService.guardar(usuario);
    }
    
    @PutMapping("/usuarios")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Usuario usuario) {
       usuarioService.actualizar(usuario);
    }
    
    @DeleteMapping("/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idUsuario) {
        usuarioService.eliminar(idUsuario);
    }    
}