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

@Entity
@Table(name = "usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
public class Usuario implements Serializable {

    @Id
    @GeneratedValue
    private long id_Usuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @Column(nullable = true)
    private String token;
    
    @ElementCollection(targetClass = Rol.class)
    @CollectionTable(name="rol",
        joinColumns = @JoinColumn(name = "id_Usuario"))
    @Enumerated(EnumType.STRING)
    @Column(name="nombre")
    private List<Rol> roles;

    private boolean eliminado;

    @Override
    public String toString() {
        return nombre;
    }
}
