package sic.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sic.modelo.Rol;
import sic.modelo.Usuario;

public class UsuarioBuilder {
    
    private long id_Usuario = 0L;
    private String nombre = "Daenerys Targaryen";
    private String password = "LaQueNoArde";
    private String token = "yJhbGci1NiIsInR5cCI6IkpXVCJ9.eyJub21icmUiOiJjZWNpbGlvIn0.MCfaorSC7Wdc8rSW7BJizasfzsm";
    private List<Rol> roles = new ArrayList<>(Arrays.asList(Rol.ADMINISTRADOR));
    private boolean eliminado = false;
    
    public Usuario build() {
        return new Usuario(id_Usuario, nombre, password, token, roles, eliminado);
    }
    
    public UsuarioBuilder withId_Usuario(long idUsuario) {
        this.id_Usuario = idUsuario;
        return this;
    }
    
    public UsuarioBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public UsuarioBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
    
    public UsuarioBuilder withToken(String token) {
        this.token = token;
        return this;
    }
    
    public UsuarioBuilder withRol(ArrayList<Rol> roles) {
        this.roles = roles;
        return this;
    }
    
    public UsuarioBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
