package sic.controller;

import java.util.List;

import org.omg.PortableServer.POAPackage.AdapterAlreadyExistsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
    public Usuario guardar(@RequestBody Usuario usuario,
                           @RequestParam(required = false) long idCliente) {
        return usuarioService.guardar(usuario, idCliente);
    }
    
    @PutMapping("/usuarios")
    @ResponseStatus(HttpStatus.OK)
    public void actualizar(@RequestBody Usuario usuario,
                           @RequestParam(required = false) long idCliente) {
       usuarioService.actualizar(usuario, idCliente);
    }
    
    @PutMapping("/usuarios/{idUsuario}/empresas/{idEmpresaPredeterminada}")
    @ResponseStatus(HttpStatus.OK)
    public void actualizarIdEmpresaDeUsuario(@PathVariable long idUsuario, @PathVariable long idEmpresaPredeterminada) {
       usuarioService.actualizarIdEmpresaDeUsuario(idUsuario, idEmpresaPredeterminada);
    }
    
    @DeleteMapping("/usuarios/{idUsuario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long idUsuario) {
        usuarioService.eliminar(idUsuario);
    }    
}