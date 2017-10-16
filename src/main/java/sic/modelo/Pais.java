package sic.modelo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pais")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"nombre"})
public class Pais implements Serializable {

    @Id
    @GeneratedValue
    private long id_Pais;

    @Column(nullable = false)
    private String nombre;
    
    private boolean eliminado;

    @Override
    public String toString() {
        return nombre;
    }

}
