package sic.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import sic.modelo.Rol;
import sic.modelo.Usuario;

public class UsuarioBuilder {
    
    private long id_Usuario = 0L;
    private String username = "daenta";
    private String password = "LaQueNoArde";
    private String nombre = "Daenerys";
    private String apellido = "Targaryen";
    private String email = "daenerys@gmail.com";
    private String token = "yJhbGci1NiIsInR5cCI6IkpXVCJ9.eyJub21icmUiOiJjZWNpbGlvIn0.MCfaorSC7Wdc8rSW7BJizasfzsm";
    private String passwordRecoveryKey = "";
    private Date passwordRecoveryKeyExpireDate = new Date();
    private List<Rol> roles = new ArrayList<>(Arrays.asList(Rol.ADMINISTRADOR));
    private boolean habilitado = true;
    private boolean eliminado = false;
    private long idEmpresa = 0l;
    
    public Usuario build() {
        return new Usuario(id_Usuario, username, password, nombre, apellido, email, token, idEmpresa, passwordRecoveryKey,
                passwordRecoveryKeyExpireDate, roles, habilitado, eliminado);
    }

    public UsuarioBuilder withId_Usuario(long idUsuario) {
        this.id_Usuario = idUsuario;
        return this;
    }

    public UsuarioBuilder withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public UsuarioBuilder withPassword(String password) {
        this.password = password;
        return this;
    }
    
    public UsuarioBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }
    
    public UsuarioBuilder withApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }
    
    public UsuarioBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public UsuarioBuilder withToken(String token) {
        this.token = token;
        return this;
    }
    
    public UsuarioBuilder withidEmpresa(long idEmpresa) {
        this.idEmpresa = idEmpresa;
        return this;
    }
    
    public UsuarioBuilder withPasswordRecoveryKey(String passwordRecoveryKey) {
        this.passwordRecoveryKey = passwordRecoveryKey;
        return this;
    }

    public UsuarioBuilder withPasswordRecoveryKeyExpireDate(Date passwordRecoveryKeyExpireDate) {
        this.passwordRecoveryKeyExpireDate = passwordRecoveryKeyExpireDate;
        return this;
    }
    
    public UsuarioBuilder withRol(ArrayList<Rol> roles) {
        this.roles = roles;
        return this;
    }
    
    public UsuarioBuilder withHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
        return this;
    }
    
    public UsuarioBuilder withEliminado(boolean eliminado) {
        this.eliminado = eliminado;
        return this;
    }
}
