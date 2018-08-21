package sic.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

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
@JsonIgnoreProperties({"token", "passwordRecoveryKey", "passwordRecoveryKeyExpireDate", "eliminado"})
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
    
    @Column(length = 300)
    private String token;
    
    private long idEmpresaPredeterminada;

    private String passwordRecoveryKey;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordRecoveryKeyExpireDate;
    
    @ElementCollection(targetClass = Rol.class)
    @CollectionTable(name="rol", joinColumns = @JoinColumn(name = "id_Usuario"))
    @Enumerated(EnumType.STRING)
    @Column(name="nombre")
    private List<Rol> roles;

    private boolean habilitado;
    
    private boolean eliminado;
        
}