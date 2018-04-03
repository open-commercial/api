package sic.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"username", "email"})
@ToString(exclude = {"roles", "password"})
public class Usuario implements Serializable {

    @Id
    @GeneratedValue
    private long id_Usuario;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = true)
    private String token;
    
    private long idEmpresa;

    private long passwordRecoveryKey;
    
    @ElementCollection(targetClass = Rol.class)
    @CollectionTable(name="rol", joinColumns = @JoinColumn(name = "id_Usuario"))
    @Enumerated(EnumType.STRING)
    @Column(name="nombre")
    private List<Rol> roles;

    private boolean habilitado;
    
    private boolean eliminado;
        
}